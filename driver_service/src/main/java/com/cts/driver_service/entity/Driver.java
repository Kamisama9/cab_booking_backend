package com.cts.driver_service.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String aadhaarNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String dlNumber;

    private LocalDate dlExpiryDate;

    private String aadhaarFrontPath;
    private String aadhaarBackPath;
    private String dlFrontPath;
    private String dlBackPath;
    private String selfiePath;
    private String addressProofPath;

    private boolean docsSubmitted = false;
    private boolean isAvailable = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public enum VerificationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED
    }
}
