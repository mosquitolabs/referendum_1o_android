package com.referendum.uoctubre.utils;

import android.content.res.Resources;
import android.util.Log;
import android.util.Xml;

import com.crashlytics.android.Crashlytics;
import com.referendum.uoctubre.UOctubreApplication;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatException;

public class StringsManager {

    protected static final String TAG = StringsManager.class.getSimpleName();

    private static final Object lock = new Object();
    private static String currentLanguage;
    private static HashMap<String, CharSequence> mStrings;

    public static void initialize() {
        currentLanguage = SharedPreferencesUtils.getString(SharedPreferencesUtils.APP_LANGUAGE, "ca");
        mStrings = new HashMap<>();
        refreshLiterals();
    }

    public static String getString(String id, Object... args) {
        synchronized (lock) {
            if (mStrings.containsKey(id)) {
                try {
                    return String.format(mStrings.get(id).toString(), args);
                } catch (IllegalFormatException e) {
                    Log.e(TAG, "A string was found with wrong format parameters! The string is: " + id);
                    return mStrings.get(id).toString();
                }
            } else {
                Crashlytics.logException(new RuntimeException("String not found: " + id));
                return "";
            }
        }

    }

    private static void refreshLiterals() {
        synchronized (lock) {
            mStrings.clear();
            loadStringsFromResourceFile();
        }
    }

    private static void loadStringsFromResourceFile() {
        try {
            Resources res = UOctubreApplication.getInstance().getResources();
            InputStream in = res.getAssets().open("strings_" + currentLanguage + ".xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readStrings(parser);
            in.close();

        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readStrings(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "resources");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("string")) {
                readString(parser);
            } else {
                skip(parser);
            }
        }
    }

    private static void readString(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "string");
        String stringName = parser.getAttributeValue(null, "name");
        String value = getInnerXml(parser);
        parser.require(XmlPullParser.END_TAG, null, "string");
        mStrings.put(stringName, value.replace("\\n", "\n"));
    }

    private static String getInnerXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    if (depth > 0) {
                        sb.append("</").append(parser.getName()).append(">");
                    }
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    StringBuilder attrs = new StringBuilder();
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        attrs.append(parser.getAttributeName(i)).append("=\"").append(parser.getAttributeValue(i)).append("\" ");
                    }
                    sb.append("<").append(parser.getName()).append(" ").append(attrs.toString()).append(">");
                    break;
                default:
                    sb.append(parser.getText());
                    break;
            }
        }
        return sb.toString();
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(String language) {
        SharedPreferencesUtils.setString(SharedPreferencesUtils.APP_LANGUAGE, language);
        currentLanguage = language;
        refreshLiterals();
    }
}