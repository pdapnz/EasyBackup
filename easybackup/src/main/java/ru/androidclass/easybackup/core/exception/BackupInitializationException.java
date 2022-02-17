package ru.androidclass.easybackup.core.exception;

public class BackupInitializationException extends Exception {
    public BackupInitializationException(Throwable cause) {
        super(cause);
    }

    public BackupInitializationException(String message) {
        super(message);
    }
}
