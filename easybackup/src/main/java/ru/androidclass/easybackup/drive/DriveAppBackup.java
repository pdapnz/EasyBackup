package ru.androidclass.easybackup.drive;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import ru.androidclass.easybackup.storage.StorageFilesBackupCreator;

public class DriveAppBackup implements Backup {
    private static final String TAG = DriveAppBackup.class.getSimpleName();

    private final List<java.io.File> mPrefsTempFiles = new ArrayList<>();
    private final List<java.io.File> mDbsTempFiles = new ArrayList<>();
    private final List<java.io.File> mFilesTempFiles = new ArrayList<>();
    private final BackupManager mBackupManager = new BackupManager();
    private final Drive mDriveService;
    private final String mBackupFolderName;


    public DriveAppBackup(@NonNull Application application, Drive driveService,
                          @Nullable List<String> prefsNames,
                          @Nullable List<String> dbsNames,
                          @Nullable List<String> filePaths
    ) throws BackupInitializationException {
        mDriveService = driveService;
        mBackupFolderName = "easyBackup_" + application.getPackageName();

        if (prefsNames != null && prefsNames.size() > 0) {
            for (String name : prefsNames) {
                java.io.File tempFile = new java.io.File(application.getCacheDir(), "backup.sp_" + name);
                mPrefsTempFiles.add(tempFile);
                mBackupManager.addBackupCreator(new SharedPreferencesFileBackupCreator(application, name, tempFile, tempFile));
            }
        }
        if (dbsNames != null && dbsNames.size() > 0) {
            for (String name : dbsNames) {
                java.io.File tempFile = new java.io.File(application.getCacheDir(), "backup.db_" + name);
                mDbsTempFiles.add(tempFile);
                mBackupManager.addBackupCreator(new SqliteFileBackupCreator(application, tempFile, tempFile, name));
            }
        }
        if (filePaths != null && filePaths.size() > 0) {
            for (String path : filePaths) {
                java.io.File tempFile = null;
                try {
                    tempFile = new java.io.File(application.getCacheDir(), "files.zip_" + URLEncoder.encode(path, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    throw new BackupInitializationException(e);
                }
                mFilesTempFiles.add(tempFile);
                mBackupManager.addBackupCreator(new StorageFilesBackupCreator(tempFile, tempFile, path));
            }
        }
    }

    private void backupAll() throws BackupException, BackupInitializationException {
        mBackupManager.backupAll();
    }

    private void restoreAll() throws RestoreException, BackupInitializationException {
        mBackupManager.restoreAll();
    }

    private void pushFile(String folderId, java.io.File inputFile) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(inputFile.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));
        FileContent mediaContent = new FileContent(null, inputFile);
        File file = mDriveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, createdTime")
                .execute();
        Log.d(TAG, "File created: " + file.getName() + " (" + file.getId() + ")" + " " + file.getCreatedTime());
    }

    private void pullFiles() throws IOException {
        String pageToken = null;
        do {
            FileList files = mDriveService.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType)")
                    .setOrderBy("createdTime desc")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : files.getFiles()) {
                Log.d(TAG, "Found file: " + file.getCreatedTime() + " " + file.getName() + " " + file.getMimeType() + " (" + file.getId() + ")");
            }
            pageToken = files.getNextPageToken();
        } while (pageToken != null);
    }

    @Nullable
    public File getBackupFolder() throws IOException {
        FileList files = mDriveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='" + mBackupFolderName + "'")
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name, createdTime)")
                .setOrderBy("createdTime desc")
                .setPageSize(1)
                .execute();
        for (File file : files.getFiles()) {
            Log.d(TAG, "Found folder: " + file.getName() + " (" + file.getId() + ")" + " " + file.getCreatedTime());
            return file;
        }
        return null;
    }

    @NonNull
    private File createBackupFolder() throws IOException {
        File fileMetadata = new File();
        fileMetadata.setParents(Collections.singletonList("appDataFolder"));
        fileMetadata.setName(mBackupFolderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file = mDriveService.files().create(fileMetadata)
                .setFields("id, name, createdTime")
                .execute();
        Log.d(TAG, "Folder created: " + file.getName() + " (" + file.getId() + ")" + " " + file.getCreatedTime());
        return file;
    }

    private void removeBackupFolder(String folderId) throws IOException {
        mDriveService.files()
                .delete(folderId)
                .execute();
        Log.d(TAG, "Folder with id=" + folderId + " was removed.");
    }


    @Override
    public void backup() throws BackupException {
        try {
            backupAll();

        } catch (BackupInitializationException e) {
            e.printStackTrace();
            throw new BackupException(e);
        }
        try {
            File folder = getBackupFolder();
            //recreate folder if exist (to store only last actual backup)
            if (folder != null)
                removeBackupFolder(folder.getId());
            folder = createBackupFolder();

            for (java.io.File tempFile : mPrefsTempFiles)
                pushFile(folder.getId(), tempFile);
            for (java.io.File tempFile : mDbsTempFiles)
                pushFile(folder.getId(), tempFile);
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            throw new RestoreException(e);
        }
    }
}
