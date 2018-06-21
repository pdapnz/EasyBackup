package ru.androidclass.backup.core;

import java.io.FileNotFoundException;

import ru.androidclass.backup.core.exception.BackupException;
import ru.androidclass.backup.core.exception.RestoreException;

/**
 * Base class for running backups.
 * */
public interface Backup {
    /**
     * This method is called for backup data
     */
    void backup() throws BackupException;
    /**
     * This method is called for restore data
     */
    void restore() throws RestoreException;
}
