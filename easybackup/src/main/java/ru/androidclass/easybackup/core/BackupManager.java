package ru.androidclass.easybackup.core;

import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;

/**
 * Entry point for creating and restoring backups.
 */
public class BackupManager {

    private final BackupCreatorHolder mBackupCreatorHolder;

    public void addBackupCreator(BackupCreator creator) {
        mBackupCreatorHolder.addJobCreator(creator);
    }

    public void backupAll() throws BackupException {
        mBackupCreatorHolder.backup();
    }

    public void restoreAll() throws RestoreException {
        mBackupCreatorHolder.restore();
    }

    public BackupManager() {
        mBackupCreatorHolder = new BackupCreatorHolder();
    }
}
