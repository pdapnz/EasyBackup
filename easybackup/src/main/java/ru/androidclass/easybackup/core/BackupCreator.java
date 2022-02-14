package ru.androidclass.easybackup.core;

import ru.androidclass.easybackup.core.exception.BackupInitializationException;

/**
 * Fabric class for creating implementations of {@link Backup}
 */
public interface BackupCreator {
    Backup create() throws BackupInitializationException;
}
