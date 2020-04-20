/*
 * Copyright (C) 2020 by olie.xdev@googlemail.com All Rights Reserved
 */

package com.health.openworkout.core.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.health.openworkout.core.datatypes.WorkoutItem;

import java.util.List;

@Dao
public interface WorkoutItemDAO {
    @Insert
    long insert(WorkoutItem workoutItem);

    @Update
    void update(WorkoutItem workoutItem);

    @Delete
    void delete(WorkoutItem workoutItem);

    @Query("SELECT * FROM WorkoutItem WHERE workoutSessionId = :workoutSessionId")
    List<WorkoutItem> getAll(long workoutSessionId);
}