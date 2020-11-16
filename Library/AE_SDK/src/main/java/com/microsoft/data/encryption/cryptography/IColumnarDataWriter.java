package com.microsoft.data.encryption.cryptography;

import java.util.List;


public abstract class IColumnarDataWriter {
    abstract void write(List<IColumn> columns);

    List<FileEncryptionSettings> getFileEncryptionSettings;
}
