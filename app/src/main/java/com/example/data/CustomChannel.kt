package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_channels")
data class CustomChannel(
    @PrimaryKey val url: String,
    val name: String,
    val category: String,
    val logoUrl: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
