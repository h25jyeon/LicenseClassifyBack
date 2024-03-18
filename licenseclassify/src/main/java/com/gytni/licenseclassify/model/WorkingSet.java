package com.gytni.licenseclassify.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class WorkingSet {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(length = 100)
    private String name;

    private int added;
    private int ignored;

    @Column(length = 300)
    private String filepath;

    @Column(length = 32)
    private String hash;

    @CreationTimestamp
    private LocalDateTime created;
    
    @UpdateTimestamp
    private LocalDateTime modified;

    @Column(length = 150)
    private String createdBy;

    @Column(length = 150)
    private String modifiedBy;
}
