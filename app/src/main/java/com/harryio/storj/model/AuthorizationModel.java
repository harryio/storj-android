package com.harryio.storj.model;

public class AuthorizationModel {
    String token;
    String hash;
    String operation;

    public AuthorizationModel(String hash, String operation, String token) {
        this.hash = hash;
        this.operation = operation;
        this.token = token;
    }
}
