package com.microsoft.data.encryption.cryptography;

import java.util.List;


public abstract class IColumnarDataReader {
    abstract Iterable<List<IColumn>> read();

    List<FileEncryptionSettings> getFileEncryptionSettings;
}
