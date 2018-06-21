package ru.androidclass.backup.sharedpreferences;

import android.content.SharedPreferences;

import java.io.File;

import ru.androidclass.backup.core.Backup;
import ru.androidclass.backup.core.BackupCreator;

/**
 * Fabric class for creating {@link SharedPreferencesFileBackup}.
 */
public class SharedPreferencesFileBackupCreator implements BackupCreator {
    private SharedPreferences mSharedPreferences;
    private File mBackupFile;
    private File mRestoreFile;

    public SharedPreferencesFileBackupCreator(SharedPreferences sharedPreferences, File backupFile, File restoreFile) {
        mSharedPreferences = sharedPreferences;
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
    }

    @Override
    public Backup create() {
        return new SharedPreferencesFileBackup(mSharedPreferences, mBackupFile, mRestoreFile);
    }
}
