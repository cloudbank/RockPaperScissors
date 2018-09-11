package com.flatbuffer.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters

@Entity
class Cache {
    @PrimaryKey
    var id: Int = 0

    var ids: Int = 0
    @TypeConverters(Converters::class)
    var hashes: IntArray? = null
}
