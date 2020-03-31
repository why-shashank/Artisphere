package com.example.artisphere.Model;

public class User {

    private String bio;
    private String dp;
    private String email;
    private String name;
    private String phone;
    private String uid;
    private String username;





    public User(String bio, String dp, String email, String name, String phone, String uid,String username){
        this.bio = bio;
        this.dp = dp;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.uid = uid;
        this.username = username;


    }
    public User(){
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
