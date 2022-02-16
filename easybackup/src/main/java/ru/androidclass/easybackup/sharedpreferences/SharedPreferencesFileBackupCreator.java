package ru.androidclass.easybackup.sharedpreferences;

import android.app.Application;

import java.io.File;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupCreator;

/**
 * Fabric class for creating {@link SharedPreferencesFileBackup}.
 */
public class SharedPreferencesFileBackupCreator implements BackupCreator {
    private final Application mApplication;
    private final String mSharedPreferencesName;
    private final File mBackupFile;
    private final File mRestoreFile;

    public SharedPreferencesFileBackupCreator(Application application, String sharedPreferencesName, File backupFile, File restoreFile) {
        mApplication = application;
        mSharedPreferencesName = sharedPreferencesName;
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
    }

    @Override
    public Backup create() {
        return new SharedPreferencesFileBackup(mApplication, mSharedPreferencesName, mBackupFile, mRestoreFile);
    }
}
