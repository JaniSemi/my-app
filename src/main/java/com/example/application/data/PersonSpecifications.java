package com.example.application.data;

import org.springframework.data.jpa.domain.Specification;

/** Yksinkertaiset Specification-rakentajat henkilöhakuihin. */
public class PersonSpecifications {

    /** case-insensitive contains -haku sukunimeen */
    public static Specification<Person> lastNameContains(String lastName) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%");
    }

    /** tarkka vertailu gender-kenttään (M / F / U) */
    public static Specification<Person> genderEquals(String gender) {
        return (root, query, cb) ->
                cb.equal(root.get("gender"), gender.toUpperCase());
    }
}
