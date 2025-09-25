package com.swp.pizzashop.config;

import com.swp.pizzashop.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/verify", "/do-login",
                                 "/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            // Disable Spring Security's built-in form login to let our controller handle POST /do-login
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true) // Invalidate HTTP session
                .clearAuthentication(true)    // Clear authentication
                .deleteCookies("JSESSIONID")  // Delete JSESSIONID cookie
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Allow API requests and custom login/register endpoints without CSRF token
                .ignoringRequestMatchers("/api/**", "/do-login", "/register")
            )
            // Redirect unauthenticated users to /login instead of sending 401 JSON
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
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
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }
}
