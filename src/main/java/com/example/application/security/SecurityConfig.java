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

    /**
     * Configure in-memory users for demo purposes
     */
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

    /**
     * Configure security rules
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public paths
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/",         // root
                        "/login",             // login page
                        "/images/**")         // images
                .permitAll()
        );

        // Default Vaadin config (adds anyRequest().authenticated())
        super.configure(http);

        // Form login and logout config
        http.formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/", true))   // return to Home after login
                .logout(Customizer.withDefaults());

        setLoginView(http, LoginView.class);
    }

    /**
     * Provide AuthenticationContext to other beans
     */
    @Bean
    public AuthenticationContext authContext() {
        return new AuthenticationContext();
    }
}