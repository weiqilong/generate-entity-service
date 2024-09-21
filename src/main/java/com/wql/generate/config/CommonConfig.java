package com.wql.generate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Desc:
 *
 * @author: weiqi
 * 2024/9/21 09:34
 */
@Configuration
public class CommonConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有路径进行跨域配置
        registry.addMapping("/**")
                .allowedOrigins("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有头部信息
                .allowCredentials(false) // 是否允许证书
                .maxAge(3600); // 预检请求的有效期，单位为秒
    }
}
