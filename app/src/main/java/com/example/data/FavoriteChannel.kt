package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteChannel(
    @PrimaryKey val url: String,
    val name: String,
    val category: String,
    val logoUrl: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
