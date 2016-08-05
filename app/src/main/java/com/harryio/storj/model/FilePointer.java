package com.harryio.storj.model;

public class FilePointer {
    private String hash;
    private String token;
    private String operation;
    private String channel;

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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "FilePointer{" +
                "hash='" + hash + '\'' +
                ", token='" + token + '\'' +
                ", operation='" + operation + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
