package ru.androidclass.backup.core;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    public void backup() throws BackupManagerBackupException {
        for (BackupCreator backupCreator : mBackupCreators) {
            backupCreator.create().backup();
        }
    }

    public void restore() throws BackupManagerRestoreException {
        for (BackupCreator backupCreator : mBackupCreators) {
            backupCreator.create().restore();
        }
    }

    public boolean isEmpty() {
        return mBackupCreators.isEmpty();
    }
}
