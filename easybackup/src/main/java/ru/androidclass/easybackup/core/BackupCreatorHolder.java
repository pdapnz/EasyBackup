package ru.androidclass.easybackup.core;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;

class BackupCreatorHolder {

    private final List<BackupCreator> mBackupCreators;

    public BackupCreatorHolder() {
        mBackupCreators = new CopyOnWriteArrayList<>();
    }

    public void addJobCreator(BackupCreator creator) {
        mBackupCreators.add(creator);
    }

    public void removeJobCreator(BackupCreator creator) {
        mBackupCreators.remove(creator);
    }

    public void backup() throws BackupException, BackupInitializationException {
        for (BackupCreator backupCreator : mBackupCreators) {
            backupCreator.create().backup();
        }
    }

    public void restore() throws RestoreException, BackupInitializationException {
        for (BackupCreator backupCreator : mBackupCreators) {
            backupCreator.create().restore();
        }
    }

    public boolean isEmpty() {
        return mBackupCreators.isEmpty();
    }
}
