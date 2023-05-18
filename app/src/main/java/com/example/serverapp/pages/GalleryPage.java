package com.example.serverapp.pages;

import static com.example.serverapp.Utilities.fetchGalleryImages;
import static com.example.serverapp.Utilities.getHtmlFromAssets;
import static fi.iki.elonen.NanoHTTPD.MIME_HTML;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.serverapp.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class GalleryPage extends RouterNanoHTTPD.DefaultHandler {
    @Override
    public String getText() {
        StringBuilder answer = new StringBuilder();
        String image = "";
        List<String> base64Img = new ArrayList<>();

        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "GalleryPage.html"));
        for (String fetchGalleryImage : fetchGalleryImages(MainActivity.getInstance())) {
            File file = new File(fetchGalleryImage);
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 20, baos); // bm is the bitmap object
            byte[] b = baos.toByteArray();
            image = Base64.encodeToString(b, Base64.DEFAULT);
            System.out.println(image.length());
            base64Img.add(image);
        }
        answer.append("<div class=\"w3-row\">");
        for (int i = 0; i < base64Img.size(); i++) {
            if (i % 3 == 0) {
                answer.append("<br>");
            }
            answer.append("<div class=\"w3-third\">").append("<img style = \"margin: 50px\" width=\"300\" height=\"300\" src=\"data:image/png;base64, ").append(base64Img.get(i)).append("\"onclick=\"onClick(this)\" />\n").append("</div>");

        }
        answer.append("</div>");
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
