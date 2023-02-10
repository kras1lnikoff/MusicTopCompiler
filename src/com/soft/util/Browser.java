package com.soft.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class Browser {

    public static Document connect(String url) {
        return connect(url, 3);
    }

    private static Document connect(String url, int count) {
        try {
            return Jsoup.connect(url).userAgent("Opera").timeout(10000).get();
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException && count > 0) return connect(url, count - 1);
            System.err.println("An error occurred while connecting to \"" + url + "\"");
        }
        return Jsoup.parse("");
    }

    public static boolean exists(String url) {
        return transform(url) != null;
    }

    public static String transform(String url) {
        try {
            HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
            huc.setRequestMethod("HEAD");
            return huc.getResponseCode() == HttpURLConnection.HTTP_OK ? huc.getURL().toString() : null;
        } catch (IOException e) {
            System.err.println("An error occurred while transforming \"" + url + "\"");
            return null;
        }
    }

    public static void browse(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("An error occurred while browsing \"" + url + "\"");
        }
    }

    public static String decode(String url) {
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }

    public static String encode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String cutTitle(Document document, String end) {
        String line = document.title();
        int index = line.length() - end.length();
        return index >= 0 ? line.substring(0, index).strip() : null;
    }
}