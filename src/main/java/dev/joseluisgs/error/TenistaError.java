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

    public static class DatabaseError extends TenistaError {
        public DatabaseError(String message) {
            super("ERROR: " + message);
        }
    }

    public static class NotFound extends TenistaError {
        public NotFound(Long id) {
            super("ERROR: No se ha encontrado el tenista con id: " + id);
        }
    }

    public static class RemoteError extends TenistaError {
        public RemoteError(String message) {
            super("ERROR: " + message);
        }
    }

    public static class ValidationError extends TenistaError {
        public ValidationError(String message) {
            super("ERROR: " + message);
        }
    }

}
