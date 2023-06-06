package com.example.serverapp.pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.example.serverapp.MainActivity;
import com.example.serverapp.Utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ImageRequestHandler {

    private static final int IMAGES_PER_PAGE = 30;
    private Context context;

    public ImageRequestHandler(Context context) {
        this.context = context;
    }

    public NanoHTTPD.Response handleRequest(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/gallery")) {
            int pageNumber = getPageNumberFromUri(session.getParameters());
            Pair<List<String>, Integer> result = getImageUrls(pageNumber, session);
            List<String> imageUrls = result.first;
            int totalImages = result.second;
            String response = generateGalleryResponse(imageUrls, totalImages, pageNumber);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", response);
        } else if (uri.startsWith("/images/")) {
            String imagePath = uri.substring(8); // Remove '/images/' from the URI
            try {
                InputStream inputStream = new FileInputStream(imagePath);
                return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, getMimeType(imagePath), inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
            }
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
    }

    private Pair<List<String>, Integer> getImageUrls(int pageNumber, NanoHTTPD.IHTTPSession session) {
        List<String> imageUrls = new ArrayList<>();
        int totalImages = 0;

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            totalImages = cursor.getCount();
            int startIndex = (pageNumber - 1) * IMAGES_PER_PAGE;
            int endIndex = startIndex + IMAGES_PER_PAGE;

            if (cursor.moveToPosition(startIndex)) {
                while (!cursor.isAfterLast() && imageUrls.size() < IMAGES_PER_PAGE && imageUrls.size() < totalImages) {
                    @SuppressLint("Range")
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String imageUrl = "http://" + session.getHeaders().get("host") + "/images/" + Uri.encode(imagePath);
                    imageUrls.add(imageUrl);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        }

        return new Pair<>(imageUrls, totalImages);
    }

    private int getPageNumberFromUri(Map<String, List<String>> parameters) {
        int pageNumber = 1; // Default page number is 1

        if (parameters.containsKey("page")) {
            List<String> values = parameters.get("page");
            if (values != null && !values.isEmpty()) {
                try {
                    pageNumber = Integer.parseInt(values.get(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return pageNumber;
    }

    private String generateGalleryResponse(List<String> imageUrls, int totalImages, int currentPage) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(Utilities.getHtmlFromAssets(MainActivity.getInstance(), "GalleryPage.html"));

        responseBuilder.append("<div class=\"gallery\">");
        for (String imageUrl : imageUrls) {
            responseBuilder.append("  <div class=\"image\">");
            String imageTag = String.format("<img style = \"margin: 15px\" width=\"100\" height=\"100\" " +
                    "src='%s' onclick=\"onClick(this)\"><br></div>", imageUrl);
            responseBuilder.append(imageTag);
        }

        responseBuilder.append("</div>");
        responseBuilder.append(getPaginationButtons(totalImages, currentPage));
        responseBuilder.append("</body></SlideShow.html>");
        return responseBuilder.toString();
    }


    private String getPaginationButtons(int totalImages, int currentPage) {
        StringBuilder buttonBuilder = new StringBuilder();

        int totalPages = (int) Math.ceil((double) totalImages / IMAGES_PER_PAGE);

        if (totalPages > 1) {
            buttonBuilder.append("<div class=\"pagination-container\">");

            // Previous button
            if (currentPage > 1) {
                buttonBuilder.append("<div class=\"pagination-number arrow\">");
                buttonBuilder.append("<a href=\"/gallery?page=").append(currentPage - 1).append("\">Previous Page</a>");
                buttonBuilder.append("</div>");
            }

            // Current page
            buttonBuilder.append("<div class=\"pagination-number current\">[");
            buttonBuilder.append(currentPage);
            buttonBuilder.append("</div>");

            // Total pages
            buttonBuilder.append("<div class=\"pagination-number total\">");
            buttonBuilder.append("of ");
            buttonBuilder.append(totalPages);
            buttonBuilder.append("]</div>");

            // Next button
            if (currentPage < totalPages) {
                buttonBuilder.append("<div class=\"pagination-number arrow\">");
                buttonBuilder.append("<a href=\"/gallery?page=").append(currentPage + 1).append("\">Next Page</a>");
                buttonBuilder.append("</div>");
            }

            buttonBuilder.append("</div>");
        }

        return buttonBuilder.toString();
    }

    private String getMimeType(String filePath) {
        String mimeType = "application/octet-stream";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }
}
