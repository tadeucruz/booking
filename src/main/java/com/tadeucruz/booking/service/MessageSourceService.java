package com.tadeucruz.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageSourceService {

    private final MessageSource messageSource;

    public String getMessage(String messageCode) {
        return getMessage(messageCode, (Object) null);
    }

    public String getMessage(String messageCode, Object... args) {
        return messageSource.getMessage(messageCode, args, LocaleContextHolder.getLocale());
    }
}
