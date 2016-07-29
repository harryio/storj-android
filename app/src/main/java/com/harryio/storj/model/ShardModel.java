package com.harryio.storj.model;

import com.google.gson.annotations.Expose;

import java.util.UUID;

public class ShardModel {
    String hash;
    int size;
    int index;
    String[] challenges;
    String[] tree;
    String __nonce;
    @Expose
    String shardPath;

    public ShardModel(String hash, int size, int index, String[] challenges, String[] trees) {
        this.hash = hash;
        this.size = size;
        this.index = index;
        this.challenges = challenges;
        this.tree = trees;
        final UUID uuid = UUID.randomUUID();
        this.__nonce = uuid.toString();
    }

    public String getShardPath() {
        return shardPath;
    }

    public void setShardPath(String shardPath) {
        this.shardPath = shardPath;
    }
}