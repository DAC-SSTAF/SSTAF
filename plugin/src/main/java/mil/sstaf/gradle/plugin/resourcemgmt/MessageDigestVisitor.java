package mil.sstaf.gradle.plugin.resourcemgmt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of {@code FileVisitor} that generates {@code MessageDigest} checksums for each file
 * in a directory tree.
 *
 * <p>
 * This class generates a checksum report for each directory in the directory graph. The report is encoded
 * in JSON and is written to a file that is always named "checksums.json."
 * </p>
 *
 * <p>
 * The hashing algorithm is specified in the constructor. If the algorithm is null or zero-length, the
 * class falls back to using MD5. The algorithm used to generate the checksums is recorded in the "checksums.json"
 * file. The property name for the algorithm is "__hashAlgorithm".
 * </p>
 */
class MessageDigestVisitor implements FileVisitor<Path> {

    private final Path resourceDir;
    private final String hashAlgorithm;
    private final MessageDigest messageDigest;
    private final ObjectNode hashReport;
    private final ObjectMapper objectMapper;
    private int depth;

    /**
     * Constructor
     *
     * @param resourceDir   the top-level directory for the tree walk. This is used to compute relative paths
     *                      for sub-directories.
     * @param hashAlgorithm the hashing algorithm to use.
     * @throws NoSuchAlgorithmException if a non-existent algorithm is selected.
     */
    public MessageDigestVisitor(final Path resourceDir, final String hashAlgorithm) throws NoSuchAlgorithmException {
        this.hashAlgorithm = hashAlgorithm == null ? "MD5" : hashAlgorithm;
        this.resourceDir = resourceDir;
        messageDigest = MessageDigest.getInstance(this.hashAlgorithm);

        Path reportFilePath = Path.of(resourceDir.toString(), "checksums.json");
        File oldReportFile = reportFilePath.toFile();
        if (oldReportFile.exists()) {
            final boolean delete = oldReportFile.delete();
            if (!delete) {
                System.err.println("sss");
            }
        }
        objectMapper = new ObjectMapper();
        hashReport = objectMapper.createObjectNode();
        hashReport.put("__hashAlgorithm", hashAlgorithm);
        depth = 0;
    }

    /**
     * Method executed at the beginning of directory traversal.
     *
     * <p>
     * Upon entering a new directory, a new {@code JSONObject} is created to hold the checksum report.
     * The "__hashAlgorithm" and "__path" properties are added to it and the {@code JSONObject) is
     * pushed onto a stack. The stack maintains separate reports for each directory in the tree.
     * </p>
     *
     * @param dir   the {@code Path} to the directory being visited
     * @param attrs file attributes (not used)
     * @return {@code FileVisitResult.CONTINUE}
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        ++depth;
        return FileVisitResult.CONTINUE;
    }

    /**
     * Method executed for each file.
     * <p>
     * Given a {@code Path} to a file, this method reads the contents of the file and routes them
     * into a {@code MessageDigest}. The {@code MessageDigest} then generates a digest (checksum) of
     * the bytes from the file. This value is then serialized into a String and recorded in the
     * hash report JSONObject.
     * </p>
     *
     * @param file  the {@code Path} to the file being visited
     * @param attrs file attributes (not used)
     * @return {@code FileVisitResult.CONTINUE}
     * @throws IOException if the file can not be read.
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relative = resourceDir.relativize(file);

        byte[] buffer = new byte[1024];
        int read = 0;
        messageDigest.reset();
        InputStream inputStream = new FileInputStream(file.toFile());
        while ((read = inputStream.read(buffer)) > 0) {
            messageDigest.update(buffer, 0, read);
        }
        byte[] checksum = messageDigest.digest();
        String encoded = bytesToString(checksum);

        hashReport.put(relative.toString(), encoded);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Method executed when something goes wrong.
     *
     * <p>Terminates traversal of the directory tree</p>
     *
     * @param file the {@code Path} to the file or directory that caused the problem.
     * @param exc  the {@code IOException=} that was thrown
     * @return {@code FileVisitResult.TERMINATE}
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println("Failed to process: " + file.toAbsolutePath());
        exc.printStackTrace(System.err);
        return FileVisitResult.TERMINATE;
    }

    /**
     * Method executed when a directory has been processed completely. This includes all subdirectories.
     *
     * <p>
     * When directory traversal is complete, the hash report for the current directory is popped from
     * the directory stack and written to the "checksums.json" file.
     * </p>
     *
     * @param dir the {@code Path} to the directory being visited
     * @param exc Any {@code IOException} thrown during traversal.
     * @return {@code FileVisitResult.CONTINUE}
     * @throws IOException if the report can not be written.
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            --depth;
            if (depth == 0) {
                Path reportFilePath = Path.of(dir.toString(), "checksums.json");
                FileWriter fileWriter = new FileWriter(reportFilePath.toFile());
                fileWriter.write(hashReport.toPrettyString());
                fileWriter.flush();
                fileWriter.close();
            }
            return FileVisitResult.CONTINUE;
        } else {
            System.err.println("Failed to process: " + dir.toAbsolutePath());
            exc.printStackTrace(System.err);
            return FileVisitResult.TERMINATE;
        }
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

}
