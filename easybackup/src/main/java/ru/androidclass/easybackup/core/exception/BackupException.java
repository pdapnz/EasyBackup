package ru.androidclass.easybackup.core.exception;

public class BackupException extends Exception {
    public BackupException(Throwable cause) {
        super(cause);
    }

    public BackupException(String message) {
        super(message);
    }
}
