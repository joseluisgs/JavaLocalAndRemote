package dev.joseluisgs.error;

public abstract class TenistaError {
    private final String message;

    public TenistaError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static class StorageError extends TenistaError {
        public StorageError(String message) {
            super("ERROR: " + message);
        }
    }
    
}
