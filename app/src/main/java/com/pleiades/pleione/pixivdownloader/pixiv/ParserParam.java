package com.pleiades.pleione.pixivdownloader.pixiv;

public class ParserParam {
    private WorkFilter filter;
    private WorkCallback callback;
    private WorkCallbackRank callbackRank;

    public ParserParam withFilter(WorkFilter filter) {
        this.filter = filter;
        return this;
    }

    public ParserParam withCallback(WorkCallback callback) {
        this.callback = callback;
        return this;
    }

    public ParserParam withCallbackRank(WorkCallbackRank callbackRank) {
        this.callbackRank = callbackRank;
        return this;
    }

    public WorkFilter getFilter() {
        return filter;
    }

    public WorkCallback getCallback() {
        return callback;
    }

    public WorkCallbackRank getCallbackRank(){
        return callbackRank;
    }
}
