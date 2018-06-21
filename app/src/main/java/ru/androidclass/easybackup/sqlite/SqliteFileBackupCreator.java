package ru.androidclass.easybackup.sqlite;

import android.app.Application;

import java.io.File;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupCreator;

/**
 * Fabric class for creating {@link SqliteFileBackup}.
 */
public class SqliteFileBackupCreator implements BackupCreator {

    private File mBackupFile;
    private File mRestoreFile;
    private String mDBName;
    private Application mApplication;

    public SqliteFileBackupCreator(Application application, File backupFile, File restoreFile, String dBName) {
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mDBName = dBName;
        mApplication = application;
    }

    @Override
    public Backup create() {
        return new SqliteFileBackup(mApplication, mBackupFile, mRestoreFile, mDBName);
    }
}
