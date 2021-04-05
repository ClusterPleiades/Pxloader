package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class Metadata {
    List<Page> pages;

    List<Frame> frames;

    @JSONField(name = "zip_urls")
    ZipUrls zipUrls;

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public ZipUrls getZipUrls() {
        return zipUrls;
    }

    public void setZipUrls(ZipUrls zipUrls) {
        this.zipUrls = zipUrls;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "pages=" + pages +
                ", frames=" + frames +
                ", zipUrls=" + zipUrls +
                '}';
    }
}
