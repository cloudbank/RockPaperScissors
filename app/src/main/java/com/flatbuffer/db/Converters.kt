package com.flatbuffer.db

import android.arch.persistence.room.TypeConverter

import com.google.gson.Gson


class Converters {

    @TypeConverter
    fun hashesToString(data: IntArray): String {
        return Gson().toJson(data)
    }


    @TypeConverter
    fun stringToHashes(data: String): IntArray {

        return Gson().fromJson(data, IntArray::class.java)
    }


}