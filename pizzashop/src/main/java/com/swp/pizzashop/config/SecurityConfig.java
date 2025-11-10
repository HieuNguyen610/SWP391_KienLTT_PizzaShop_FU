package com.swp.pizzashop.config;

import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.messages.SystemMessageCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.config.Customizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            String code;
            if (exception instanceof DisabledException) {
                code = SystemMessageCode.MSG15.name();
            } else if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
                code = SystemMessageCode.MSG08.name();
            } else {
                code = SystemMessageCode.MSG08.name();
            }
            response.sendRedirect(request.getContextPath() + "/login?errorCode=" + code);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService, AuthenticationFailureHandler authenticationFailureHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/verify", "/do-login",
                                "/forgot-password", "/reset-password", "/reset-password/**",
                                "/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico",
                                "/uploads/**", "/do-register", "/verify-otp", "/menu/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("Admin")
                        .requestMatchers("/user/**").hasRole("Customer")
                        .requestMatchers("/chef/**").hasRole("Chef")
                        .requestMatchers("/manager/**").hasRole("Manager")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/do-login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/menu", true)
                        .failureHandler(authenticationFailureHandler) // use custom handler
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        // Allow API requests and custom login/register endpoints without CSRF token
                        // Important: /payment/** is handled by JS and Stripe, so we exclude it here
                        .ignoringRequestMatchers("/api/**", "/do-login", "/register", "/forgot-password", "/reset-password", "/payment/**")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedPage("/error/403")
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("rememberMeSecretKey")
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .userDetailsService(userDetailsService)
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/menu", true)
                )
                // enable CORS so frontend fetch calls are accepted if CORS is used
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Primary
    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return email -> {
            var user = userService.findByEmail(email);
            if (user == null) throw new UsernameNotFoundException("User not found");

            boolean activeStatus = user.getStatus() != null && "ACTIVE".equalsIgnoreCase(user.getStatus().trim());
            Boolean isDeleted = user.getIsDeleted();
            boolean notDeleted = (isDeleted == null) || !isDeleted;
            boolean enabled = activeStatus && notDeleted;

            Set<GrantedAuthority> authorities = user.getRoles() == null ? Set.of() :
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                            .collect(Collectors.toSet());
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .disabled(!enabled)
                    .accountLocked(false)
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .build();
        };
    }

    // Provide a permissive CORS mapping for the small number of endpoints the frontend may call via fetch
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow same-origin local dev and also explicit https local origin if used
                registry.addMapping("/payment/**")
                        .allowedOrigins("http://localhost:8080", "https://localhost:8080")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders("*");
            }
        };
    }
}

