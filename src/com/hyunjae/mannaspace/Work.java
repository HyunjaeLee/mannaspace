package com.hyunjae.mannaspace;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

public class Work {

    private final Elements items;
    private final JsonObject jsonObject;

    public Work(String url) throws Exception {
        Document document = CFConnection.connect(url).get();
        items = document.select(".item");
        String json = document.select("script[type=\"application/ld+json\"]").first().data();
        JsonParser jsonParser = new JsonParser();
        jsonObject = jsonParser.parse(json).getAsJsonObject();
    }

    public String getName() {
        return jsonObject.get("name").getAsString();
    }

    public String getAuthor() {
        return jsonObject.getAsJsonObject("author").get("name").getAsString();
    }

    public String getThumbnail() {
        return jsonObject.get("thumbnailUrl").getAsString();
    }

    public List<String> getThumbnails() {
        return items.stream()
                .map(item -> item.select("img[data-src]").first().attr("data-src"))
                .collect(Collectors.toList());
    }

    public List<String> getDates() {
        return items.stream()
                .map(item -> item.select(".date").first().text())
                .collect(Collectors.toList());
    }

    public List<String> getIssueNumbers() {
        return items.stream()
                .map(item -> item.select(".classification").first().text())
                .collect(Collectors.toList());
    }

    public List<String> getUrls() {
        return items.stream()
                .map(item -> item.select("a[href]").first().attr("abs:href"))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getName();
    }
}
