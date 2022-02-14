package ru.androidclass.easybackupsample.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Lipsum")
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

    public void setName(String name) {
        this.name = name;
    }

}
