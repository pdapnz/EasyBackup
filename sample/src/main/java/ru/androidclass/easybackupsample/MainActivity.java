package ru.androidclass.easybackupsample;

import static ru.androidclass.easybackupsample.PathUtil.getPath;
import static ru.androidclass.easybackupsample.db.AppDatabase.DATABASE_NAME;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.sharedpreferences.SharedPreferencesFileBackupCreator;
import ru.androidclass.easybackup.sqlite.SqliteFileBackupCreator;
import ru.androidclass.easybackup.storage.InternalStorageFilesBackupCreator;
import ru.androidclass.easybackup.storage.StorageFilesBackupCreator;
import ru.androidclass.easybackupsample.db.DB;
import ru.androidclass.easybackupsample.db.entity.Lipsum;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private File spBackupFile;
    private File spRestoreFile;
    private File dpBackupFile;
    private File dpRestoreFile;
    private File ifBackupFile;
    private File ifRestoreFile;
    private static final String SPNAME = "prefs.xml";
    private static final int SELECT_FOLDER_CODE_FOR_BACKUP = 1001;
    private static final int SELECT_FOLDER_CODE_FOR_RESTORE = 1002;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.backupButton).setOnClickListener(
                v -> MainActivityPermissionsDispatcher.selectBackupFolderWithPermissionCheck(this));
        findViewById(R.id.restoreButton).setOnClickListener(
                v -> MainActivityPermissionsDispatcher.selectRestoreFolderWithPermissionCheck(this));
        findViewById(R.id.drive).setOnClickListener(view -> startActivity(new Intent(this, DriveBackupActivity.class)));

        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        preferences.edit().putString("test_key", String.valueOf(Calendar.getInstance().getTime())).apply();

        DB db = new DB(getApplication());
        List<Lipsum> lipsums = db.getDB().lipsumDao().getLipsums();
        Log.d(TAG, "lipsums " + lipsums);

        createFile(getFilesDir(), "textBackupRestore.txt", String.valueOf(Calendar.getInstance().getTime()));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void selectBackupFolder() {
        selectFolder(SELECT_FOLDER_CODE_FOR_BACKUP);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void selectRestoreFolder() {
        selectFolder(SELECT_FOLDER_CODE_FOR_RESTORE);
    }

    public void selectFolder(int code) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory for backup"), code);
    }

    //After selection of folder make backup or restore of files
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if ((requestCode == SELECT_FOLDER_CODE_FOR_BACKUP || requestCode == SELECT_FOLDER_CODE_FOR_RESTORE)
                && resultCode == Activity.RESULT_OK) {
            String path = getPath(this,
                    DocumentsContract.buildDocumentUriUsingTree(intent.getData(), DocumentsContract.getTreeDocumentId(intent.getData())));
            Log.d(TAG, "selected path=" + path);
            if (path != null) {
                if (requestCode == SELECT_FOLDER_CODE_FOR_BACKUP) {
                    spBackupFile = new File(path, SPNAME);
                    dpBackupFile = new File(path, DATABASE_NAME);
                    ifBackupFile = new File(path, "internalFiles.zip");
                    Log.d(TAG, "starting backup");
                    backup();
                }
                if (requestCode == SELECT_FOLDER_CODE_FOR_RESTORE) {
                    spRestoreFile = new File(path, SPNAME);
                    dpRestoreFile = new File(path, DATABASE_NAME);
                    ifRestoreFile = new File(path, "internalFiles.zip");
                    Log.d(TAG, "starting restore");
                    restore();
                }
            }
        }
    }

    private void backup() {
        if (spBackupFile != null && dpBackupFile != null && ifBackupFile != null) {
            try {
                Log.d(TAG, "starting backupAll");
                getBackupManager().backupAll();
            } catch (BackupInitializationException e) {
                e.printStackTrace();
                Toast.makeText(this, "Initialization Failed!", Toast.LENGTH_LONG).show();
            } catch (BackupException e) {
                e.printStackTrace();
                Toast.makeText(this, "Backup Failed!", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void restore() {
        if (spRestoreFile != null && dpRestoreFile != null && ifRestoreFile != null) {
            try {
                Log.d(TAG, "starting restoreAll");
                getBackupManager().restoreAll();
            } catch (BackupInitializationException e) {
                e.printStackTrace();
                Toast.makeText(this, "Initialization Failed!", Toast.LENGTH_LONG).show();
            } catch (RestoreException e) {
                e.printStackTrace();
                Toast.makeText(this, "Restore Failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private BackupManager getBackupManager() {
        BackupManager backupManager = new BackupManager();
        backupManager.addBackupCreator(new SharedPreferencesFileBackupCreator(getApplication(), getPackageName(), spBackupFile, spRestoreFile));
        backupManager.addBackupCreator(new SqliteFileBackupCreator(getApplication(), dpBackupFile, dpRestoreFile, DATABASE_NAME));
        backupManager.addBackupCreator(new InternalStorageFilesBackupCreator(getApplication(), ifBackupFile, ifRestoreFile, "./"));
        return backupManager;
    }


    public void createFile(File dir, String fileName, String body) {
        try {
            File textFile = new File(dir, fileName);
            FileWriter writer = new FileWriter(textFile);
            writer.append(body);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
