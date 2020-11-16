//package com.microsoft.data.encryption.cryptography;
//
//public class testSerial {
//
//    public static void main(String[] args) throws AAPSDKException {
//        boolean t = true;
//        boolean f = false;
//        Boolean tB = true;
//        Boolean fB = false;
//        Boolean n = null;
//
//        byte[] a = StandardSingleton.serialize(t);
//        byte[] b = StandardSingleton.serialize(f);
//        byte[] c = StandardSingleton.serialize(tB);
//        byte[] d = StandardSingleton.serialize(fB);
//        byte[] e = StandardSingleton.serialize(n);
//
//        System.out.println(a);
//        System.out.println(b);
//        System.out.println(c);
//        System.out.println(d);
//        System.out.println(e);
//
//        String id = Boolean.class.getName();
//        System.out.println(StandardSingleton.deserialize(id, a));
//        System.out.println(StandardSingleton.deserialize(id, b));
//        System.out.println(StandardSingleton.deserialize(id, c));
//        System.out.println(StandardSingleton.deserialize(id, d));
//        System.out.println(StandardSingleton.deserialize(id, e));
//    }
//}
