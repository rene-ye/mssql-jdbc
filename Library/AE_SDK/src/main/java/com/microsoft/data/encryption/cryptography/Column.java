package com.microsoft.data.encryption.cryptography;

import java.util.List;


public class Column<T> extends IColumn {

    String name;

    Types dataType;

    public List<T> data;

    /**
     * Constructor for Column object
     * 
     * @param data
     */
    public Column(List<T> data) {
        this.data = data;
    }
}
