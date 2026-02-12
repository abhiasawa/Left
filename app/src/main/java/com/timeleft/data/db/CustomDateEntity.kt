package com.timeleft.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timeleft.domain.models.CustomDate
import com.timeleft.domain.models.SymbolType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "custom_dates")
data class CustomDateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val startDate: Long, // epoch millis
    val endDate: Long,   // epoch millis
    val colorHex: String = "#FFFFFF",
    val symbolType: String = "DOT",
    val isCountUp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): CustomDate {
        return CustomDate(
            id = id,
            name = name,
            startDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate(),
            endDate = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate(),
            colorHex = colorHex,
            symbolType = SymbolType.fromString(symbolType),
            isCountUp = isCountUp,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(customDate: CustomDate): CustomDateEntity {
            return CustomDateEntity(
                id = customDate.id,
                name = customDate.name,
                startDate = customDate.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                endDate = customDate.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                colorHex = customDate.colorHex,
                symbolType = customDate.symbolType.name,
                isCountUp = customDate.isCountUp,
                createdAt = customDate.createdAt
            )
        }
    }
}
