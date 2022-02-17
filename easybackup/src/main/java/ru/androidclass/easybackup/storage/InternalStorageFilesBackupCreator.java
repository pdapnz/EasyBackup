package ru.androidclass.easybackup.storage;

import android.app.Application;

import java.io.File;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupCreator;

/**
 * Fabric class for creating {@link StorageFilesBackup}.
 */
public class InternalStorageFilesBackupCreator implements BackupCreator {
    private final Application mApplication;
    private final File mBackupFile;
    private final File mRestoreFile;
    private final String mRelativePath;

    public InternalStorageFilesBackupCreator(Application application, File backupFile, File restoreFile, String relativePath) {
        mApplication = application;
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mRelativePath = relativePath;
    }

    @Override
    public Backup create() {
        return new InternalStorageFilesBackup(mApplication, mBackupFile, mRestoreFile, mRelativePath);
    }
}
