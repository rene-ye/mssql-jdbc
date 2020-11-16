package com.microsoft.data.encryption.cryptography;

public class Triple<X, Y, Z> {
    final X x;
    final Y y;
    final Z z;

    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
