package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonRepository
        extends JpaRepository<Person, Long>,
        JpaSpecificationExecutor<Person> {

    // tänne hakuja esim
    // List<Person> findByLastNameContainingIgnoreCase(String lastName);

}
