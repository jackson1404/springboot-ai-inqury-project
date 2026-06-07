package com.jack.springaiopenrouter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 50)
    private String tier;

    @Column(nullable = false, length = 80)
    private String region;

    protected CustomerEntity() {
        // Required by JPA
    }

    public CustomerEntity(String id, String name, String email, String tier, String region) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.tier = tier;
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getTier() {
        return tier;
    }

    public String getRegion() {
        return region;
    }
}
