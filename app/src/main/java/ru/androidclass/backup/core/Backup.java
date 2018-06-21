package ru.androidclass.backup.core;

/**
 * Base class for running backups.
 * */
public interface Backup {
    /**
     * This method is called for backup data
     */
    void backup() throws BackupManagerBackupException;
    /**
     * This method is called for restore data
     */
    void restore() throws BackupManagerRestoreException;
}
