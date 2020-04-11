package com.shenxy13.project;
public class User implements Comparable<User> {
    private String displayName, uid;
    private long permissions, score;
    public User(String uid, String displayName, long permissions, long score) {
        this.uid = uid;
        this.displayName = displayName;
        this.permissions = permissions;
        this.score = score;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getUid() {
        return uid;
    }
    public long getPermissions() {
        return permissions;
    }
    public long getScore() {
        return score;
    }
    @Override public int compareTo(User u) {
        if (u.permissions != this.permissions) return (int) (u.permissions - this.permissions);
        return (int) (u.score - this.score);
    }
}