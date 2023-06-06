package com.example.serverapp.pages;

import static com.example.serverapp.Utilities.getHtmlFromAssets;
import static com.example.serverapp.Utilities.getImageToBase64;
import static com.example.serverapp.Utilities.getMimeType;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

import com.example.serverapp.MainActivity;
import com.example.serverapp.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class DirectoriesRequestHandler {

    private static final int FILES_PER_PAGE = 20;
    private Context context;

    public DirectoriesRequestHandler(Context context) {
        this.context = context;
    }

    public NanoHTTPD.Response handleRequest(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/directories")) {
            int pageNumber = getPageNumberFromUri(session.getParameters());
            File file = new File(Environment.getExternalStorageDirectory().toURI());
            Pair<List<String>, Integer> result = getDirectoryContents(file, pageNumber, session);
            List<String> directoryUrls = result.first;
            int totalDirectories = result.second;
            String response = generateDirectoriesResponse(directoryUrls, totalDirectories, pageNumber);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", response);
        } else if (uri.startsWith("/storage/")) {
            String filePath = uri.substring(9); // Remove '/files/' from the URI
            File file = new File(uri);
            if (file.exists()) {
                if (file.isDirectory()) {
                    // Handle directory click
                    int pageNumber = getPageNumberFromUri(session.getParameters());
                    Pair<List<String>, Integer> result = getDirectoryContents(file, pageNumber, session);
                    List<String> fileUrls = result.first;
                    int totalFiles = result.second;
                    String response = generateDirectoriesResponse(fileUrls, totalFiles, pageNumber);

                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", response);
                } else {
                    // Handle file click
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, getMimeType(filePath), fileInputStream, file.length());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "File Not Found");
                    }
                }
            }
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
    }

    private Pair<List<String>, Integer> getDirectoryContents(File directory, int pageNumber, NanoHTTPD.IHTTPSession session) {
        List<String> fileUrls = new ArrayList<>();
        int totalFiles = 0;

        File[] files = directory.listFiles();
        if (files != null) {
            totalFiles = files.length;
            int startIndex = (pageNumber - 1) * FILES_PER_PAGE;
            int endIndex = Math.min(startIndex + FILES_PER_PAGE, files.length);

            for (int i = startIndex; i < endIndex; i++) {
                File file = files[i];
                fileUrls.add(file.getAbsolutePath());
            }
        }
        return new Pair<>(fileUrls, totalFiles);
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

    private String generateDirectoriesResponse(List<String> fileUrls, int totalFiles, int currentPage) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(Utilities.getHtmlFromAssets(MainActivity.getInstance(), "GalleryPage.html"));

        responseBuilder.append("<div class=\"gallery\">");
        for (String fileUrl : fileUrls) {
            responseBuilder.append("  <div class=\"item\">");
            if (isDirectory(fileUrl)) {
                responseBuilder.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64FolderIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append((fileUrl)).append("> ").append(getDirectoryName(fileUrl)).append("</a>" + "    </div>");

            } else if (fileUrl.contains(".txt")) {
                responseBuilder.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64TextFileIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append(fileUrl).append("> ").append(getDirectoryName(fileUrl)).append("</a>" + "    </div>");

            } else if (getDirectoryName(fileUrl).contains(".png") || getDirectoryName(fileUrl).contains(".jpg")
                    || getDirectoryName(fileUrl).contains(".jpeg")) {
                responseBuilder.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">" +
                        "<a href=").append((fileUrl)).append("> ").append("</a>" + "    </div>");
                responseBuilder.append("<p>").append(subStringLongName(getFileName(fileUrl))).append("</p>");
                String imageTag = String.format("<img style = \"margin: 15px\" width=\"100\" height=\"100\" src='%s' onclick=\"onClick(this)\"></img>" +
                        "<br></div>", fileUrl);
                responseBuilder.append(imageTag);
            } else {
                responseBuilder.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64TextFileIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append((fileUrl)).append("> ").append(getDirectoryName(fileUrl)).append("</a></div>");

            }
        }

        responseBuilder.append("</div>");
        responseBuilder.append(getPaginationButtons(totalFiles, currentPage));
        responseBuilder.append("</body></html>");
        return responseBuilder.toString();
    }

    private String getPaginationButtons(int totalFiles, int currentPage) {
        StringBuilder buttonBuilder = new StringBuilder();

        int totalPages = (int) Math.ceil((double) totalFiles / FILES_PER_PAGE);

        if (totalPages > 1) {
            buttonBuilder.append("<div class=\"pagination-container\">");

            // Previous button
            if (currentPage > 1) {
                buttonBuilder.append("<div class=\"pagination-number arrow\">");
                buttonBuilder.append("<a href=\"/directories?page=").append(currentPage - 1).append("\">Previous Page</a>");
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
                buttonBuilder.append("<a href=\"/directories?page=").append(currentPage + 1).append("\">Next Page</a>");
                buttonBuilder.append("</div>");
            }

            buttonBuilder.append("</div>");
        }

        return buttonBuilder.toString();
    }

    private boolean isDirectory(String fileUrl) {
        File file = new File(fileUrl);
        return file.isDirectory();
    }

    private String getDirectoryName(String fileUrl) {
        File file = new File(fileUrl);
        return file.getName();
    }

    private String getFileName(String fileUrl) {
        File file = new File(fileUrl);
        return file.getName();
    }

    private String subStringLongName(String fileUrl) {
        if (fileUrl.length() > 8) {
            fileUrl = getDirectoryName(fileUrl).substring(0, 7) + getMimeType(fileUrl);
        } else {
            fileUrl = getDirectoryName(fileUrl);
        }
        return fileUrl;
    }
}
