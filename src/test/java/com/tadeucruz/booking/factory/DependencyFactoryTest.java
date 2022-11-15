package com.tadeucruz.booking.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;

@ExtendWith(MockitoExtension.class)
class DependencyFactoryTest {

    @InjectMocks
    private DependencyFactory dependencyFactory;

    @Test
    void test_modelMapper() {

        var result = dependencyFactory.modelMapper();

        assertNotNull(result);
    }

    @Test
    void test_restTemplate() {

        var result = dependencyFactory.restTemplate(new RestTemplateBuilder());

        assertNotNull(result);
    }

    @Test
    void test_messageSource() {

        var result = dependencyFactory.messageSource();

        assertNotNull(result);
    }
}