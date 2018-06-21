package ru.androidclass.backup.sharedpreferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import ru.androidclass.backup.core.Backup;
import ru.androidclass.backup.core.BackupManagerBackupException;
import ru.androidclass.backup.core.BackupManagerRestoreException;

/**
 * Class for backup and restore application's Shared Preference
 */
public class SharedPreferencesFileBackup implements Backup {
    private SharedPreferences mSharedPreferences;
    private File mBackupFile;
    private File mRestoreFile;

    public SharedPreferencesFileBackup(SharedPreferences sharedPreferences, File backupFile, File restoreFile) {
        mSharedPreferences = sharedPreferences;
        mBackupFile = backupFile;
        mRestoreFile = restoreFile;
    }

    @Override
    public void backup() throws BackupManagerBackupException {
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(mBackupFile));
            output.writeObject(mSharedPreferences.getAll());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw (new BackupManagerBackupException(e));
        } catch (IOException e) {
            e.printStackTrace();
            throw (new BackupManagerBackupException(e));
        }
        try {
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw (new BackupManagerBackupException(e));
        }
    }

    @SuppressWarnings({"unchecked", "UnnecessaryUnboxing"})
    @SuppressLint("ApplySharedPref")
    @Override
    public void restore() throws BackupManagerRestoreException {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(mRestoreFile));
            SharedPreferences.Editor prefEdit = mSharedPreferences.edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw (new BackupManagerRestoreException(e));
        } catch (IOException e) {
            e.printStackTrace();
            throw (new BackupManagerRestoreException(e));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw (new BackupManagerRestoreException(e));
        }

        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw (new BackupManagerRestoreException(e));
        }
    }
}
