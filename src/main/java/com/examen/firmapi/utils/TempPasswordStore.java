package com.examen.firmapi.utils;

import java.util.HashMap;
import java.util.Map;

public class TempPasswordStore {

    private static final Map<String, String> tempPasswords = new HashMap<>();

    public static void save(String email, String tempPassword) {
        tempPasswords.put(email, tempPassword);
    }

    public static String get(String email) {
        return tempPasswords.get(email);
    }

    public static void remove(String email) {
        tempPasswords.remove(email);
    }
}