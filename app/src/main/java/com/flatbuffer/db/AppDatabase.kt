package com.flatbuffer.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase


@Database(entities = arrayOf(Cache::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

