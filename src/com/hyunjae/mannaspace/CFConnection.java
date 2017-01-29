package com.hyunjae.mannaspace;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFConnection {

    private static final Logger logger = LoggerFactory.getLogger(CFConnection.class);

    private static final Pattern PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{vars,t,o,p,b,r,e,a,k,i,n,g,f,(.+?;).+?f=document.getElementById\\('challenge-form'\\);(.+?;)a.value=parseInt\\((.+?),.+?\\).+?},([0-9]+?)\\);");
    private static final String DOMAIN = "manaa.space";
    private static final int DOMAIN_LENGTH = DOMAIN.length();
    private static final String CLOUDFLARE_URL = "https://manaa.space/cdn-cgi/l/chk_jschl";
    private static final String USERAGENT = "Mozilla/5.0";
    private static final String COOKIES_JSON = "cookies.json";

    private static Map<String, String> cookies = loadCookies();

    private final Connection connection;

    private CFConnection(String url) {
        connection = Jsoup.connect(url);
        connection.ignoreHttpErrors(true);
        connection.userAgent(USERAGENT);
        connection.cookies(cookies);
    }

    public static CFConnection connect(String url) {
        return new CFConnection(url);
    }

    public CFConnection data(Map<String, String> map) {
        connection.data(map);
        return this;
    }

    public CFConnection data(String... strings) {
        connection.data(strings);
        return this;
    }

    public Document get() throws Exception {
        connection.method(Connection.Method.GET);
        Connection.Response response = connection.execute();
        int statusCode = response.statusCode();
        Document document = response.parse();
        if (statusCode == 503) { // Cloudflare DDOS protection
           setCloudflareCookies(document);
           return connection.get();
        } else {
            return document;
        }
    }

    private void setCloudflareCookies(Document response) throws Exception {
        Map<String, String> cloudflareCookies = cloudflare(response);
        cookies = cloudflareCookies;
        connection.cookies(cloudflareCookies);
        saveCookies(cloudflareCookies);
    }

    private static void saveCookies(Map<String, String> cookies) throws IOException {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(COOKIES_JSON)) {
            gson.toJson(cookies, writer);
        }
    }

    @NotNull
    private static Map<String, String> loadCookies() {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        try(FileReader fileReader = new FileReader(COOKIES_JSON)) {
            Map<String, String> cookies = gson.fromJson(fileReader, type);
            if (cookies == null)
                throw new IOException("File is empty");
            else
                return cookies;
        } catch (IOException e) {
            logger.debug(e.getMessage());
            return Maps.newHashMap();
        }
    }

    private static Map<String, String> cloudflare(Document document) throws Exception {
        logger.debug("Cloudflare started");

        String jschl_vc;
        String pass;
        String ischl_answer;

        String rawScript = document.select("script").first().data();
        String minScript = rawScript.replaceAll("[\n\\s]", "");

        Matcher matcher = PATTERN.matcher(minScript);
        if (!matcher.find())
            throw new Exception("No matches");
        String script = matcher.group(1) + matcher.group(2) + matcher.group(3);
        long millis = Long.parseLong(matcher.group(4));

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        int answer = ((Double) engine.eval(script)).intValue() + DOMAIN_LENGTH;
        ischl_answer = String.valueOf(answer);

        Element challenge_form = document.select("#challenge-form").first();
        jschl_vc = challenge_form.select("[name=\"jschl_vc\"]").first().val();
        pass = challenge_form.select("[name=\"pass\"]").first().val();

        Thread.sleep(millis);

        Connection.Response response = Jsoup.connect(CLOUDFLARE_URL)
                .userAgent(USERAGENT)
                .data(
                        "jschl_vc", jschl_vc,
                        "pass", pass,
                        "jschl_answer", ischl_answer
                )
                .method(Connection.Method.GET)
                .execute();

        logger.debug("Cloudflare finished");

        return response.cookies();
    }
}
