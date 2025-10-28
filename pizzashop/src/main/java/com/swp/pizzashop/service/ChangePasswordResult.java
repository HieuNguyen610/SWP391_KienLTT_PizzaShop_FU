package com.swp.pizzashop.service;

public enum ChangePasswordResult {
    SUCCESS,
    OLD_PASSWORD_INVALID,
    NEW_PASSWORD_SAME_AS_OLD,
    WEAK_NEW_PASSWORD,
    ERROR
}

