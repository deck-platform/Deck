package com.bupt.kdapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StartInfoDao {
    @Query("Select * From startinfo")
    List<StartInfo> getAll();

    @Insert
    void insertAll(StartInfo startInfo);

    @Delete
    void delete(StartInfo startInfo);
}
