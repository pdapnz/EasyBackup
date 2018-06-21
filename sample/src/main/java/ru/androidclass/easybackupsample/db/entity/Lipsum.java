package ru.androidclass.easybackupsample.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "Lipsum", indices = {
        @Index(value = "lipsum")
})
public class Lipsum {
    @PrimaryKey
    private int Id;
    private String name;

    public Lipsum() {

    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setNames(String name) {
        this.name = name;
    }

}
