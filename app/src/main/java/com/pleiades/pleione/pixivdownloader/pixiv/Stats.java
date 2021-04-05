package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class Stats {
    @JSONField(name = "scored_count")
    private int scoredCount;

    @JSONField(name = "views_count")
    private int viewsCount;

    @JSONField(name = "favorited_count")
    private FavoritedCount favoritedCount;

    @JSONField(name = "commented_count")
    private String commentedCount;

    private int score;

    public int getScoredCount() {
        return scoredCount;
    }

    public void setScoredCount(int scoredCount) {
        this.scoredCount = scoredCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public FavoritedCount getFavoritedCount() {
        return favoritedCount;
    }

    public void setFavoritedCount(FavoritedCount favoritedCount) {
        this.favoritedCount = favoritedCount;
    }

    public String getCommentedCount() {
        return commentedCount;
    }

    public void setCommentedCount(String commentedCount) {
        this.commentedCount = commentedCount;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "scoredCount=" + scoredCount +
                ", score=" + score +
                ", viewsCount=" + viewsCount +
                ", favoritedCount=" + favoritedCount +
                ", commentedCount='" + commentedCount + '\'' +
                '}';
    }
}
