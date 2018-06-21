package ru.androidclass.easybackupsample.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.androidclass.easybackupsample.db.entity.Lipsum;

@Dao
public interface LipsumDao {
    @Query("SELECT * FROM Lipsum")
    List<Lipsum> getLipsums();
}
