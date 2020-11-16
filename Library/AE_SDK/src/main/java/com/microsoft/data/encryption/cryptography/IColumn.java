package com.microsoft.data.encryption.cryptography;

import java.sql.Array;


abstract class IColumn {

    String name;

    Types dataType;

    Array data;
}
