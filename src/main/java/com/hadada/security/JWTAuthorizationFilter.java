package com.hadada.security;

import com.hadada.SelfServiceRestApplication;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import com.hadada.exception.CustomException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.net.URLDecoder;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    public final static String HEADER = "HCSession";
    private final String PREFIX = "Bearer ";
    private final String SECRET = "mySecretKey";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean urlMatch = false;
        for (String url: SelfServiceRestApplication.PERMIT_URLS)
        {
            url = url.replace("*", "");
            if(path.contains(url)) {
                urlMatch = true;
                break;
            }

        }
        if (urlMatch) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (checkJWTToken(request, response)) {
                Claims claims = validateToken(request);
                if (claims.get("authorities") != null) {
                    setUpSpringAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }else {
                SecurityContextHolder.clearContext();
            }

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            SecurityContextHolder.clearContext();

        }
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            // log error if needed here then redirect
           throw new CustomException();
        }

    }

    private Claims validateToken(HttpServletRequest request) {
        String jwtToken = this.getToken(request).replace(PREFIX, "");
        return Jwts.parser().setSigningKey(SECRET.getBytes()).parseClaimsJws(jwtToken).getBody();
    }

    /**
     * Authentication method in Spring flow
     *
     * @param claims
     */
    private void setUpSpringAuthentication(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> authorities = (List) claims.get("authorities");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);

    }

    private boolean checkJWTToken(HttpServletRequest request, HttpServletResponse res) {
        String authenticationHeader = this.getToken(request);
        if (authenticationHeader == null || !authenticationHeader.startsWith(PREFIX))
            return false;
        return true;
    }

    private String getToken(HttpServletRequest request){
        String jwtToken = "";
        Cookie cookies[] = request.getCookies();
        if (cookies != null && cookies.length != 0) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie c = cookies[i];
                if(c.getName().equalsIgnoreCase(HEADER)){
                    try{
                        jwtToken = URLDecoder.decode(c.getValue(),"UTF-8");
                    }catch (Exception e) {
                        jwtToken = c.getValue();
                    }
                }
            }
        }
        return jwtToken;
    }

}