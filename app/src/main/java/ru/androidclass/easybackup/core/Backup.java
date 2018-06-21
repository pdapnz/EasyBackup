package ru.androidclass.easybackup.core;

import java.io.FileNotFoundException;

import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;

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
