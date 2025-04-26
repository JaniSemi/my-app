package com.example.application.data;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    /**
     * Optimistic locking version.
     * DEFAULT 0 varmistaa, että olemassa olevat rivit saavat arvon 0.
     */
    @Version
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int version = 0;

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractEntity that)) {
            return false;
        }
        if (id != null) {
            return id.equals(that.id);
        }
        return super.equals(obj);
    }
}
