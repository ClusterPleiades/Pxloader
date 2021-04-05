package com.pleiades.pleione.pixivdownloader.ui;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Art implements Comparable<Art> {
    public String name;
    public Uri uri;
    public long lastModified;

    public Art(String name, Uri uri, long lastModified) {
        this.name = name;
        this.uri = uri;
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(@NonNull Art art) {
        return -1 * Long.compare(this.lastModified, art.lastModified);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.uri.equals(((Art)obj).uri);
    }
}