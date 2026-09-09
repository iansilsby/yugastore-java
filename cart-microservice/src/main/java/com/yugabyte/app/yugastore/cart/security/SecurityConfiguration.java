package com.yugabyte.app.yugastore.cart.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	  @Bean
	  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
	        .httpBasic(basic -> basic.disable())
	        .csrf(csrf -> csrf.disable());
	    return http.build();
	  }
}
