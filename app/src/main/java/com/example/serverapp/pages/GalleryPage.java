package com.example.serverapp.pages;

import static com.example.serverapp.Utilities.fetchGalleryImages;
import static com.example.serverapp.Utilities.getHtmlFromAssets;
import static fi.iki.elonen.NanoHTTPD.MIME_HTML;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.example.serverapp.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class GalleryPage extends RouterNanoHTTPD.DefaultHandler {
    int n;
    int initialSize;

    public GalleryPage(int n) {
        this.n = n;
    }

    @Override
    public String getText() {
        StringBuilder answer = new StringBuilder();
        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "GalleryPage.html"));

        for (int i = n * 10; i < fetchGalleryImages(MainActivity.getInstance()).size(); i++) {
            if (i == (n * 10) + 12) {
                break;
            } else {
                File file = new File(fetchGalleryImages(MainActivity.getInstance()).get(i));
                System.out.println(file.getName());
                if (file.getName().contains(".png")) {
                    continue;
                }
                Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 20, baos); // bm is the bitmap object
                byte[] b = baos.toByteArray();
                answer.append("<div class=\"w3-third\">").append("<img style = \"margin: 50px\" width=\"150\" height=\"150\" src=\"data:image/png;base64, ")
                        .append(Base64.encodeToString(b, Base64.DEFAULT)).append("\"onclick=\"onClick(this)\" />\n").append("</div>");

            }
        }

        if (n * 10 == fetchGalleryImages(MainActivity.getInstance()).size()) {


        } else {

            answer.append(
                    // Back button////
                    //class="w3-dark-grey w3-xlarge w3-padding-32"
                    "<div class=\"w3-row-padding\">" +
                            "<div class=\"w3-half w3-margin-bottom\">" +
                            "<ul class=\"w3-ul w3-light-grey w3-center\">" +
                            "<li class=\"w3-dark-grey w3-xlarge w3-padding-32\" onclick=\"history.back()\"> Previous page </li>" +
                            "</ul>" +
                            "</div>");
            // Next button...
            answer.append(
                    "<div class=\"w3-half\">" +
                            "<ul class=\"w3-ul w3-light-grey w3-center\">" +
                            "<li class=\"w3-red w3-xlarge w3-padding-32\" onclick=\"location.href='/" + n + "';\"> Next Page </li>" +
                            "</ul>" +
                            "</div>" +
                            "</div>");
        }




//        for (String fetchGalleryImage : fetchGalleryImages(MainActivity.getInstance())) {
//            File file = new File(fetchGalleryImage);
//            System.out.println(file.getName());
//            if (file.getName().contains(".png")) {
//                continue;
//            }
//            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bm.compress(Bitmap.CompressFormat.JPEG, 20, baos); // bm is the bitmap object
//            byte[] b = baos.toByteArray();
//            image = Base64.encodeToString(b, Base64.DEFAULT);
//            System.out.println(image.length());
//            base64Img.add(image);
//        }
//        answer.append("<div class=\"w3-row\">");
//        for (int i = 0; i < base64Img.size(); i++) {
//            if (i % 3 == 0) {
//                answer.append("<br>");
//            }
//            answer.append("<div class=\"w3-third\">").append("<img style = \"margin: 50px\" width=\"300\" height=\"300\" src=\"data:image/png;base64, ").append(base64Img.get(i)).append("\"onclick=\"onClick(this)\" />\n").append("</div>");
//
//        }
//        answer.append("</div>");

        return answer.toString();
    }

    @Override
    public String getMimeType() {
        return MIME_HTML;
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }
}
