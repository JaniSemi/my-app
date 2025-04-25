package com.example.application;

import com.example.application.data.PersonRepository;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@Theme(value = "vaadinharjoitus")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // alustaa tietokannan (data.sql) vain jos Person-taulu on tyhjä
    // estää tuplasisällön kun sovellus käynnistyy useamman kerran
    @Bean
    SqlDataSourceScriptDatabaseInitializer initializer(
            DataSource dataSource,
            SqlInitializationProperties props,
            PersonRepository personRepository) {

        return new SqlDataSourceScriptDatabaseInitializer(dataSource, props) {
            @Override
            public boolean initializeDatabase() {
                // Ajetaan data.sql vain jos kantaan ei ole vielä henkilöitä
                return personRepository.count() == 0 && super.initializeDatabase();
            }
        };
    }

}
