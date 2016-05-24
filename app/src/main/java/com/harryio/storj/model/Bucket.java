package com.harryio.storj.model;

import java.util.Arrays;

public class Bucket {
    int storage;
    int transfer;
    String status;
    String[] pubkeys;
    String user;
    String name;
    String created;
    String id;

    public int getStorage() {
        return storage;
    }

    public int getTransfer() {
        return transfer;
    }

    public String getStatus() {
        return status;
    }

    public String[] getPubkeys() {
        return pubkeys;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "storage=" + storage +
                ", transfer=" + transfer +
                ", status='" + status + '\'' +
                ", pubkeys=" + Arrays.toString(pubkeys) +
                ", user='" + user + '\'' +
                ", name='" + name + '\'' +
                ", created='" + created + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
