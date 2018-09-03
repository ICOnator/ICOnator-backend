package io.iconator.commons.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@Order(100)
public class ActuatorAuthSecurityConfig extends WebSecurityConfigurerAdapter {

    private String username;
    private String password;

    private static final String ROLE = "OPS_ADMIN";

    public ActuatorAuthSecurityConfig(@Value("${io.iconator.commons.auth.actuator.user}") String username, @Value("${io.iconator.commons.auth.actuator.password}") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/actuator/**").hasRole(ROLE)
                .antMatchers("/cloudfoundryapplication/**").hasRole(ROLE)
                .antMatchers("/kyc/**").hasRole(ROLE)
                .antMatchers("/keypairs/**").hasRole(ROLE)
                .antMatchers(HttpMethod.POST, "/tiers/create/**").hasRole(ROLE)
                .and()
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = passwordEncoder();
        auth.inMemoryAuthentication().passwordEncoder(encoder).withUser(username).password(encoder.encode(password)).roles(ROLE);
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
