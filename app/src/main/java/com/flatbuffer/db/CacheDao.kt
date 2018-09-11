package com.flatbuffer.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCache(c: Cache)


    @Query("SELECT * FROM Cache")
    fun loadCache(): Cache


}




