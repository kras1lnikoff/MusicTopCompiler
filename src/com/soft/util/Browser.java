package com.soft.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class Browser {

    public static Document connect(String url) {
        return connect(url, 5);
    }

    private static Document connect(String url, int count) {
        try {
            return Jsoup.connect(url).userAgent("Opera").timeout(10000).get();
        } catch (SocketTimeoutException e) {
            if (count > 0) return connect(url, count - 1);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("An error occurred while connecting to \"" + url + "\"...");
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
            return null;
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

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("MMMM d, u", Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            try {
                return YearMonth.parse(text, DateTimeFormatter.ofPattern("MMMM u", Locale.ENGLISH)).atDay(1);
            } catch (DateTimeParseException ex) {
                return Year.parse(text).atDay(1);
            }
        }
    }
}
