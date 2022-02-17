package ru.androidclass.easybackup.storage;

import android.app.Application;

import java.io.File;
import java.io.IOException;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.utils.ZipUtils;

/**
 * Class for backup and restore application's files
 */
public class InternalStorageFilesBackup implements Backup {
    private final File mBackupFile;
    private final File mRestoreFile;
    private final String mPath;

    public InternalStorageFilesBackup(Application application, File backupFile, File restoreFile, String relativePath) {
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mPath = application.getFilesDir() + relativePath;
    }

    @Override
    public void backup() throws BackupException {
        try {
            ZipUtils.zipFolder(new File(mPath), mBackupFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BackupException(e);
        }
    }

    @Override
    public void restore() throws RestoreException {
        try {
            ZipUtils.unZip(mRestoreFile, mPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RestoreException(e);
        }
    }
}
