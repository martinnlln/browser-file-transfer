package com.example.serverapp.pages;

import com.example.serverapp.MainActivity;
import com.example.serverapp.Utilities;

public class IndexPage {

    public IndexPage() {
    }

    public String get() {
        return (Utilities.getHtmlFromAssets(MainActivity.getInstance(), "index.html"));
    }
}
