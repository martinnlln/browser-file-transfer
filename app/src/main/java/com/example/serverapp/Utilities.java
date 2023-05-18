package com.example.serverapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public Utilities() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void renameFileWithSpaces(String path) {
        File root = new File(path);
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(" ")) {
                    @SuppressLint({"NewApi", "LocalSuppress"}) Path source = Paths.get(file.getAbsolutePath());
                    try {
                        if (!file.getName().contains(".txt")) {
                            Files.move(source, source.resolveSibling(file.getName().replace(' ', '-') + ".txt"));
                        } else {
                            Files.move(source, source.resolveSibling(file.getName().replace(' ', '-')));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public static String getHtmlFromAssets(Context context, String fileName) {
        InputStream is;
        String str;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return str;
    }

    public static ArrayList<String> fetchGalleryImages(Activity context) {
        ArrayList<String> galleryImageUrls;
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};//get all columns of type images
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;//order data by date
        Cursor imagecursor = context.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
        galleryImageUrls = new ArrayList<String>();
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);//get column index
            galleryImageUrls.add(imagecursor.getString(dataColumnIndex));//get Image from column index

        }

        return galleryImageUrls;
    }

    public static String getImageToBase64(String path, int quality) {
        String image = "";
        File file = new File(path);
        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, baos); // bm is the bitmap object
        byte[] b = baos.toByteArray();
        image = Base64.encodeToString(b, Base64.DEFAULT);

        return image;
    }


    public static String readFromFile(String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset());
        BufferedReader br = new BufferedReader(isr);

        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }
        br.close();

        return builder.toString();
    }

    public static String getRealFileName(String file) {
        File file1 = new File(file);

        return file1.getName();
    }

}
