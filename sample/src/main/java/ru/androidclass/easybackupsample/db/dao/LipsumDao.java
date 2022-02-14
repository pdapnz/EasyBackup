package ru.androidclass.easybackupsample.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ru.androidclass.easybackupsample.db.entity.Lipsum;

@Dao
public interface LipsumDao {
    @Query("SELECT * FROM Lipsum")
    List<Lipsum> getLipsums();
}
