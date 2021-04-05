package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class RankWork {
    @JSONField(name = "previous_rank")
    private int previousRank;

    private int rank;
    private Work work;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPreviousRank() {
        return previousRank;
    }

    public void setPreviousRank(int previousRank) {
        this.previousRank = previousRank;
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    @Override
    public String toString() {
        return "Rank{" +
                "rank=" + rank +
                ", previousRank=" + previousRank +
                ", work=" + work +
                '}';
    }
}
