package com.example.data

import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val favoriteDao: FavoriteDao) {
    val allFavorites: Flow<List<FavoriteChannel>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(channel: FavoriteChannel) {
        favoriteDao.insertFavorite(channel)
    }

    suspend fun removeFavoriteByUrl(url: String) {
        favoriteDao.deleteFavoriteByUrl(url)
    }

    fun isFavorite(url: String): Flow<Boolean> {
        return favoriteDao.isFavorite(url)
    }
}
