package com.acf.careerfinder.config;

import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;

    public WebConfig(UserService userService) {
        this.userService = userService;
    }

    private static final String LANG_COOKIE = "uiLang";
    private static final Set<String> ALLOWED_LANGS = Set.of("en", "hi", "mr");

    private static String normalizeLang(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_LANGS.contains(v) ? v : null;
    }

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    private static String cookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver r = new CookieLocaleResolver();
        r.setCookieName(LANG_COOKIE);
        r.setDefaultLocale(Locale.ENGLISH);
        r.setCookiePath("/");
        r.setCookieMaxAge((int) Duration.ofDays(365).getSeconds());
        return r;
    }

    private HandlerInterceptor localeInterceptor(LocaleResolver resolver) {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) {

                HttpSession session = request.getSession(false);

                String email = (session == null) ? null : (String) session.getAttribute("USER_EMAIL");
                boolean loggedIn = (email != null && !email.isBlank());

                String paramLang   = normalizeLang(request.getParameter("lang"));
                String sessionLang = (session == null) ? null : normalizeLang((String) session.getAttribute("uiLang"));
                String cookieLang  = normalizeLang(cookieValue(request, LANG_COOKIE));

                String chosen;

                if (loggedIn) {
                    // Logged-in users: never allow ?lang to override account language.
                    chosen = sessionLang;
                    if (chosen == null) {
                        chosen = normalizeLang(userService.getUiLang(email).orElse(null));
                        if (chosen == null) chosen = "en";
                        session.setAttribute("uiLang", chosen);
                    }
                } else {
                    // Anonymous: param wins (and we persist it in cookie), else cookie, else default.
                    chosen = (paramLang != null) ? paramLang : (cookieLang != null ? cookieLang : "en");
                }

                Locale locale = toLocale(chosen);

                // Make Thymeleaf + MessageSource use this locale for the current request
                LocaleContextHolder.setLocale(locale);

                // Persist cookie only when:
                // - anonymous explicitly chooses ?lang, OR
                // - logged in (optional: keep cookie synced so sample inherits after logout)
                try {
                    if (!loggedIn && paramLang != null) {
                        resolver.setLocale(request, response, locale);
                    } else if (loggedIn) {
                        resolver.setLocale(request, response, locale);
                    }
                } catch (Exception ignore) { }

                if (session != null) session.setAttribute("uiLang", chosen);

                return true;
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor(localeResolver()));
    }
}