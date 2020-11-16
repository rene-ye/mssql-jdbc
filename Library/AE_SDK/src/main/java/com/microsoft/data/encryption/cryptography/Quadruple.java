package com.microsoft.data.encryption.cryptography;

public class Quadruple<W, X, Y, Z> {
    final W w;
    final X x;
    final Y y;
    final Z z;

    public Quadruple(W w, X x, Y y, Z z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
