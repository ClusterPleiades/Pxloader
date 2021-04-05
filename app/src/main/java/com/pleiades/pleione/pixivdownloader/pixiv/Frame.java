package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class Frame {
    @JSONField(name = "delay_msec")
    private int delayMsec;

    public int getDelayMsec() {
        return delayMsec;
    }

    public void setDelayMsec(int delayMsec) {
        this.delayMsec = delayMsec;
    }
}
