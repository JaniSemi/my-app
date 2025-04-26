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

    /** Demo-käyttäjät */
    @Bean
    public UserDetailsService userDetailsService() {
        var encoder = passwordEncoder();
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(encoder.encode("user")).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build()
        );
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1) Julkiset reitit ennen Vaadinin anyRequest()
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/images/**").permitAll()
        );

        // 2) Vaadinin omat turvatoimet (CSRF, staattiset resurssit, anyRequest().authenticated())
        super.configure(http);

        // 3) Login/Logout
        http.formLogin(form -> form
                        .loginPage("/login")
                )
                .logout(Customizer.withDefaults());

        // 4) Määritä LoginView
        setLoginView(http, LoginView.class);
    }

    @Bean
    public AuthenticationContext authContext() {
        return new AuthenticationContext();
    }
}
