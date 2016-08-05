package com.harryio.storj.model;

import java.util.UUID;

public class TokenModel {
    String operation;
    String __nonce;

    public TokenModel(Operation operation) {
        this.operation = operation.name();
        this.__nonce = UUID.randomUUID().toString();
    }
}
