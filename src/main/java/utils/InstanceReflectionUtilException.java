package utils;

public class InstanceReflectionUtilException extends RuntimeException {
    public InstanceReflectionUtilException(String message) {
        super(message);
    }

    public InstanceReflectionUtilException(Exception exception) {
        super(exception);
    }
}
