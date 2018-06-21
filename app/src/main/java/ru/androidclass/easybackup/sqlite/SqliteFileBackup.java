package ru.androidclass.easybackup.sqlite;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;

/**
 * Class for backup and restore application's sqlite database
 */
public class SqliteFileBackup implements Backup {
    private File mBackupFile;
    private File mRestoreFile;
    private String mDBName;
    private Context mContext;

    public SqliteFileBackup(Application application, File backupFile, File restoreFile, String dBName) {
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
        mDBName = dBName;
        mContext = application;
    }

    @Override
    public void backup() throws BackupException {
        File database = mContext.getDatabasePath(mDBName);
        try {
            FileChannel src = new FileInputStream(database).getChannel();
            FileChannel dst = new FileOutputStream(mBackupFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new BackupException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BackupException(e);
        }
    }

    @Override
    public void restore() throws RestoreException {
        File database = mContext.getDatabasePath(mDBName);
        try {
            FileChannel src = new FileInputStream(mRestoreFile).getChannel();
            FileChannel dst = new FileOutputStream(database).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RestoreException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RestoreException(e);
        }
    }
}
