package com.harryio.storj.model;

public class Shard {
    String token;
    String hash;
    String operation;
    String channel;

    public String getToken() {
        return token;
    }

    public String getHash() {
        return hash;
    }

    public String getOperation() {
        return operation;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Shard{" +
                "token='" + token + '\'' +
                ", hash='" + hash + '\'' +
                ", operation='" + operation + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
