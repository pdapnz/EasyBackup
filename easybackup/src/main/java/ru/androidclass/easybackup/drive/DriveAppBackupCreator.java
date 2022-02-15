package ru.androidclass.easybackup.drive;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.drive.Drive;

import java.util.List;

import ru.androidclass.easybackup.core.Backup;
import ru.androidclass.easybackup.core.BackupCreator;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;

public class DriveAppBackupCreator implements BackupCreator {

    private final Application mApplication;
    private final List<String> mPrefsNames;
    private final List<String> mDbsNames;
    private final List<String> mFilePaths;
    private final Drive mDriveService;

    public DriveAppBackupCreator(@NonNull Application application, Drive driveService,
                                 @Nullable List<String> prefsNames,
                                 @Nullable List<String> dbsNames,
                                 @Nullable List<String> filePaths
    ) {
        mApplication = application;
        mPrefsNames = prefsNames;
        mDbsNames = dbsNames;
        mFilePaths = filePaths;
        mDriveService = driveService;
    }

    @Override
    public Backup create() throws BackupInitializationException {
        return new DriveAppBackup(mApplication, mDriveService, mPrefsNames, mDbsNames, mFilePaths);
    }
}
