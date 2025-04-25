package com.example.application.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.example.application.views.login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    /* ----------------------------------------------------------
     * 1) Demo-käyttäjät in-memory
     * -------------------------------------------------------- */
    @Bean
    public UserDetailsService userDetailsService() {
        var enc = passwordEncoder();
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(enc.encode("user")).roles("USER").build(),
                User.withUsername("admin").password(enc.encode("admin")).roles("ADMIN").build()
        );
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* ----------------------------------------------------------
     * 2) Suojaussäännöt
     * -------------------------------------------------------- */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 1) julkiset polut
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/",              // <-- root
                        "/login",         // login-sivu
                        "/images/**")     // mahdolliset kuvat
                .permitAll()
        );

        // 2) Vaadinin oletus-konfig (lisää anyRequest().authenticated())
        super.configure(http);

        // 3) lomake-login + logout
        http.formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/", true))   // palaudu Homeen
                .logout(Customizer.withDefaults());

        setLoginView(http, LoginView.class);
    }

    /* ----------------------------------------------------------
     * 3) Tarjoa AuthenticationContext muille beaneille
     * -------------------------------------------------------- */
    @Bean
    public AuthenticationContext authContext() {
        return new AuthenticationContext();
    }
}
