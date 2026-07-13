package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomChannelDao {
    @Query("SELECT * FROM custom_channels ORDER BY addedAt DESC")
    fun getAllCustomChannels(): Flow<List<CustomChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomChannel(channel: CustomChannel)

    @Query("DELETE FROM custom_channels WHERE url = :url")
    suspend fun deleteCustomChannelByUrl(url: String)
}
