package com.microsoft.data.encryption.cryptography;

public class Type {
    final String ID;
    final int PRECISION;
    final int SCALE;

    public Type(String id, int precision, int scale) {
        this.ID = id;
        this.PRECISION = precision;
        this.SCALE = scale;
    }
}
