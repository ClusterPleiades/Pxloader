package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class FavoritedCount {

    @JSONField(name = "public")
    private int publicCount;

    @JSONField(name = "private")
    private int privateCount;

    public int getPublicCount() {
        return publicCount;
    }

    public void setPublicCount(int publicCount) {
        this.publicCount = publicCount;
    }

    public int getPrivateCount() {
        return privateCount;
    }

    public void setPrivateCount(int privateCount) {
        this.privateCount = privateCount;
    }

    @Override
    public String toString() {
        return "FavoritedCount{" +
                "publicCount=" + publicCount +
                ", privateCount=" + privateCount +
                '}';
    }
}


