package com.harryio.storj.model;

public class FrameModel {
    long __nonce;

    public FrameModel() {
        this.__nonce = System.currentTimeMillis();
    }
}
