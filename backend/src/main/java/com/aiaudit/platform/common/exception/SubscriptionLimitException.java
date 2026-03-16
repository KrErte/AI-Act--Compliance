package com.aiaudit.platform.common.exception;

public class SubscriptionLimitException extends RuntimeException {

    public SubscriptionLimitException(String message) {
        super(message);
    }
}
