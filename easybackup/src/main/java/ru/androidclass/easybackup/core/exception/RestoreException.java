package ru.androidclass.easybackup.core.exception;

public class RestoreException extends Exception {
    public RestoreException(Throwable cause) {
        super(cause);
    }

    public RestoreException(String message) {
        super(message);
    }
}
