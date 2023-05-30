package com.antifraud.system.configuration;

import com.antifraud.system.service.UserDetailsServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final UserDetailsServiceImplementation UserDetailsServiceImplementation;

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.httpBasic()
        //.authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
        .and()
        .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
        .and()
        .authorizeRequests() // manage access
        .antMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
        .antMatchers("/actuator/shutdown").permitAll() // needs to run test
        // other matchers
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(UserDetailsServiceImplementation)
        .passwordEncoder(getEncoder());
    auth
        .inMemoryAuthentication() // user store 2
        .withUser("Admin").password("hardcoded").roles("USER")
        .and().passwordEncoder(NoOpPasswordEncoder.getInstance());
  }

  @Bean
  public PasswordEncoder getEncoder() {
    return new BCryptPasswordEncoder();
  }
}
