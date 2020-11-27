package com.sanjeev.sn;

public class UsersModel {

    String uid;
    String name;
    String email;
    String phone;
    String image;
    String username;

    public UsersModel() {
    }

    public UsersModel(String uid, String name, String email, String phone, String image, String username) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
