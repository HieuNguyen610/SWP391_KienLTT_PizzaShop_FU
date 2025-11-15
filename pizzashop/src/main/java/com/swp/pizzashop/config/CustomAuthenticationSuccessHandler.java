package com.swp.pizzashop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            // Redirect to originally requested protected resource
            String target = savedRequest.getRedirectUrl();
            redirectStrategy.sendRedirect(request, response, target);
            return;
        }

        String context = request.getContextPath();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Role priority ordering
        if (hasRole(authorities, "ROLE_Customer")) {
            redirectStrategy.sendRedirect(request, response, context + "/menu");
            return;
        }
        if (hasRole(authorities, "ROLE_Admin")) {
            redirectStrategy.sendRedirect(request, response, context + "/admin");
            return;
        }
        if (hasRole(authorities, "ROLE_Cashier")) {
            redirectStrategy.sendRedirect(request, response, context + "/cashier/orders");
            return;

        }
        // Other roles fallback
        redirectStrategy.sendRedirect(request, response, context + "/admin");
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        if (authorities == null) return false;
        for (GrantedAuthority ga : authorities) {
            if (role.equalsIgnoreCase(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}

