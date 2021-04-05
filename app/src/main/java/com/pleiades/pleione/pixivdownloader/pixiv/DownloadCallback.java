package com.pleiades.pleione.pixivdownloader.pixiv;

import java.util.List;

public interface DownloadCallback {
    void onIllustrationDownloaded(Work work, byte[] bytes);

    void onUgoiraDownloaded(Work work, byte[] bytes);

    void onMangaDownloaded(Work work, List<byte[]> bytesList);
}
