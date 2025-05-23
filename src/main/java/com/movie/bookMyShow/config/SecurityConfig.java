package com.movie.bookMyShow.config;

import com.movie.bookMyShow.util.AdminJwtFilter;
import com.movie.bookMyShow.util.CityJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Configuration
//public class SecurityConfig {
//
//    private final JwtFilter jwtFilter;
//
//    public SecurityConfig(JwtFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/city/set", "/city/get").permitAll() // ✅ Allow public access
//                        .requestMatchers("/admin/**").authenticated() // 🔒 Require authentication for admin APIs
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults()) // ✅ Enable Basic Authentication for username-password login
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class);
//
//
//        return http.build();
//    }
//
//
//
//    // ✅ Custom Filter to Log Requests
//    public static class RequestLoggingFilter extends OncePerRequestFilter {
//        @Override
//        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//                throws ServletException, IOException {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            System.out.println("🔍 Request: " + request.getMethod() + " " + request.getRequestURL());
//            System.out.println("🔍 Authenticated? " + (authentication != null));
//            filterChain.doFilter(request, response);
//        }
//    }
//}

@Configuration
public class SecurityConfig {

    private final CityJwtFilter cityJwtFilter;
    private final AdminJwtFilter adminJwtFilter;

    public SecurityConfig(CityJwtFilter cityJwtFilter, AdminJwtFilter adminJwtFilter) {
        this.cityJwtFilter = cityJwtFilter;
        this.adminJwtFilter = adminJwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/city/**").permitAll() // Public access
                        .requestMatchers("/admin/register", "/admin/login").permitAll()
                        .requestMatchers("/admin/update/**").hasRole("ADMIN")
                        .anyRequest().authenticated() // All other endpoints need JWT
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(cityJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    public static class RequestLoggingFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🔍 Request: " + request.getMethod() + " " + request.getRequestURL());
            System.out.println("🔍 Authenticated? " + (authentication != null));
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}