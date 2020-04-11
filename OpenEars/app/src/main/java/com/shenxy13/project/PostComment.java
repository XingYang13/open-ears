package com.shenxy13.project;
public class PostComment implements Comparable<PostComment> {
    private String postId, text;
    private long score;
    public PostComment(String postId, String text, long score) {
        this.postId = postId;
        this.text = text;
        this.score = score;
    }
    public String getText() {
        return text;
    }
    public String getPostId() {
        return postId;
    }
    public long getScore() {
        return score;
    }
    @Override public int compareTo(PostComment u) {
        return (int) (u.score - this.score);
    }
}