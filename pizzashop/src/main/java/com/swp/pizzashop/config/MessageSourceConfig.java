package com.swp.pizzashop.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Central i18n configuration overriding Spring Boot's default message source auto-config.
 * This gives you:
 *  - UTF-8 encoding
 *  - Ability to add multiple basename bundles easily
 *  - Reloadable (good during development)
 *  - Locale switch via request param ?lang=xx
 *  - Integration with Bean Validation messages
 */
@Configuration
public class MessageSourceConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        // Add additional bundles by appending more entries to setBasenames
        ms.setBasenames("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setCacheSeconds(3600); // 1 hour cache; set to -1 in production for no refresh or 0 for always reload in dev
        ms.setFallbackToSystemLocale(false);
        ms.setUseCodeAsDefaultMessage(true); // If a code is missing, show the code instead of throwing
        return ms;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Switch locale with ?lang=vi, ?lang=en, etc.
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

