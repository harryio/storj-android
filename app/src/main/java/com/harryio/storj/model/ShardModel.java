package com.harryio.storj.model;

import com.google.gson.annotations.Expose;

public class ShardModel {
    String hash;
    int size;
    int index;
    String[] challenges;
    String[] tree;
    @Expose
    String shardPath;

    public ShardModel(String hash, int size, int index, String[] challenges, String[] trees) {
        this.hash = hash;
        this.size = size;
        this.index = index;
        this.challenges = challenges;
        this.tree = trees;
    }

    public String getShardPath() {
        return shardPath;
    }

    public void setShardPath(String shardPath) {
        this.shardPath = shardPath;
    }
}