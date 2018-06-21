package ru.androidclass.easybackupsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;

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
    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView();
        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
    }

    public void selectFolderToBackup() {
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
