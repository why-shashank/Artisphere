package com.example.artisphere.Model;

public class Post {
    private String description;
    private String postImage;
    private String postid;
    private String publisher;

    public Post(String description, String postImage, String postid, String publisher) {
        this.description = description;
        this.postImage = postImage;
        this.postid = postid;
        this.publisher = publisher;
    }

    public Post() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
