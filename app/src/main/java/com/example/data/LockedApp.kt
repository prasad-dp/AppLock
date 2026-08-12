package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)
