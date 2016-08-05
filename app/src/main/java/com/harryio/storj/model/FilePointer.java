package com.harryio.storj.model;

public class FilePointer {
    long size;
    private String hash;
    private String token;
    private String operation;
    private Farmer farmer;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public long getSize() {
        return size;
    }
}
