package com.pleiades.pleione.pixivdownloader.pixiv;

import java.util.Date;
import java.util.List;

public class Rank {
    private String content;
    private String mode;
    private Date date;
    private List<RankWork> works;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<RankWork> getWorks() {
        return works;
    }

    public void setWorks(List<RankWork> works) {
        this.works = works;
    }

    @Override
    public String toString() {
        return "Rank{" +
               "content='" + content + '\'' +
                ", mode='" + mode + '\'' +
                ", date=" + date +
                ", works=" + works +
                '}';
    }
}
