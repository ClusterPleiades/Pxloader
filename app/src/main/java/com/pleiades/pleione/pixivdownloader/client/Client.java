package com.pleiades.pleione.pixivdownloader.client;

import android.os.SystemClock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pleiades.pleione.pixivdownloader.pixiv.DownloadCallback;
import com.pleiades.pleione.pixivdownloader.pixiv.Page;
import com.pleiades.pleione.pixivdownloader.pixiv.ParserParam;
import com.pleiades.pleione.pixivdownloader.pixiv.Rank;
import com.pleiades.pleione.pixivdownloader.pixiv.RankWork;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;
import com.pleiades.pleione.pixivdownloader.pixiv.WorkCallback;
import com.pleiades.pleione.pixivdownloader.pixiv.WorkCallbackRank;
import com.pleiades.pleione.pixivdownloader.pixiv.WorkFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.pleiades.pleione.pixivdownloader.Config.PAGE_NO_NEXT;
import static com.pleiades.pleione.pixivdownloader.Config.PAGE_START;
import static com.pleiades.pleione.pixivdownloader.Config.URI_COLLECTION;
import static com.pleiades.pleione.pixivdownloader.Config.URI_FOLLOWING;
import static com.pleiades.pleione.pixivdownloader.Config.URI_RANKINGS;
import static com.pleiades.pleione.pixivdownloader.Config.URI_SEARCH;
import static com.pleiades.pleione.pixivdownloader.Config.URI_USER;
import static com.pleiades.pleione.pixivdownloader.Config.URI_WORK;
import static com.pleiades.pleione.pixivdownloader.Variable.accessToken;
import static com.pleiades.pleione.pixivdownloader.Variable.accessUserId;
import static com.pleiades.pleione.pixivdownloader.Variable.refreshTime;

public class Client {
    private final CommonClient commonClient;
    private final ThreadPoolExecutor threadPoolExecutor;

    public int currentPage = PAGE_START;
    public int currentPageSize, currentPageDownloadCount, totalDownloadCount;
    public long currentWorkId;

    private String collectionNextPageUri = null;
    private JSONObject paginationJSONObject;

    // constructor
    public Client() {
        this.commonClient = new CommonClient();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.threadPoolExecutor = new ThreadPoolExecutor(availableProcessors, availableProcessors, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500));

        refreshAccessToken();
    }

    // refresh access token
    public boolean refreshAccessToken() {
        if (SystemClock.elapsedRealtime() - refreshTime > 300000) // 5 minutes
            return commonClient.refreshAccessToken();
        return true;
    }

    // count download
    public void countDownload(int workId) {
        currentWorkId = workId;
        currentPageDownloadCount++;
        totalDownloadCount++;
    }

    // set core
    public void setPoolSize(int size){
        threadPoolExecutor.setCorePoolSize(size);
        threadPoolExecutor.setMaximumPoolSize(size);
    }

    // shut down process
    public void shutDownProcess() {
        threadPoolExecutor.shutdownNow();
    }

    // is page finished
    public boolean isPageFinished() {
        if (currentPageDownloadCount == currentPageSize) {
            currentPageDownloadCount = 0;
            return true;
        } else
            return false;
    }

    // is shut down
    public boolean isShutDown() {
        return threadPoolExecutor.isShutdown();
    }

    // build uri
    private URL buildKeywordURL(String keyword, String order) throws MalformedURLException {
        Map<String, String> parameterMap = commonClient.getDefaultParameterMap(currentPage, order);
        parameterMap.put("q", keyword);
        parameterMap.put("mode", "tag");
        parameterMap.put("per_page", "100");

        String keywordUri = commonClient.buildUri(URI_SEARCH, parameterMap);
        keywordUri = keywordUri.replace(" ", "%20");

        return new URL(keywordUri);
    }

    private URL buildRankingsURL(String date, String mode) throws MalformedURLException {
        Map<String, String> parameterMap = commonClient.getDefaultParameterMap(currentPage);
        parameterMap.put("mode", mode);
        parameterMap.put("per_page", "99999");
        if (date != null)
            parameterMap.put("date", date);
        else
            parameterMap.put("date", "");

        String rankingsUri = commonClient.buildUri(URI_RANKINGS, parameterMap);
        rankingsUri = rankingsUri.replace(" ", "%20");

        return new URL(rankingsUri);
    }

    private URL buildFollowingURL() throws MalformedURLException {
        Map<String, String> parameterMap = commonClient.getDefaultParameterMap(currentPage);
        parameterMap.put("mode", "tag");
        parameterMap.put("per_page", "100");

        String followingUri = commonClient.buildUri(URI_FOLLOWING, parameterMap);
        followingUri = followingUri.replace(" ", "%20");

        return new URL(followingUri);
    }

    private URL buildCollectionURL(String publicity) throws MalformedURLException {
        Map<String, String> parameterMap = commonClient.getDefaultParameterMap(currentPage);
        parameterMap.put("mode", "tag");
        parameterMap.put("per_page", "100");
        parameterMap.put("user_id", accessUserId);
        parameterMap.put("restrict", publicity);

        String collectionUri = commonClient.buildUri(URI_COLLECTION, parameterMap);
        collectionUri = collectionUri.replace(" ", "%20");

        return new URL(collectionUri);
    }

    private URL buildUserUri(String userId) throws MalformedURLException {
        Map<String, String> parameterMap = commonClient.getDefaultParameterMap(currentPage);
        parameterMap.put("mode", "tag");
        parameterMap.put("per_page", "100");

        String userUri = commonClient.buildUri(URI_USER.replace("{authorId}", userId), parameterMap);
        userUri = userUri.replace(" ", "%20");

        return new URL(userUri);
    }

    private URL buildWorkUri(String workId) throws MalformedURLException {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("image_sizes", "small,medium,large");
        parameterMap.put("include_stats", "true");

        String workUri = commonClient.buildUri(URI_WORK.replace("{illustId}", workId), parameterMap);
        workUri = workUri.replace(" ", "%20");

        return new URL(workUri);
    }

    // download
    public void searchKeyword(String keyword, ParserParam parserParam, String order) {
        try {
            // build keyword URL
            URL keywordURL = buildKeywordURL(keyword, order);

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) keywordURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject responseJSONObject = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            // initialize response JSON array
            JSONArray responseJSONArray = responseJSONObject.getJSONArray("response");

            // initialize pagination JSON object
            paginationJSONObject = (JSONObject) responseJSONObject.get("pagination");

            // initialize work list
            ArrayList<Work> workList = new ArrayList<>();
            for (int i = 0; i < responseJSONArray.size(); i++) {
                if (isShutDown())
                    return;

                Work work = JSON.parseObject(responseJSONArray.getJSONObject(i).toJSONString(), Work.class);
                WorkFilter filter = parserParam.getFilter();
                if (filter == null || filter.doFilter(work))
                    workList.add(work);
            }

            // initialize current page size
            currentPageSize = workList.size();

            // callback on found
            WorkCallback callback = parserParam.getCallback();
            if (currentPageSize > 0) {
                for (Work work : workList) {
                    if (isShutDown())
                        return;

                    callback.onFound(work);
                }
            } else
                callback.onFound(null);

        } catch (Exception e) {
            parserParam.getCallback().onFound(null);
        }
    }

    public Rank searchRank(String date, String mode) {
        try {
            // build rankings URL
            URL rankingsURL = buildRankingsURL(date, mode);

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) rankingsURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject responseJSONObject = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            // initialize response JSON array
            JSONArray responseJSONArray = responseJSONObject.getJSONArray("response");

            return JSON.parseObject(responseJSONArray.getJSONObject(0).toJSONString(), Rank.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void searchRankings(Rank rank, ParserParam parserParam) {
        // initialize rank work list
        List<RankWork> rankWorkList = rank.getWorks();

        // initialize current page size
        currentPageSize = rankWorkList.size();

        // callback on found
        WorkCallbackRank callbackRank = parserParam.getCallbackRank();
        for (RankWork rankWork : rankWorkList) {
            if (isShutDown())
                return;
            callbackRank.onFoundRank(rankWork.getWork(), rankWork.getRank());
        }
    }

    public void searchFollowing(ParserParam parserParam) {
        try {
            // build following URL
            URL followingURL = buildFollowingURL();

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) followingURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject responseJSONObject = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            // initialize response JSON array
            JSONArray responseJSONArray = responseJSONObject.getJSONArray("response");

            // initialize pagination JSON object
            paginationJSONObject = (JSONObject) responseJSONObject.get("pagination");

            // initialize work list
            ArrayList<Work> workList = new ArrayList<>();
            for (int i = 0; i < responseJSONArray.size(); i++) {
                if (isShutDown())
                    return;

                Work work = JSON.parseObject(responseJSONArray.getJSONObject(i).toJSONString(), Work.class);
                WorkFilter filter = parserParam.getFilter();
                if (filter == null || filter.doFilter(work))
                    workList.add(work);
            }

            // initialize current page size
            currentPageSize = workList.size();

            // callback on found
            WorkCallback callback = parserParam.getCallback();
            if (currentPageSize > 0) {
                for (Work work : workList) {
                    if (isShutDown())
                        return;

                    callback.onFound(work);
                }
            } else
                callback.onFound(null);

        } catch (Exception e) {
            parserParam.getCallback().onFound(null);
        }
    }

    public void searchCollection(String publicity, ParserParam parserParam) {
        try {
            // build collection URL
            URL collectionURL = (collectionNextPageUri == null) ? buildCollectionURL(publicity) : new URL(collectionNextPageUri);

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) collectionURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://app-api.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject responseJSONObject = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            // initialize illusts JSON array
            JSONArray illustsJSONArray = responseJSONObject.getJSONArray("illusts");

            // initialize collection next page uri
            collectionNextPageUri = responseJSONObject.getString("next_url");

            // initialize work list
            ArrayList<Work> workList = new ArrayList<>();
            for (int i = 0; i < illustsJSONArray.size(); i++) {
                if (isShutDown())
                    return;

                Work work = JSON.parseObject(illustsJSONArray.getJSONObject(i).toJSONString(), Work.class);
                WorkFilter filter = parserParam.getFilter();
                if (filter == null || filter.doFilter(work))
                    workList.add(work);
            }

            // initialize current page size
            currentPageSize = workList.size();

            // callback on found
            WorkCallback callback = parserParam.getCallback();
            if (currentPageSize > 0) {
                for (Work work : workList) {
                    if (isShutDown())
                        return;

                    callback.onFound(work);
                }
            } else
                callback.onFound(null);

        } catch (Exception e) {
            parserParam.getCallback().onFound(null);
        }
    }

    public void searchUser(String userId, ParserParam parserParam) {
        try {
            // build user URL
            URL userURL = buildUserUri(userId);

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) userURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject responseJSONObject = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            // initialize response JSON array
            JSONArray responseJSONArray = responseJSONObject.getJSONArray("response");

            // initialize pagination JSON object
            paginationJSONObject = (JSONObject) responseJSONObject.get("pagination");

            // initialize works
            ArrayList<Work> workList = new ArrayList<>();
            for (int i = 0; i < responseJSONArray.size(); i++) {
                if (isShutDown())
                    return;

                Work work = JSON.parseObject(responseJSONArray.getJSONObject(i).toJSONString(), Work.class);
                WorkFilter filter = parserParam.getFilter();
                if (filter == null || filter.doFilter(work))
                    workList.add(work);
            }

            // initialize current page size
            currentPageSize = workList.size();

            // callback on found
            WorkCallback callback = parserParam.getCallback();
            if (currentPageSize > 0) {
                for (Work work : workList) {
                    if (isShutDown())
                        return;

                    callback.onFound(work);
                }
            } else
                callback.onFound(null);

        } catch (Exception e) {
            parserParam.getCallback().onFound(null);
        }
    }

    public Work searchWork(String workId) {
        try {
            // build work URL
            URL workURL = buildWorkUri(workId);

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) workURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize response JSON object
            JSONObject json = commonClient.getResponseJSONObject(httpURLConnection);
            httpURLConnection.disconnect();

            return JSON.parseObject(json.getJSONArray("response").getJSONObject(0).toJSONString(), Work.class);
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] searchFrameZipBytes(Work work) {
        try {
            // build frame zip URL
            URL URL = new URL(work.getMetadata().getZipUrls().getUgoira600x600());

            // initialize http URL connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) URL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            httpURLConnection.setRequestProperty("Referer", "http://spapi.pixiv.net/");
            httpURLConnection.setRequestProperty("User", "PixivIOSApp/7.9.7");

            // initialize IO stream
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            // read write steam
            int read;
            while ((read = bufferedInputStream.read()) != -1)
                bufferedOutputStream.write(read);

            // close buffered IO stream
            bufferedInputStream.close();
            bufferedOutputStream.flush();
            bufferedOutputStream.close();

            // initialize frame zip bytes
            byte[] frameZipBytes = outputStream.toByteArray();

            // close IO stream
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            httpURLConnection.disconnect();

            return frameZipBytes;
        } catch (Exception e) {
            return null;
        }
    }

    public int indexNextPage() {
        if (paginationJSONObject == null)
            return PAGE_NO_NEXT;
        else {
            Integer nextPage = paginationJSONObject.getInteger("next");
            if (nextPage == null)
                return PAGE_NO_NEXT;
            else {
                currentPage = nextPage;
                return nextPage;
            }
        }
    }

    public int indexNextPageUri() {
        if (collectionNextPageUri == null)
            return PAGE_NO_NEXT;
        else {
            currentPage++;
            return currentPage;
        }
    }

    public boolean submitWork(Work work, DownloadCallback callback) {
        if (work == null)
            return false;
        else {
            if (!isShutDown()) {
                work = searchWork(Integer.toString(work.getId()));
                if (work == null)
                    return false;
                else
                    threadPoolExecutor.submit(new Downloader(work, callback));
            }
            return true;
        }
    }

    public boolean submitWork(String workId, DownloadCallback callback) {
        Work work = new Work();
        work.setId(Integer.parseInt(workId));
        return submitWork(work, callback);
    }

    private class Downloader implements Runnable {
        private final Work work;
        private final DownloadCallback callback;

        Downloader(Work work, DownloadCallback callback) {
            this.work = work;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (isShutDown())
                return;

            if (refreshAccessToken()) {
                if (work.isManga()) {
                    ArrayList<byte[]> imageBytesList = new ArrayList<>();
                    for (Page page : work.getMetadata().getPages())
                        imageBytesList.add(downloadImage(page.getImageUrls().getLarge()));

                    callback.onMangaDownloaded(work, imageBytesList);
                } else {
                    byte[] imageBytes = downloadImage(work.getImageUrls().getLarge());

                    if (work.isUgoira())
                        callback.onUgoiraDownloaded(work, imageBytes);
                    else
                        callback.onIllustrationDownloaded(work, imageBytes);
                }
            }
        }

        private byte[] downloadImage(String imageUri) {
            try {
                // build image URL
                URL imageURL = new URL(imageUri);

                // initialize http URL connection
                HttpURLConnection httpURLConnection = (HttpURLConnection) imageURL.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Referer", "http://www.pixiv.net");

                // initialize IO stream
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                // read write steam
                int read;
                while ((read = bufferedInputStream.read()) != -1)
                    bufferedOutputStream.write(read);

                // close buffered IO stream
                bufferedInputStream.close();
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                // initialize image bytes
                byte[] imageBytes = outputStream.toByteArray();

                // close IO stream
                httpURLConnection.disconnect();
                inputStream.close();
                outputStream.flush();
                outputStream.close();

                return imageBytes;
            } catch (Exception e) {
                return null;
            }
        }
    }
}