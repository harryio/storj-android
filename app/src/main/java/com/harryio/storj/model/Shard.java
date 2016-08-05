package com.harryio.storj.model;

public class Shard {
    String token;
    String hash;
    String operation;
    Farmer farmer;

    public String getToken() {
        return token;
    }

    public String getHash() {
        return hash;
    }

    public String getOperation() {
        return operation;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    @Override
    public String toString() {
        return "Shard{" +
                "token='" + token + '\'' +
                ", hash='" + hash + '\'' +
                ", operation='" + operation + '\'' +
                ", farmer='" + farmer.toString() + '\'' +
                '}';
    }
}
