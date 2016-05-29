package com.harryio.storj.model;

public class ShardModel {
    String hash;
    int size;
    int index;
    String[] challenges;
    String[] trees;
    long __nonce;

    public ShardModel(String hash, int size, int index, String[] challenges, String[] trees) {
        this.hash = hash;
        this.size = size;
        this.index = index;
        this.challenges = challenges;
        this.trees = trees;
        this.__nonce = System.currentTimeMillis();
    }
}
