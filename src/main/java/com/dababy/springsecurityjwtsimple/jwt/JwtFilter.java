package com.dababy.springsecurityjwtsimple.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class JwtFilter extends GenericFilterBean {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);


    private static final String AUTHORIZATION_HEADER = "Authorization";
    public TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    /*
    GenericFilterBean 을 extends 해서 doFilter를 Override,
    실제 필터링 로직은 doFilter 내부에 작성한다.

    doFilter의 역할은 jwt 토큰 인증 정보를 현재 실행 중인 Security Context에 저장하는 일을 수행하는 것이다.
     */

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 1. resolveToken 메소드를 통해 request에서 토큰을 받아와서 유효성 검증을 한다. (tokenProvider class에 만들어 두었던 유효성 검증 메소드를 통과한다. )
        // 2. 정상 토큰이면 Security Context에 저장.

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }


    // Request Header에서 토큰 정보를 꺼내오기 위한 resolveToken 메소드를 추가
    // 필터링을 하기 위해서는 토큰 정보가 있어야 한다. 이 메소드를 통해 doFilter에서 유효성 검증을 한다.

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}
