package com.harryio.storj.model;

public class User {
    String email;
    String password;
    String pubkey;

    public User(String email, String password, String pubkey) {
        this.email = email;
        this.password = password;
        this.pubkey = pubkey;
    }
}
