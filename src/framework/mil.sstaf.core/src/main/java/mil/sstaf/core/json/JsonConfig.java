package mil.sstaf.core.json;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JsonConfig {

    private static final AtomicReference<JsonConfig> reference = new AtomicReference<>();
    private final Lock lock = new ReentrantLock();
    private Path rootDir;

    public static JsonConfig getInstance() {
        synchronized (reference) {
            JsonConfig jsonConfig = reference.get();
            if (jsonConfig == null) {
                jsonConfig = new JsonConfig();
                reference.set(jsonConfig);
            }
            return jsonConfig;
        }
    }

    private JsonConfig() {
        String cwd = System.getProperty("user.dir");
        rootDir = Path.of(cwd);
    }

    public void setRootDir(Path rootDir) {
        Objects.requireNonNull(rootDir);
        try {
            lock.lock();
            this.rootDir = rootDir;
        } finally {
            lock.unlock();
        }
    }

    public Path getRootDir() {
        try {
            lock.lock();
            return rootDir;
        } finally {
            lock.unlock();
        }
    }
}
