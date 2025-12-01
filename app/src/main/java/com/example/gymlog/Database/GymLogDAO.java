package com.example.gymlog.Database;


import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gymlog.Database.entities.GymLog;

import java.util.ArrayList;

//DAO = data access obj, represents queries run in db
public interface GymLogDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GymLog gymlog);


    //TODO: @update

    @Query("Select * from " + GymLogDatabase.GYM_LOG_TABLE)
    ArrayList<GymLog> getAllRecords();

}
