package ru.androidclass.easybackup.drive;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.sharedpreferences.SharedPreferencesFileBackupCreator;
import ru.androidclass.easybackup.sqlite.SqliteFileBackupCreator;

public class DriveAppBackup implements Backup {
    private static final String TAG = DriveAppBackup.class.getSimpleName();

    private final Application mApplication;
    private final List<String> mPrefsNames;
    private final List<String> mDbsNames;
    private final List<java.io.File> mPrefsTempFiles = new ArrayList<>();
    private final List<java.io.File> mDbsTempFiles = new ArrayList<>();
    private final BackupManager mBackupManager = new BackupManager();
    private final Drive mDriveService;


    public DriveAppBackup(@NonNull Application application, Drive driveService, @Nullable List<String> prefsNames, @Nullable List<String> dbsNames) {
        mApplication = application;
        mPrefsNames = prefsNames;
        mDbsNames = dbsNames;
        mDriveService = driveService;

        if (mPrefsNames != null && mPrefsNames.size() > 0) {
            for (String name : mPrefsNames) {
                java.io.File tempFile = new java.io.File(application.getCacheDir(), "sp_backup_" + name);
                mPrefsTempFiles.add(tempFile);
                mBackupManager.addBackupCreator(new SharedPreferencesFileBackupCreator(
                        mApplication.getSharedPreferences(name, Context.MODE_PRIVATE), tempFile, tempFile));
            }
        }
        if (mDbsNames != null && mDbsNames.size() > 0) {
            for (String name : mDbsNames) {
                java.io.File tempFile = new java.io.File(application.getCacheDir(), "db_backup_" + name);
                mDbsTempFiles.add(tempFile);
                mBackupManager.addBackupCreator(new SqliteFileBackupCreator(application, tempFile, tempFile, name));
            }
        }
    }

    private void backupAll() throws BackupException, BackupInitializationException {
        mBackupManager.backupAll();
    }

    private void restoreAll() throws RestoreException, BackupInitializationException {
        mBackupManager.restoreAll();
    }

    private void pushFile(java.io.File inputFile) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(inputFile.getName());
        fileMetadata.setParents(Collections.singletonList("appDataFolder"));
        FileContent mediaContent = new FileContent(null, inputFile);
        File file = mDriveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        Log.d(TAG, "File ID: " + file.getId());
    }

    private void pullFiles() throws IOException {
        FileList files = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setPageSize(100)
                .execute();
        for (File file : files.getFiles()) {
            Log.d(TAG, "Found file: " + file.getName() + " (" + file.getId() + ")");
        }
    }

    @Override
    public void backup() throws BackupException {
        try {
            backupAll();
        } catch (BackupInitializationException e) {
            throw new BackupException(e);
        }
        try {
            for (java.io.File tempFile : mPrefsTempFiles)
                pushFile(tempFile);
            for (java.io.File tempFile : mDbsTempFiles)
                pushFile(tempFile);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

    @Override
    public void restore() throws RestoreException {
        try {
            restoreAll();
        } catch (BackupInitializationException e) {
            throw new RestoreException(e);
        }
        try {
            pullFiles();
        } catch (IOException e) {
            throw new RestoreException(e);
        }
    }
}
