package mil.sstaf.core.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;

/**
 * Manages access to resources that must be extracted to disk for use.
 */
public class ResourceManager {

    public static final String HASH_ALGORITHM = "__hashAlgorithm";
    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private final Map<String, File> resourceFiles = new HashMap<>();
    private Path directory;
    private int numWritten = 0;

    /**
     * Constructor
     *
     * @param resourceOwner the {@code Class}
     */
    private ResourceManager(final Class<? extends Feature> resourceOwner, Path where) {
        new Extractor(resourceOwner).extract(where);
    }

    /**
     * Factory
     *
     * @param resourceOwner the {@code Class} that owns the resources.
     * @return a ResourceManager
     */
    public static ResourceManager getManager(final Class<? extends Feature> resourceOwner) {
        return new ResourceManager(resourceOwner, null);
    }

    /**
     * Factory
     *
     * @param resourceOwner   the {@code Class} that owns the resources.
     * @param installationDir where resources should be installed
     * @return a ResourceManager
     */
    public static ResourceManager getManager(final Class<? extends Feature> resourceOwner, Path installationDir) {
        return new ResourceManager(resourceOwner, installationDir);
    }

    /**
     * Extracts an item from the resources package and writes it to a {@link File}.
     *
     * <p>
     * Some {@code Feature} implementation rely on external applications to perform their function. These
     * applications might require access to items that are packaged as resources. Since files in the jar
     * are not accessible it is necessary to extract the resources and write them as normal files.
     * </p>
     *
     * @param resourceOwner the {@code Class} that owns the resource
     * @param resourceName  the name of the resource
     * @param tempDir       the directory into which the resource should be copied
     * @return a {@code File} that points to the extracted resource
     * @throws IOException if any of the {@code File} operations fail
     */
    public static File extractResource(Class<?> resourceOwner, String resourceName, Path tempDir) throws IOException {
        Objects.requireNonNull(resourceOwner, "resourceOwner");
        Objects.requireNonNull(resourceName, "resourceName");
        Objects.requireNonNull(tempDir, "tempDir");
        Module ownerModule = resourceOwner.getModule();

         InputStream is = ownerModule.getResourceAsStream(resourceName);
         if (is == null) {
            logger.error("Resource {} was not found in module {}", resourceName,
                    ownerModule.getName());
            throw new IOException("Resource '" + resourceName + "' was not found in module '"
                    + ownerModule.getName() + "'");
        }
        Path path;

        if (resourceName.contains("/")) {
            int chopAt = resourceName.lastIndexOf("/");
            String resourceDirPath = resourceName.substring(0, chopAt);
            String terminal = resourceName.substring(chopAt + 1);
            //
            // Using Matcher below fixes the problems caused by the Windows file separator being treated
            // as an escape character. Using File.separator alone results in an IllegalArgumentException
            //
            String resourceDirPathLocalFS = resourceDirPath.replaceAll("/",  Matcher.quoteReplacement(File.separator));
            Path dir = Path.of(String.valueOf(tempDir), resourceDirPathLocalFS);
            Files.createDirectories(dir);
            path = Path.of(String.valueOf(dir), terminal);
        } else {
            String resourceNameLocalFS = resourceName.replaceAll("/", File.separator);
            path = Path.of(tempDir.toString(), resourceNameLocalFS);
        }
        logger.debug("Extracting {} to {}", resourceName, path);
        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        return path.toFile();
    }

    /**
     * Returns the number of resources extracted and written to disk.
     *
     * <p>
     * The number will range from 0 to the number of resources depending on whether or not a resource is
     * already on disk.
     * </p>
     *
     * @return the number of files written
     */
    public int getNumWritten() {
        return numWritten;
    }

    /**
     * Returns the top-level directory into which the resources are written.
     *
     * @return the directory
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * Returns the {@code Map} that contains the association of resource names to {@code File}s.
     *
     * @return the {@code Map}
     */
    public Map<String, File> getResourceFiles() {
        return resourceFiles;
    }

    /**
     * Utility class for extracting and verifying
     */
    private class Extractor {

        private final Class<? extends Feature> resourceOwner;
        private final Map<String, String> checksums = new TreeMap<>();
        private MessageDigest messageDigest;
        private boolean locationIsNew;
        private List<String> resourcesToExtract;
        private String algorithm;

        private Extractor(Class<? extends Feature> owner) {
            this.resourceOwner = owner;
        }

        void extract(Path where) {
            try {
                determineResourcesToExtract();
                retrieveChecksums();
                this.messageDigest = MessageDigest.getInstance(algorithm);
                verifyChecksumExistForResources();
                makeExtractionLocation(where);
                writeResources();
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new SSTAFException("The " + algorithm + " algorithm is not available");
            } catch (IOException exception) {
                throw new SSTAFException(exception);
            }
        }


        /**
         * Reads the manifest to determine what to pull.
         */
        void determineResourcesToExtract() throws IOException {
            CodeSource codeSource = resourceOwner.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL sourceJar = codeSource.getLocation();
                JarFile jarFile = new JarFile(sourceJar.getFile());
                Manifest mf = jarFile.getManifest();
                String value = mf.getMainAttributes().getValue("SSTAF-Resources-To-Extract");
                if (value == null) {
                    resourcesToExtract = List.of();
                } else {
                    resourcesToExtract = List.of(value.split(" "));
                }
            } else {
                resourcesToExtract = List.of();
            }
        }

        /**
         * Computes the checksum for the specified file using the {@code MessageDigest} algorithm
         * that was specified in the checksum file.
         *
         * @param file the file for which the checksum is to be computed
         * @return the checksum as a String
         * @throws IOException if something doesn't work
         */
        public String computeChecksum(final Path file) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            messageDigest.reset();
            InputStream inputStream = new FileInputStream(file.toFile());
            while ((read = inputStream.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, read);
            }
            byte[] checksum = messageDigest.digest();
            return bytesToString(checksum);
        }

        /**
         * Converts the byte array from the {@code MessageDigest} into a hex string.
         *
         * @param bytes the bytes
         * @return a {@code String} containing hexadecimal values.
         */
        private String bytesToString(byte[] bytes) {
            StringBuilder hexString = new StringBuilder();
            for (byte aByte : bytes) {
                if ((0xff & aByte) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & aByte)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & aByte));
                }
            }
            return hexString.toString();

        }

        /**
         * Creates or confirms directory for resource extraction.
         *
         * @param where the {@code Path} for the directory.
         */
        private void makeExtractionLocation(final Path where) {
            try {
                if (where == null) {
                    directory = Files.createTempDirectory(resourceOwner.getSimpleName());
                    locationIsNew = true;
                } else {
                    File f = where.toFile();
                    if (!(f.exists() && f.canRead() && f.canWrite())) {
                        throw new SSTAFException("Can't access specified resource directory " + where);
                    }
                    directory = where;
                    locationIsNew = false;
                }
            } catch (IOException exception) {
                throw new SSTAFException("Can't create temp directory");
            }
        }

        /**
         * Compares the checksum for a file on disk with the matching one in the checksum file.
         *
         * @param name the name of the resource
         * @param p    the path to the file on disk
         * @return true if the checksums match, false otherwise
         * @throws IOException if something bad happens.
         */
        private boolean compareChecksums(String name, Path p) throws IOException {
            String hashFromDisk = computeChecksum(p);
            String hashFromJar = checksums.get(name);
            return Objects.equals(hashFromJar, hashFromDisk);
        }

        /**
         * Writes all resources specified in the manifest to the desired directory
         *
         * @throws IOException if something bad happens
         */
        private void writeResources() throws IOException {
            for (String resource : resourcesToExtract) {
                File resourceFile;
                if (needToExtract(resource)) {
                    resourceFile = extractResource(resourceOwner, resource, directory);
                    Path p = Path.of(resourceFile.toString());
                    if (compareChecksums(resource, p)) {
                        ++numWritten;
                        resourceFiles.put(resource, resourceFile);
                    } else {
                        throw new SSTAFException("The checksum for the newly-written file doesn't match");
                    }
                } else {
                    resourceFile = Path.of(directory.toString(), resource).toFile();
                }

                resourceFiles.put(resource, resourceFile);
            }
        }

        /**
         * Determines whether a resource needs to be written to disk.
         *
         * @param name the name of the resource
         * @return true if the resource must be written, false otherwise.
         * @throws IOException if something doesn't work
         */
        private boolean needToExtract(String name) throws IOException {
            if (locationIsNew) {
                return true;
            } else {
                Path p = Path.of(directory.toString(), name);
                File f = p.toFile();
                if (f.exists() && f.canRead()) {
                    if (!compareChecksums(name, p)) {
                        logger.warn("The hashes for " + name + " do not match");
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }

        /**
         * Retrieves the checksums (hashes) that were recorded in the jar.
         */
        private void retrieveChecksums() {
            try {
                InputStream is = resourceOwner.getModule().getResourceAsStream("/checksums.json");
                ObjectMapper om = new ObjectMapper();
                JsonNode node = om.readTree(is);
                ObjectNode co = (ObjectNode) node;
                algorithm = co.get(HASH_ALGORITHM).asText();
                Iterator<String> iter = co.fieldNames();
                while(iter.hasNext()) {
                    String key = iter.next();
                    if (!Objects.equals(key, HASH_ALGORITHM)) {
                        String val = co.get(key).asText();
                        checksums.put(key, val);
                    }
                }
            } catch (IOException e) {
                throw new SSTAFException("Could not read checksum values in " + resourceOwner.getModule().getName(),
                        e);
            }
        }

        /**
         * Checks that a checksum exists for each file that is to be extracted.
         */
        private void verifyChecksumExistForResources() {
            List<String> missing = new ArrayList<>();
            for (String file : resourcesToExtract) {
                if (!checksums.containsKey(file)) {
                    missing.add(file);
                }
            }
            if (!missing.isEmpty()) {
                throw new SSTAFException("Checksum table did not contain entries for: " + missing);
            }
        }

    }


}


