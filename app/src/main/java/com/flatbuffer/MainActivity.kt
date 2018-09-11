/*
 *    Copyright (C) 2016 Amit Shekhar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.flatbuffer

import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.flatbuffer.db.AppDatabase
import com.flatbuffer.db.Cache
import com.flatbuffer.flatmodel.FBCache
import com.flatbuffer.utils.Utils
import com.google.flatbuffers.FlatBufferBuilder
import com.google.gson.Gson
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {


    private var spIds: SharedPreferences? = null
    private var prefs: Preferences? = null
    internal var fbLength: Int = 0
    lateinit var db: AppDatabase
    val counterContext = newSingleThreadContext("CounterContext")
    val TAG = "MainActivity"

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var ids = 0
        val hashes = IntArray(3000)
        setContentView(R.layout.activity_main)
        val r = Random()
        for (i in 0..2999) {

            hashes[i] = i + r.nextInt()

        }
        for (i in 0..31) {
            ids = ids or (1 shl i)
        }
        asserts(ids, hashes)
        db = Room.databaseBuilder<AppDatabase>(applicationContext,
                AppDatabase::class.java, "database-name").build()
        spIds = this.getSharedPreferences("ids", Context.MODE_PRIVATE)
        prefs = BinaryPreferencesBuilder(application)
                .name("user_data")
                .build()

        launch(counterContext) { saveData(ids, hashes) }

    }


    private fun asserts(ids: Int, hashes: IntArray?) {
        if (ids and (1 shl 30) != 1 shl 30) throw AssertionError()
        if (hashes?.size != 3000) throw AssertionError()
        if (hashes?.get(2999) == 0) throw AssertionError()
        //Log.d("Activity","asserts done");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun saveData(ids: Int, hashes: IntArray) {
        saveGsonToSp(ids, hashes)
        saveGsonToBp(ids, hashes)
        Paper.book().write("ids", ids)
        Paper.book().write("hashes", hashes)
        saveToFlatBuffer(ids, hashes)
        saveToRoom(ids, hashes)

    }

    private fun saveGsonToSp(ids: Int, hashes: IntArray) {
        val ed = spIds!!.edit()
        ed.putInt("ids", ids)
        val string = Gson().toJson(hashes)

        ed.putString("hashes", string)
        ed.commit()
    }

    private fun saveGsonToBp(ids: Int, hashes: IntArray) {
        val ed = prefs!!.edit()
        ed.putInt("ids", ids)
        val string = Gson().toJson(hashes)

        ed.putString("hashes", string)
        ed.commit()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun saveToRoom(ids: Int, hashes: IntArray) {

        val c = Cache()
        c.ids = ids
        c.hashes = hashes
        db.cacheDao().insertCache(c)


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun saveToFlatBuffer(ids: Int, hashes: IntArray) {
        var fbb: FlatBufferBuilder? = FlatBufferBuilder(12032)

        val h = FBCache.createHashesVector(fbb!!, hashes)

        FBCache.startFBCache(fbb)
        FBCache.addHashes(fbb, h)

        FBCache.addIds(fbb, ids)
        val fud = FBCache.endFBCache(fbb)
        fbb.finish(fud)

        //java.nio.ByteBuffer buf = fbb.dataBuffer();
        val data = fbb.sizedByteArray()
        fbLength = data.size
        //write raw file binary mode to raw
        Utils.writeBin(application, data, "hashes")
        fbb = null

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun loadFromFlatBuffer(v: View) {
        val startTime = System.currentTimeMillis()
        launch(counterContext) {
            //final long[] avg = {0};
            //Again, make sure you read the bytes in BINARY mode, otherwise the code below won't work
            val paperAvg: Long = 0
            //for (int i = 1; i < 6; i++) {
            // for (int i = 0; i < 5; i++) {
            var bytes = ByteArray(fbLength)
            try {
                bytes = Utils.readBin(application, "hashes")
            } catch (e: IOException) {
                Log.e("Activity", "Exception in loadFB$e")
            }

            var buf: java.nio.ByteBuffer? = java.nio.ByteBuffer.wrap(bytes)
            val fud = FBCache.getRootAsFBCache(buf)
            val ids = fud.ids()
            val hashes = IntArray(fud.hashesLength())
            for (j in 0 until fud.hashesLength()) {
                hashes[j] = fud.hashes(j)
            }
            buf = null
            asserts(ids, hashes)
            val timeTaken = System.currentTimeMillis() - startTime
            //paperAvg = (paperAvg + timeTaken) / i;

            //long finalPaperAvg = paperAvg;
            //avg[0] += timeTaken;
            // }
            // avg[0] = avg[0] / 5;


            //return timeTaken[0];
            runOnUiThread {
                val logText = "FB : " + timeTaken + "ms"
                textViewFB.text = logText
                Log.d(TAG, "loadFromFB$logText")
            }
        }
    }


    fun loadFromPaper(v: View) {
        val startTime = System.currentTimeMillis()
        //final long[] avg = {0};

        launch(counterContext) {
            val paperAvg: Long = 0
            //for (int i = 1; i <=60; i++) {
            val ids = Paper.book().read<Int>("ids")
            val hashes = Paper.book().read<IntArray>("hashes")
            asserts(ids, hashes)
            val timeTaken = System.currentTimeMillis() - startTime
            //paperAvg += timeTaken;
            //Log.d(TAG, "paper avg" + timeTaken+ ":::"+ i+ ":::" + paperAvg);
            //avg[0] += timeTaken;
            //}
            // avg[0] = avg[0] / 5;
            //}
            //long finalPaperAvg = paperAvg;
            runOnUiThread {

                val logText = "Paper : " + timeTaken + "ms"
                textViewPaper.text = logText
                Log.d(TAG, "loadFromPaper$logText")
            }
        }
    }

    fun loadFromSP(v: View) {
        val startTime = System.currentTimeMillis()
        // final long[] avg = {0};
        launch(counterContext) {
            //for (int i = 0; i < 5; i++) {
            val ids = spIds!!.getInt("ids", 0)
            val hashesList = spIds!!.getString("hashes", "")
            val hashes = Gson().fromJson<IntArray>(hashesList, IntArray::class.java!!)
            asserts(ids, hashes)
            val timeTaken = System.currentTimeMillis() - startTime
            //avg[0] += timeTaken;
            // }
            // avg[0] = avg[0] / 5;
            runOnUiThread {

                val logText = "SP : " + timeTaken + "ms"
                textViewSP.text = logText
                Log.d(TAG, "loadFromSP $logText")
            }
        }
    }

    fun loadFromBP(v: View) {
        val startTime = System.currentTimeMillis()
        // final long[] avg = {0};
        launch(counterContext) {
            //for (int i = 0; i < 5; i++) {
            val ids = prefs!!.getInt("ids", 0)
            val hashesList = prefs!!.getString("hashes", "")
            val hashes = Gson().fromJson<IntArray>(hashesList, IntArray::class.java!!)
            asserts(ids, hashes)
            val timeTaken = System.currentTimeMillis() - startTime
            //avg[0] += timeTaken;
            // }
            // avg[0] = avg[0] / 5;
            runOnUiThread {

                val logText = "BP : " + timeTaken + "ms"
                textViewBP.text = logText
                Log.d(TAG, "loadFromBP $logText")
            }
        }
    }


    fun loadFromRoom(v: View) {

        val startTime = System.currentTimeMillis()
        //final long[] avg = {0};
        launch(counterContext) {
            // for (int i = 0; i < 5; i++) {
            val c = db.cacheDao().loadCache()
            val ids = c.ids

            val hashes = c.hashes
            asserts(ids, hashes)
            val timeTaken = System.currentTimeMillis() - startTime
            // avg[0] += timeTaken;
            //}
            //avg[0] = avg[0] / 5;
            runOnUiThread {

                val logText = "Room : " + timeTaken + "ms"
                textViewRoom!!.text = logText
                Log.d(TAG, "loadFromRoom $logText")
            }
        }

    }


}



