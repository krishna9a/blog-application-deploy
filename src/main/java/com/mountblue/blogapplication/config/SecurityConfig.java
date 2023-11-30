package com.mountblue.blogapplication.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private DataSource dataSource;
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService getDetailsService(){
        return new CustomUserDetailsService();
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(getDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf->csrf.disable())
                .authenticationProvider(getAuthenticationProvider());
        http.authorizeHttpRequests(configure->
                configure.requestMatchers("/update","/submission","/showFormForUpdate",
                                "/updatePost","/showFormForDelete","/comment",
                                "/comments/*","/delete").authenticated()
                        .requestMatchers("/post").authenticated()

                        .anyRequest().permitAll()
        ).formLogin(form-> form.loginPage("/showMyLoginPage")
                .usernameParameter("email")
                .loginProcessingUrl("/authenticateTheUser").permitAll()
        ).logout(logout->logout.permitAll());
        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }

}
