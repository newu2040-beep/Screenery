package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecordingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY dateAdded DESC")
    fun getAllRecordings(): Flow<List<RecordingItem>>

    @Query("SELECT * FROM recordings ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentRecordings(limit: Int = 5): Flow<List<RecordingItem>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): RecordingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(item: RecordingItem): Long

    @Update
    suspend fun updateRecording(item: RecordingItem)

    @Delete
    suspend fun deleteRecording(item: RecordingItem)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE recordings SET title = :newTitle WHERE id = :id")
    suspend fun renameRecording(id: Long, newTitle: String)
}
