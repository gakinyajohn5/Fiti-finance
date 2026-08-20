package com.fitifinance.comrade.data

import androidx.room.TypeConverter
import com.fitifinance.comrade.data.entity.*

class Converters {
    @TypeConverter
    fun fromLivingSituation(v: LivingSituation): String = v.name
    @TypeConverter
    fun toLivingSituation(v: String): LivingSituation = LivingSituation.valueOf(v)

    @TypeConverter
    fun fromRoommateStatus(v: RoommateStatus): String = v.name
    @TypeConverter
    fun toRoommateStatus(v: String): RoommateStatus = RoommateStatus.valueOf(v)

    @TypeConverter
    fun fromMealStyle(v: MealStyle): String = v.name
    @TypeConverter
    fun toMealStyle(v: String): MealStyle = MealStyle.valueOf(v)

    @TypeConverter
    fun fromPartyStatus(v: PartyStatus): String = v.name
    @TypeConverter
    fun toPartyStatus(v: String): PartyStatus = PartyStatus.valueOf(v)

    @TypeConverter
    fun fromPartyFrequency(v: PartyFrequency): String = v.name
    @TypeConverter
    fun toPartyFrequency(v: String): PartyFrequency = PartyFrequency.valueOf(v)

    @TypeConverter
    fun fromTransactionCategory(v: TransactionCategory): String = v.name
    @TypeConverter
    fun toTransactionCategory(v: String): TransactionCategory = TransactionCategory.valueOf(v)

    @TypeConverter
    fun fromTransactionSource(v: TransactionSource): String = v.name
    @TypeConverter
    fun toTransactionSource(v: String): TransactionSource = TransactionSource.valueOf(v)

    @TypeConverter
    fun fromMealWindow(v: MealWindow): String = v.name
    @TypeConverter
    fun toMealWindow(v: String): MealWindow = MealWindow.valueOf(v)
}
