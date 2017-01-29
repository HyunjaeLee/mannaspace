package com.hyunjae.mannaspace;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Post {

    private static final Pattern PATTERN_P = Pattern.compile("p: \"(.*?)\"");
    private static final Pattern PATTERN_K = Pattern.compile("k: \"(.*?)\"");

    private final Document document;
    private final JsonObject jsonObject;

    public Post(String url) throws Exception {
        document = CFConnection.connect(url).get();
        String json = document.select("script[type=\"application/ld+json\"]").first().data();
        json = json.replaceFirst("},", "}"); // Fix json syntax error
        JsonParser jsonParser = new JsonParser();
        jsonObject = jsonParser.parse(json).getAsJsonObject();
    }

    public List<String> getImages() throws Exception {
        String p, k;

        String html = document.outerHtml();

        Matcher matcherP = PATTERN_P.matcher(html);
        Matcher matcherK = PATTERN_K.matcher(html);
        if (matcherP.find() && matcherK.find()) {
            p = matcherP.group(1);
            k = matcherK.group(1);
        } else
            throw new Exception("No matches");

        String json = Crypto.decrypt(k, p);
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("content");
        Gson gson = new Gson();
        String imageArray[] = gson.fromJson(jsonArray, String[].class);
        return Arrays.asList(imageArray);
    }

    public String getTumbnail() {
        return jsonObject.get("thumbnailUrl").getAsString();
    }

    public String getName() {
        return jsonObject.get("name").getAsString();
    }

    public String getIssueNumber() {
        return jsonObject.get("issueNumber").getAsString();
    }

    public String getDatePublished() {
        return jsonObject.get("datePublished").getAsString();
    }

    public String getSeriesName() {
        return jsonObject.getAsJsonObject("isPartOf").get("name").getAsString();
    }

    public String getAuthor() {
        return jsonObject.getAsJsonObject("isPartOf").getAsJsonObject("author").get("name").getAsString();
    }

    @Override
    public String toString() {
        return getName();
    }
}
