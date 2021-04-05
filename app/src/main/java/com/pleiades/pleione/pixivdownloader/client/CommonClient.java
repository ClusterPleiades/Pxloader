package com.pleiades.pleione.pixivdownloader.client;

import android.os.SystemClock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pleiades.pleione.pixivdownloader.pixiv.MD5;
import com.pleiades.pleione.pixivdownloader.pixiv.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.pleiades.pleione.pixivdownloader.Config.URI_LOGIN;
import static com.pleiades.pleione.pixivdownloader.Config.REFRESH_TOKENS;
import static com.pleiades.pleione.pixivdownloader.Variable.accessToken;
import static com.pleiades.pleione.pixivdownloader.Variable.accessUserId;
import static com.pleiades.pleione.pixivdownloader.Variable.isGuest;
import static com.pleiades.pleione.pixivdownloader.Variable.refreshTime;
import static com.pleiades.pleione.pixivdownloader.Variable.refreshToken;

public class CommonClient {
    public String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    public JSONObject getResponseJSONObject(HttpURLConnection conn) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            buffer.append(line);
        return JSON.parseObject(buffer.toString());
    }

    public Map<String, String> getDefaultParameterMap(int page) {
        return getDefaultParameterMap(page, "desc");
    }

    public Map<String, String> getDefaultParameterMap(int page, String order) {
        Map<String, String> params;

        if (order.equals("popular")) {
            params = createPopularParameterMap(page);
            params.put("order", "desc");
        } else {
            params = createDefaultParameterMap(page);
            params.put("order", order);
        }
        return params;
    }

    private Map<String, String> createDefaultParameterMap(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("image_size", "profile_image_sizes");
        params.put("profile_image_sizes", "px_170x170");
        params.put("include_sanity_level", "true");
        params.put("include_stats", "true");
        params.put("period", "all");
        params.put("page", Integer.toString(page));
        params.put("sort", "date"); // date
        return params;
    }

    private Map<String, String> createPopularParameterMap(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("image_size", "profile_image_sizes");
        params.put("profile_image_sizes", "px_170x170");
        params.put("include_sanity_level", "true");
        params.put("include_stats", "true");
        params.put("period", "all");
        params.put("page", Integer.toString(page));
        params.put("sort", "popular"); // popular
        return params;
    }

    public String buildUri(String uri, Map<String, String> parameterMap) {
        if (parameterMap.isEmpty())
            return uri;

        StringBuilder buffer = new StringBuilder(uri);

        if (!uri.endsWith("?"))
            buffer.append("?");

        for (Map.Entry<String, String> entry : parameterMap.entrySet())
            buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");

        buffer.deleteCharAt(buffer.length() - 1);

        return buffer.toString();
    }

    public boolean refreshAccessToken() {
        // case guest
        if (isGuest) {
            Random random = new Random();
            for (int i = 0; i < REFRESH_TOKENS.length; i++) {
                int randomValue = random.nextInt(REFRESH_TOKENS.length);
                if (refreshAccessToken(REFRESH_TOKENS[randomValue]))
                    return true;
            }
            return false;
        } else {
            return refreshAccessToken(refreshToken);
        }
    }

    private boolean refreshAccessToken(String refreshToken) {
        try {
            String pixivTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US).format(new Date());
            String pixivHash = MD5.convert(pixivTime + "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c");
            List<NameValuePair> params = new ArrayList<>();

            URL url = new URL(URI_LOGIN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // default values
            conn.setRequestProperty("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT");
            conn.setRequestProperty("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj");
            conn.setRequestProperty("device_token", "pixiv");
            conn.setRequestProperty("get_secure_url", "true");
            conn.setRequestProperty("include_policy", "true");
            params.add(new NameValuePair("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT"));
            params.add(new NameValuePair("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj"));
            params.add(new NameValuePair("device_token", "pixiv"));
            params.add(new NameValuePair("get_secure_url", "true"));
            params.add(new NameValuePair("include_policy", "true"));

            // values for refresh
            conn.setRequestProperty("refresh_token", refreshToken);
            conn.setRequestProperty("grant_type", "refresh_token");
            params.add(new NameValuePair("refresh_token", refreshToken));
            params.add(new NameValuePair("grant_type", "refresh_token"));

            // headers
            conn.setRequestProperty("User-Agent", "PixivAndroidApp/5.0.234 (Android 4.4.2; R831T)");
            conn.setRequestProperty("Accept-Language", "en_US");
            conn.setRequestProperty("App-OS", "android");
            conn.setRequestProperty("App-OS-Version", "4.4.2");
            conn.setRequestProperty("App-Version", "5.0.234");
            conn.setRequestProperty("X-Client-Time", pixivTime);
            conn.setRequestProperty("X-Client-Hash", pixivHash);
            params.add(new NameValuePair("User-Agent", "PixivAndroidApp/5.0.234 (Android 4.4.2; R831T)"));
            params.add(new NameValuePair("Accept-Language", "en_US"));
            params.add(new NameValuePair("App-OS", "android"));
            params.add(new NameValuePair("App-OS-Version", "4.4.2"));
            params.add(new NameValuePair("App-Version", "5.0.234"));
            params.add(new NameValuePair("X-Client-time", pixivTime));
            params.add(new NameValuePair("X-Client-Hash", pixivHash));

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            outputStream.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                JSONObject responseContent = getResponseJSONObject(conn).getJSONObject("response");
                accessToken = responseContent.getString("access_token");
                accessUserId = responseContent.getJSONObject("user").getString("id");
                refreshTime = SystemClock.elapsedRealtime();
                conn.disconnect();
                return true;
            } else {
                conn.disconnect();
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}