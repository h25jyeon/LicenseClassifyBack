package com.gytni.licenseclassify.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.gytni.licenseclassify.Type.LicenseType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ProductPattern {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    private boolean exceptions;

    @Enumerated(EnumType.STRING)
    private LicenseType fastText;

    @Enumerated(EnumType.STRING)
    private LicenseType llm;

    private String evidences;

    private String patterns;

    @Column(length = 150) // 이름 + 로그인 아이디/이메일
    private String createdBy;

    @Column(length = 150)
    private String modifiedBy;
    
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID workingSetId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "exception_keyword_id")
    private ExceptionKeyword exceptionKeyword;


    @CreationTimestamp
    private LocalDateTime created;
    
    @UpdateTimestamp
    private LocalDateTime modified;

    private boolean unclassified = true;
}

