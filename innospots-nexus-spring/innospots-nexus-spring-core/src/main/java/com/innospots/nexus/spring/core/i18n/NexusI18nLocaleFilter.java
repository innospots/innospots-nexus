package com.innospots.nexus.spring.core.i18n;

import com.innospots.nexus.base.i18n.I18nConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

/**
 * 将 Spring {@link LocaleContextHolder} 中的 locale 同步到 {@link I18nConverter}，供 JSON 序列化等使用。
 *
 * @author Smars
 * @date 2026/09/16
 */
public class NexusI18nLocaleFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Locale locale = LocaleContextHolder.getLocale();
        I18nConverter.setLocale(locale);
        try {
            filterChain.doFilter(request, response);
        } finally {
            I18nConverter.setLocale(null);
        }
    }
}
