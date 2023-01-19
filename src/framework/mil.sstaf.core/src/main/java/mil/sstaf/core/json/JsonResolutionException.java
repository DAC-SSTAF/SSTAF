package mil.sstaf.core.json;

import mil.sstaf.core.util.SSTAFException;

public class JsonResolutionException extends SSTAFException {
    public JsonResolutionException() {
    }

    public JsonResolutionException(String message) {
        super(message);
    }

    public JsonResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonResolutionException(Throwable cause) {
        super(cause);
    }

    public JsonResolutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
