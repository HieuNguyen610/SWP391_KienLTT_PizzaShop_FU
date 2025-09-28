package com.swp.pizzashop.config;

import com.swp.pizzashop.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/verify", "/do-login",
                                 "/forgot-password", "/reset-password", "/reset-password/**",
                                 "/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/do-login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/profile", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true) // Invalidate HTTP session
                .clearAuthentication(true)    // Clear authentication
                .deleteCookies("JSESSIONID", "remember-me")  // Delete JSESSIONID and remember-me cookies
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Allow API requests and custom login/register endpoints without CSRF token
                .ignoringRequestMatchers("/api/**", "/do-login", "/register", "/forgot-password", "/reset-password")
            )
            // Redirect unauthenticated users to /login instead of sending 401 JSON
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedPage("/error/403")
            )
            // Use hash-based remember-me (TokenBasedRememberMeServices)
            .rememberMe(rememberMe -> rememberMe
                .key("rememberMeSecretKey") //  secure key
                .rememberMeParameter("remember-me") // Matches login.html
                .rememberMeCookieName("remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                // explicit userDetailsService required for hash-based remember-me to rebuild Authentication
                .userDetailsService(userDetailsService)
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/profile", true)
            );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
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
}
