package site.yemei.photoApp.api.users.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import site.yemei.photoApp.api.users.service.UserService;
import site.yemei.photoApp.api.users.shared.UserDto;
import site.yemei.photoApp.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	private UserService userService;
	private Environment environment;
	
	String token_expiration_time;
	String token_secret;
	
	public AuthenticationFilter(UserService userService, 
			Environment environment,
			AuthenticationManager authenticationManager) {
		
		this.userService = userService;
		this.environment = environment;	
		super.setAuthenticationManager(authenticationManager);
		
		this.token_expiration_time = environment.getProperty("token.expiration_time");
		this.token_secret = environment.getProperty("token.secret");
	}

	@Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
  
            LoginRequestModel creds = new ObjectMapper()
                    .readValue(req.getInputStream(), LoginRequestModel.class);
            
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
	@Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
		
		String userName = ((User) auth.getPrincipal()).getUsername();
		UserDto userDetails = userService.getUserDetailsByEmail(userName);
		
		//String test = environment.getProperty("token.expiration_time");
        
		String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
//                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(environment.getProperty("token.expiration_time"))))
//                .signWith(SignatureAlgorithm.HS512, environment.getProperty("token.secret") )
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(token_expiration_time)))
                .signWith(SignatureAlgorithm.HS512, token_secret)
                .compact();
        
        res.addHeader("token", token);
        res.addHeader("userId", userDetails.getUserId());
		
	}	
}
