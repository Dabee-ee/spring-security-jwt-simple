package com.dababy.springsecurityjwtsimple.config;

import com.dababy.springsecurityjwtsimple.jwt.JwtAccessDeniedHandler;
import com.dababy.springsecurityjwtsimple.jwt.JwtAuthenticationEntryPoint;
import com.dababy.springsecurityjwtsimple.jwt.JwtSecurityConfig;
import com.dababy.springsecurityjwtsimple.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity // 기본적인 Web 보안을 활성화 하겠다는 의미
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            TokenProvider tokenProvider,
            CorsFilter corsFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/h2-console/**", "/favicon.ico", "/error");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                // token을 사용하는 방식이기 때문에 csrf disable
                .csrf().disable()

                // add cors filter
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                // add exception handling
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                .accessDeniedHandler(jwtAccessDeniedHandler); // 403

        // h2-console
        http
                .headers()
                .frameOptions()
                .sameOrigin();

        // session > stateless
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .authorizeRequests()
                .mvcMatchers("/api/hello").permitAll()
                .anyRequest().authenticated();

        // add jwt security config using token provider
        http
                .apply(new JwtSecurityConfig(tokenProvider)); // create token , get authentication, validation
    }
}
