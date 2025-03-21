package org.thoughtlabs.blogbackend.security.services.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.thoughtlabs.blogbackend.config.AppProperties;
import org.thoughtlabs.blogbackend.models.RefreshToken;
import org.thoughtlabs.blogbackend.security.jwt.JwtUtils;
import org.thoughtlabs.blogbackend.security.services.RefreshTokenService;
import org.thoughtlabs.blogbackend.util.CookieUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.thoughtlabs.blogbackend.security.services.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private JwtUtils jwtUtils;

    private RefreshTokenService refreshTokenService;

    private AppProperties appProperties;

    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    OAuth2AuthenticationSuccessHandler(
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            AppProperties appProperties,
            HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository
    ){
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.appProperties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String accessToken = jwtUtils.generateTokenFromOAuth2Username(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication);

        CookieUtils.addCookie(response, "access_token", accessToken, 60 * 60 * 24);
        CookieUtils.addCookie(response, "refresh_token", refreshToken.getToken(), 60 * 60 * 24 * 7);

        String targetUrl = determineTargetUrl(request, response, authentication);
        clearAuthenticationAttributes(request, response);

        if(response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            try {
                throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        return redirectUri.orElse("http://localhost:3000");
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getoAuth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedUri = URI.create(authorizedRedirectUri);
                    if(authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authorizedUri.getPort() == clientRedirectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }
}
