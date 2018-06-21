package ru.androidclass.easybackupsample.db;

import android.app.Application;
import android.arch.persistence.room.Room;

import static ru.androidclass.easybackupsample.db.AppDatabase.DATABASE_NAME;


public class DB {
    private AppDatabase mAppDataBase;

    public DB(Application application) {
        mAppDataBase = Room.databaseBuilder(application,
                AppDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
    }

    public AppDatabase getDB() {
        return mAppDataBase;
    }

}
