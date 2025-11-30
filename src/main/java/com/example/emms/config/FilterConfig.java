package com.example.emms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final SimpleAuthFilter simpleAuthFilter;

    @Bean
    public FilterRegistrationBean<SimpleAuthFilter> authFilterRegistration() {
        FilterRegistrationBean<SimpleAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(simpleAuthFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1); // 最高优先级
        return registrationBean;
    }
}