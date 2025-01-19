package com.m2i.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticLogger {
    private static final Logger logger = LoggerFactory.getLogger(StaticLogger.class);

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}