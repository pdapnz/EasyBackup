package ru.androidclass.easybackup.storage;

import java.io.File;
import java.io.IOException;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.utils.ZipUtils;

/**
 * Class for backup and restore application's files
 */
public class StorageFilesBackup implements Backup {
    private final File mBackupFile;
    private final File mRestoreFile;
    private final String mSourcePath;

    public StorageFilesBackup(File backupFile, File restoreFile, String sourcePath) {
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mSourcePath = sourcePath;
    }

    @Override
    public void backup() throws BackupException {
        try {
            ZipUtils.zipFolder(new File(mSourcePath), mBackupFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BackupException(e);
        }
    }

    @Override
    public void restore() throws RestoreException {
        try {
            ZipUtils.unZip(mRestoreFile, mSourcePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RestoreException(e);
        }
    }
}
