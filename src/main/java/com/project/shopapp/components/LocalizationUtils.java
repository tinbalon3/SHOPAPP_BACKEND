package com.project.shopapp.components;

import com.project.shopapp.untils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@RequiredArgsConstructor
@Configuration
public class LocalizationUtils {
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public String getLocalizeMessage(String messageKey,Object ... params){//spread operator
        Locale locale = localeResolver.resolveLocale(WebUtils.getCurrentRequest());
        return messageSource.getMessage(messageKey,params,locale);
    }
}
