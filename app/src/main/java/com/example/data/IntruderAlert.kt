package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "intruder_alerts", indices = [Index(value = ["timestamp"])])
data class IntruderAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val photoPath: String,
    val attemptedPackage: String?,
    val lockType: String
)
