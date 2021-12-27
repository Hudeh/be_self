package com.hadada.security;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hadada.modal.Customer;
import com.hadada.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.ExpiredJwtException;
@Component
public class JwtFilter {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TokenManager tokenManager;

    public boolean checkValidAuthorization(String tokenHeader, String email) {
        try{
            tokenHeader = URLDecoder.decode(tokenHeader,"UTF-8");
        }catch (Exception e) {
            return false;
        }
        boolean isAuthorized = true;
        String username = null;
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
            try {
                username = tokenManager.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                isAuthorized = false;
            } catch (ExpiredJwtException e) {
                isAuthorized = false;
            }
        } else {
            isAuthorized = false;
        }
        List<Customer> customerList = customerRepository.findByUsername(email);
        if (isAuthorized && !(customerList.size() > 0 && tokenManager.validateJwtToken(token, email))) {
            isAuthorized = false;
        }
        return isAuthorized;
    }
}