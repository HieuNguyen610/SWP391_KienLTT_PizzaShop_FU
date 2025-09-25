package com.swp.pizzashop.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("GET /login returns login view with empty LoginForm")
    void getLogin_showsForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginForm"));
    }

    @Test
    @DisplayName("POST /do-login with valid credentials redirects to /profile and stores SecurityContext in session")
    void postDoLogin_success_redirectsToProfile_andStoresContext() throws Exception {
        String email = "john@example.com";
        String password = "Secret123!";

        Authentication auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class)))
                .thenReturn(auth);

        MvcResult result = mockMvc.perform(post("/do-login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andReturn();

        Object ctx = Objects.requireNonNull(result.getRequest().getSession(false))
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        org.junit.jupiter.api.Assertions.assertNotNull(ctx, "SPRING_SECURITY_CONTEXT should be present in session");
    }

    @Test
    @DisplayName("POST /do-login with invalid input shows validation errors and stays on login view")
    void postDoLogin_validationErrors_staysOnLogin() throws Exception {
        mockMvc.perform(post("/do-login")
                        .param("email", "not-an-email")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errors"));
    }

    @Test
    @DisplayName("POST /do-login with bad credentials shows generic error and stays on login view")
    void postDoLogin_badCredentials_staysOnLogin() throws Exception {
        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/do-login")
                        .param("email", "john@example.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", notNullValue()));
    }

    @Test
    @DisplayName("GET /login?error shows generic error message")
    void getLogin_withErrorParam_showsErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", notNullValue()));
    }

    @Test
    @DisplayName("GET /login?logout shows success message")
    void getLogin_withLogoutParam_showsSuccessMessage() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("success", notNullValue()));
    }
}
