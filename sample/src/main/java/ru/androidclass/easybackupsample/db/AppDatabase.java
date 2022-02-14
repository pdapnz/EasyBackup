package ru.androidclass.easybackupsample.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.androidclass.easybackupsample.db.dao.LipsumDao;
import ru.androidclass.easybackupsample.db.entity.Lipsum;

@Database(entities = {Lipsum.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "test.db";

    public abstract LipsumDao lipsumDao();

}