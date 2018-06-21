package ru.androidclass.backup.core;

/**
 * Fabric class for creating implementations of {@link Backup}
 */
public interface BackupCreator {
    Backup create();
}
