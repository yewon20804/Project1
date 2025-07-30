package com.example.project1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.project1.data.ScheduleEntity

@Dao
interface ScheduleDao {

    @Insert
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules WHERE date = :date")
    suspend fun getSchedulesByDate(date: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedules(): List<ScheduleEntity>

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}