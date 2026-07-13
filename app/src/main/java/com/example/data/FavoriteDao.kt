package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(channel: FavoriteChannel)

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteFavoriteByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE url = :url)")
    fun isFavorite(url: String): Flow<Boolean>
}
