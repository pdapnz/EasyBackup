package ru.androidclass.easybackup.storage;

import java.io.File;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupCreator;

/**
 * Fabric class for creating {@link StorageFilesBackup}.
 */
public class StorageFilesBackupCreator implements BackupCreator {

    private final File mBackupFile;
    private final File mRestoreFile;
    private final String mSourcePath;

    public StorageFilesBackupCreator(File backupFile, File restoreFile, String sourcePath) {
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mSourcePath = sourcePath;
    }

    @Override
    public Backup create() {
        return new StorageFilesBackup(mBackupFile, mRestoreFile, mSourcePath);
    }
}
