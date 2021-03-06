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

package com.flatbuffer.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by amitshekhar on 17/04/16.
 */
public class Utils {

    public static byte[] readRawResource(Context context, int resId) {
        InputStream stream = null;
        byte[] buffer = null;
        try {
            stream = context.getResources().openRawResource(resId);
            buffer = new byte[stream.available()];
            while (stream.read(buffer) != -1) ;
        } catch (IOException e) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return buffer;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] readBin(Context ctx, String filename) throws IOException {
        File filepath = new File(ctx.getFilesDir(), filename);
        Path path = Paths.get(filepath.getPath());
        return Files.readAllBytes(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void writeBin(Context ctx, byte[] file, String filename) {

        File filepath = new File(ctx.getFilesDir(), filename);


        try {
            Files.write(filepath.toPath(), file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}