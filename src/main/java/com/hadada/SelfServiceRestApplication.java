package com.hadada;

import com.hadada.security.JWTAuthorizationFilter;
import com.hadada.security.JwtFilter;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@SpringBootApplication
@EnableEncryptableProperties
public class SelfServiceRestApplication {

    public static String[] PERMIT_URLS = {"/hadada/create-organization", "/hadada/generateOtp/*", "/hadada/validateOtp", "/hadada/create-user", "/hadada/get-organization/*", "/hadada/login", "/hadada/get-health", "/hadada/resetPassword", "/hadada/save-kyc/*"};

    @Autowired
    private JwtFilter filter;

    public static void main(String[] args) {
        SpringApplication.run(SelfServiceRestApplication.class, args);
    }


    @EnableWebSecurity
    @Configuration
    class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.csrf().disable().cors().configurationSource(configurationSource()).and()
                    .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests()
                    .antMatchers(PERMIT_URLS).permitAll()
                    .anyRequest().authenticated();
        }

        private CorsConfigurationSource configurationSource() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedOrigin("*");
            config.setAllowCredentials(true);
            config.addAllowedHeader("Content-Type");
            config.addAllowedMethod(HttpMethod.POST);
            config.addAllowedMethod(HttpMethod.GET);
            config.addAllowedMethod(HttpMethod.OPTIONS);
            source.registerCorsConfiguration("/**", config);
            return source;
        }

    }
}
