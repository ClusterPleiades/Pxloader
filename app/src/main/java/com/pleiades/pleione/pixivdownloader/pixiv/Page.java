package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class Page {
    @JSONField(name = "image_urls")
    ImageUrls imageUrls;

    public ImageUrls getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ImageUrls imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public String toString() {
        return "Page{" +
                "imageUrls=" + imageUrls +
                '}';
    }
}
