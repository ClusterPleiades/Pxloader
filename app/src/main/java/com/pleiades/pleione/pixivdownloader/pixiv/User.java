package com.pleiades.pleione.pixivdownloader.pixiv;

import com.alibaba.fastjson.annotation.JSONField;

public class User {
    private int id;
    private String account, name;

    @JSONField(name = "is_friend")
    private boolean following;

    @JSONField(name = "is_friend")
    private boolean follower;

    @JSONField(name = "is_friend")
    private boolean friend;

    @JSONField(name = "is_premium")
    private boolean premium;

    @JSONField(name = "profile_image_urls")
    private ProfileImageUrls profileImageUrls;

    private String stats, profile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollower() {
        return follower;
    }

    public void setFollower(boolean follower) {
        this.follower = follower;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public ProfileImageUrls getProfileImageUrls() {
        return profileImageUrls;
    }

    public void setProfileImageUrls(ProfileImageUrls profileImageUrls) {
        this.profileImageUrls = profileImageUrls;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "User{" +
                "stats='" + stats + '\'' +
                ", id=" + id +
                ", account='" + account + '\'' +
                ", name='" + name + '\'' +
                ", following=" + following +
                ", follower=" + follower +
                ", friend=" + friend +
                ", premium=" + premium +
                ", profileImageUrls=" + profileImageUrls +
                ", profile='" + profile + '\'' +
                '}';
    }
}
