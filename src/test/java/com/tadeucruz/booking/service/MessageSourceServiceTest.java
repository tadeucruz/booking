package com.tadeucruz.booking.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class MessageSourceServiceTest {

    @InjectMocks
    private MessageSourceService messageSourceService;

    @Mock
    private MessageSource messageSource;

    @Test
    void test_getMessage() {

        var messageCode = "test";
        Object[] args = new Object[1];
        args[0] = null;

        messageSourceService.getMessage(messageCode);

        verify(messageSource).getMessage(eq(messageCode), eq(args),
            eq(LocaleContextHolder.getLocale()));
    }

    @Test
    void test_getMessage_withParams() {

        var messageCode = "test";
        var param = "1";
        Object[] args = new Object[1];
        args[0] = param;

        messageSourceService.getMessage(messageCode, param);

        verify(messageSource).getMessage(eq(messageCode), eq(args),
            eq(LocaleContextHolder.getLocale()));
    }
}