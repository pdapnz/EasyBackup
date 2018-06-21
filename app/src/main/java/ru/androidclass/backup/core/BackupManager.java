package ru.androidclass.backup.core;

import ru.androidclass.backup.core.exception.BackupException;
import ru.androidclass.backup.core.exception.RestoreException;

/**
 * Entry point for creating and restoring backups.
 */
public class BackupManager {

    private static volatile BackupManager instance;
    private final BackupCreatorHolder mBackupCreatorHolder;

    public static BackupManager create() {
        if (instance == null) {
            synchronized (BackupManager.class) {
                if (instance == null) {
                    instance = new BackupManager();
                }
            }
        }
        return instance;
    }

    public void addBackupCreator(BackupCreator creator) {
        mBackupCreatorHolder.addJobCreator(creator);
    }

    public void backupAll() throws BackupException {
        mBackupCreatorHolder.backup();
    }

    public void restoreAll() throws RestoreException {
        mBackupCreatorHolder.restore();
    }

    private BackupManager() {
        mBackupCreatorHolder = new BackupCreatorHolder();
    }
}
