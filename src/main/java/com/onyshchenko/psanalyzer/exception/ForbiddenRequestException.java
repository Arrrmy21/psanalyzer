package com.onyshchenko.psanalyzer.exception;

import org.jsoup.HttpStatusException;

public class ForbiddenRequestException extends HttpStatusException {

    public ForbiddenRequestException(String message, int statusCode, String url) {
        super(message, statusCode, url);
    }
}
