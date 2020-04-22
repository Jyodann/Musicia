package com.kimiwakirei.recyclerview;

public class UserProfile {

    public String userName, userEmail;

    public UserProfile(){

    }
    public UserProfile(String userName, String userEmail ) {
        this.userName = userName;
        this.userEmail = userEmail;

    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
