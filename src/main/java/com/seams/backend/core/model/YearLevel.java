package com.seams.backend.core.model;

import jakarta.persistence.*;

@Entity
public class YearLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String level;

    public YearLevel() {}
    public YearLevel(String level) {
        this.level = level;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
