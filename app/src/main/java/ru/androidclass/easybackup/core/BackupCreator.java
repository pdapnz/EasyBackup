package ru.androidclass.easybackup.core;

/**
 * Fabric class for creating implementations of {@link Backup}
 */
public interface BackupCreator {
    Backup create();
}
