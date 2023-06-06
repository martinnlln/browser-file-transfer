package com.example.serverapp.pages;

import static com.example.serverapp.Utilities.getHtmlFromAssets;
import static com.example.serverapp.Utilities.getImageToBase64;
import static com.example.serverapp.Utilities.getRealFileName;
import static com.example.serverapp.Utilities.readFromFile;

import com.example.serverapp.HTMLSCRIPTS;
import com.example.serverapp.MainActivity;

import java.io.File;
import java.io.IOException;

public class BrowsePhoneDirectories {
    public BrowsePhoneDirectories() {
    }
    public String getTextForImages(String fileName) {
        StringBuilder answer = new StringBuilder();
        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "ImagePopUp.html"));
        answer.append("<div class=\"popup\">" +
                        "<button  onclick=\"history.back()\" id=\"close\">&times;</button>\n")
                .append("<div class=\"w3-quarter\">" + "<img  src=\"data:image/png;base64,")
                .append((getImageToBase64(fileName, 80)))
                .append("\" style=\"width:70%\" onclick=\"onClick(this)\">")
                .append("<a download=\"" +
                        getRealFileName(fileName) +
                        "\" href=\"" +
                        "data:application/octet-stream;base64," +
                        getImageToBase64(fileName, 100) +
                        "\">download image</a>\n")
                .append("</div>");

        answer.append(HTMLSCRIPTS.indexHtmlLocations);
        return answer.toString();
    }

    public String getTextForTextFiles(String fileName) {
        StringBuilder answer = new StringBuilder();
        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "EditTextFile.html"));
        answer.append("<button> Save </button>");

        answer.append(
                "<textarea  style=\"font-family: Arial;font-size: 12pt;width:80%;height:90vw\">");
        try {
            answer.append(readFromFile(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        answer.append("</textarea>");
        answer.append("</div>");
        answer.append(HTMLSCRIPTS.indexHtmlLocations);
        return answer.toString();
    }

    public String getText(String paths) {
        StringBuilder answer = new StringBuilder();
        String path = String.valueOf(paths);
        File root = new File(path);
        File[] files = root.listFiles();

        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "index.html"));

        if (files == null || files.length < 1) {
            answer = new StringBuilder();
            answer.append(getHtmlFromAssets(MainActivity.getInstance(), "EmptyPage.html"));
            return answer.toString();
        }

        for (int i = 0; i < files.length; i++) {
            if (i % 4 == 0) {
                answer.append("<br>");
            }
            if (files[i].isDirectory()) {

                answer.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64FolderIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append(files[i].getAbsolutePath()).append("> ").append(files[i].getName()).append("</a>" + "    </div>");
            } else if (files[i].getName().contains(".txt")) {
                answer.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64TextFileIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append(files[i].getAbsolutePath()).append("> ").append(files[i].getName()).append("</a>" + "    </div>");

            } else if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
                    || files[i].getName().contains(".jpeg")) {
                answer.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64," + "")
                        .append(getImageToBase64(files[i].getAbsolutePath(), 15))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append(files[i].getAbsolutePath()).append("> ").append(files[i].getName()).append("</a>" + "    </div>");

            } else {
                answer.append("<div style=\"margin-bottom:30px;\" class=\"w3-quarter\">\n" + "<img  src=\"data:image/png;base64,")
                        .append(getHtmlFromAssets(MainActivity.getInstance(), "Base64TextFileIcon.txt"))
                        .append("\" style=\"width:100px; height: 100px;\" onclick=\"onClick(this)\">")
                        .append("<a href=").append(files[i].getAbsolutePath()).append("> ").append(files[i].getName()).append("</a></div>");

            }
        }
        answer.append("</div>");
        answer.append(HTMLSCRIPTS.indexHtmlLocations);
        return answer.toString();
    }

}
