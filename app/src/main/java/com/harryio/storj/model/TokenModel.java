package com.harryio.storj.model;

import java.util.UUID;

public class TokenModel {
    String operation;
    String __nonce;

    public TokenModel(String operation) {
        this.operation = operation;
        this.__nonce = UUID.randomUUID().toString();
    }
}
