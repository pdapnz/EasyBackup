package ru.androidclass.easybackupsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.sharedpreferences.SharedPreferencesFileBackupCreator;
import ru.androidclass.easybackup.sqlite.SqliteFileBackupCreator;

/**
 * Created by Dmitry Polozov <pdapnz@ya.ru> on 21.06.2018.
 */
public class MainActivity extends AppCompatActivity {

    private File spBackupFile;
    private File spRestoreFile;
    private File dpBackupFile;
    private File dpRestoreFile;
    private static final String DBNAME = "testdb.db";
    private static final String SPNAME = "prefs.xml";
    private static final int SELECT_FOLDER_CODE_FOR_BACKUP = 1001;
    private static final int SELECT_FOLDER_CODE_FOR_RESTORE = 1002;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView();
        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
    }

    public void selectFolder(int code) {
        Intent i = new Intent(this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, code);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode == SELECT_FOLDER_CODE_FOR_BACKUP || requestCode == SELECT_FOLDER_CODE_FOR_RESTORE)
                && resultCode == Activity.RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            if (!files.isEmpty()) {
                File file = Utils.getFileForUri(files.get(0));
                if (requestCode == SELECT_FOLDER_CODE_FOR_BACKUP) {
                    spBackupFile = new File(file, SPNAME);
                    dpBackupFile = new File(file, DBNAME);
                    backup();
                }
                if (requestCode == SELECT_FOLDER_CODE_FOR_RESTORE) {
                    spRestoreFile = new File(file, SPNAME);
                    dpRestoreFile = new File(file, DBNAME);
                    restore();
                }
            }
        }
    }

    public void backup() {
        if (spBackupFile != null && dpBackupFile != null) {
            try {
                getBackupManager().backupAll();
            } catch (BackupException e) {
                e.printStackTrace();
                Toast.makeText(this, "Backup Failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void restore() {
        if (spRestoreFile != null && dpRestoreFile != null) {
            try {
                getBackupManager().restoreAll();
            } catch (RestoreException e) {
                e.printStackTrace();
                Toast.makeText(this, "Restore Failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private BackupManager getBackupManager() {
        BackupManager backupManager = new BackupManager();
        backupManager.addBackupCreator(new SharedPreferencesFileBackupCreator(preferences, spBackupFile, spRestoreFile));
        backupManager.addBackupCreator(new SqliteFileBackupCreator(getApplication(), dpBackupFile, dpRestoreFile, DBNAME));
        return backupManager;
    }
}
