package com.campus.campus_system.config;

import com.campus.campus_system.interceptor.AuthInterceptor;
import com.campus.campus_system.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class AuthConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final String imageUploadDir;

    public AuthConfig(
            AuthInterceptor authInterceptor,
            AdminInterceptor adminInterceptor,
            @org.springframework.beans.factory.annotation.Value("${app.upload.image-dir:uploads/images}") String imageUploadDir
    ) {
        this.authInterceptor = authInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.imageUploadDir = imageUploadDir;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = Path.of(imageUploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(resourceLocation.endsWith("/") ? resourceLocation : resourceLocation + "/");
    }
}
