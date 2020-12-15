package com.alibaba.arthas.tunnel.server.endpoint;

import com.alibaba.arthas.tunnel.server.app.security.CustomFilterSecurityInterceptor;
import com.alibaba.arthas.tunnel.server.app.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
public class ActuatorSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomFilterSecurityInterceptor customFilterSecurityInterceptor;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/vendors/**",
                        "/**.js",
                        "/**.css",
                        "/**.png"
                )
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/redefine")
                .permitAll()
                .and()
                .logout()
                .permitAll();
        httpSecurity.addFilterBefore(customFilterSecurityInterceptor, FilterSecurityInterceptor.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
}