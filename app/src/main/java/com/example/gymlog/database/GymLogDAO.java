package com.example.gymlog.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gymlog.database.entities.GymLog;

import java.util.List;

//DAO = data access obj, represents queries run in db
@Dao
public interface GymLogDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GymLog gymlog);


    //TODO: @update

    @Query("Select * from " + GymLogDatabase.GYM_LOG_TABLE)
    List<GymLog> getAllRecords();

}
