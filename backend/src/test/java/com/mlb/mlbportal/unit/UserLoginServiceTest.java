package com.mlb.mlbportal.unit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.security.jwt.JwtTokenProvider;
import com.mlb.mlbportal.security.jwt.LoginRequest;
import com.mlb.mlbportal.security.jwt.UserLoginService;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserLoginService userLoginService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return SUCCESS and set cookies on successful login")
    @SuppressWarnings("null")
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest(TEST_USER_USERNAME, "pass");

        when(this.authenticationManager.authenticate(any())).thenReturn(this.authentication);
        when(this.userDetailsService.loadUserByUsername(TEST_USER_USERNAME)).thenReturn(this.userDetails);
        when(this.jwtTokenProvider.generateAccessToken(this.userDetails)).thenReturn("access-token");
        when(this.jwtTokenProvider.generateRefreshToken(this.userDetails)).thenReturn("refresh-token");

        ResponseEntity<AuthResponse> result = this.userLoginService.login(this.response, loginRequest);

        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.SUCCESS);
        assertThat(result.getBody().getMessage()).isEqualTo("Auth successful. Tokens are created in cookie.");
        verify(this.response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Should return SUCCESS and set new access token on valid refresh")
    @SuppressWarnings("null")
    void testRefreshSuccess() {
        String refreshToken = "valid-refresh-token";
        Claims claims = mock(Claims.class);

        when(this.jwtTokenProvider.validateToken(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USER_USERNAME);
        when(this.userDetailsService.loadUserByUsername(TEST_USER_USERNAME)).thenReturn(this.userDetails);
        when(this.jwtTokenProvider.generateAccessToken(this.userDetails)).thenReturn("new-access-token");

        ResponseEntity<AuthResponse> result = this.userLoginService.refresh(this.response, refreshToken);

        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.SUCCESS);
        assertThat(result.getBody().getMessage()).isEqualTo("Auth successful. Tokens are created in cookie.");
        verify(this.response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Should return FAILURE on invalid refresh token")
    @SuppressWarnings("null")
    void testRefreshFailure() {
        String refreshToken = "invalid-token";

        when(this.jwtTokenProvider.validateToken(refreshToken)).thenThrow(new RuntimeException("Invalid"));

        ResponseEntity<AuthResponse> result = this.userLoginService.refresh(this.response, refreshToken);

        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.FAILURE);
        assertThat(result.getBody().getMessage()).isEqualTo("Failure while processing refresh token");
    }

    @Test
    @DisplayName("Should return SUCCESS and clear cookies on logout")
    @SuppressWarnings("null")
    void testLogoutSuccess() {
        when(this.authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(this.authentication);

        ResponseEntity<AuthResponse> result = this.userLoginService.logout(this.response);

        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.SUCCESS);
        assertThat(result.getBody().getMessage()).isEqualTo("Logout Successful");
        verify(this.response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Should return FAILURE with status 400 when logout is called on anonymous user")
    @SuppressWarnings("null")
    void testLogoutFailureWhenAnonymous() {
        Authentication anonymousAuth = mock(AnonymousAuthenticationToken.class);
        when(anonymousAuth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

        ResponseEntity<AuthResponse> result = this.userLoginService.logout(this.response);

        assertThat(result.getStatusCode().value()).isEqualTo(400);
        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.FAILURE);
        assertThat(result.getBody().getMessage()).isEqualTo("There is no user to logout");
    }

    @Test
    @DisplayName("Should return FAILURE with status 400 when logout is called with no authentication")
    @SuppressWarnings("null")
    void testLogoutFailureWhenNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        ResponseEntity<AuthResponse> result = this.userLoginService.logout(this.response);

        assertThat(result.getStatusCode().value()).isEqualTo(400);
        assertThat(result.getBody().getStatus()).isEqualTo(AuthResponse.Status.FAILURE);
        assertThat(result.getBody().getMessage()).isEqualTo("There is no user to logout");
    }
}