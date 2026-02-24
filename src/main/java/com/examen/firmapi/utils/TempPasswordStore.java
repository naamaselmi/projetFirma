package com.examen.firmapi.utils;

import java.util.HashMap;
import java.util.Map;

public class TempPasswordStore {

    private static final Map<String, String> tempPasswords = new HashMap<>();

    public static void saveTempPassword(String email, String tempPassword) {
        tempPasswords.put(email, tempPassword);
    }

    public static String getTempPassword(String email) {
        return tempPasswords.get(email);
    }

    public static void removeTempPassword(String email) {
        tempPasswords.remove(email);
    }
}