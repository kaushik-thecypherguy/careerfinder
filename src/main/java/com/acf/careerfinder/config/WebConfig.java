package com.acf.careerfinder.config;

import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;

    public WebConfig(UserService userService) {
        this.userService = userService;
    }

    private static Locale toLocale(Object langObj) {
        String lang = (langObj instanceof String s && !s.isBlank()) ? (String) langObj : "en";
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    private HandlerInterceptor localeFromAccountInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                HttpSession session = request.getSession(false);

                String fromDb = null;
                if (session != null) {
                    String email = (String) session.getAttribute("USER_EMAIL");
                    if (email != null) {
                        fromDb = userService.getUiLang(email).orElse(null);
                    }
                }
                String fromSession = (session != null) ? (String) session.getAttribute("uiLang") : null;
                String lang = (fromDb != null && !fromDb.isBlank()) ? fromDb : fromSession;

                Locale locale = toLocale(lang);
                LocaleContextHolder.setLocale(locale);
                if (session != null) session.setAttribute("uiLang", locale.getLanguage());
                return true;
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeFromAccountInterceptor());
    }
}