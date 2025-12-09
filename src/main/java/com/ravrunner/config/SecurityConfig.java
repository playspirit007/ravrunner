package com.ravrunner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // statische Dateien immer erlauben
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Login-Seite darf jeder sehen
                .requestMatchers("/login").permitAll()
                // alles andere nur nach Login
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")              // deine login.html
                .loginProcessingUrl("/login")     // POST-Ziel des Formulars
                .defaultSuccessUrl("/", true)     // nach Login auf Startseite
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")   // nach Logout zurück zur Login-Seite
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder() // Demo solange es Lokal läuft
                .username("ravuser")
                .password("ravpass")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
