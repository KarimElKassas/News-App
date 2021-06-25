package com.elkassas.newsmvvm.room

import androidx.room.TypeConverter
import com.elkassas.newsmvvm.data.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }

}