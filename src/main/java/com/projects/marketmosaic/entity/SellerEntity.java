package com.projects.marketmosaic.entity;

import com.projects.marketmosaic.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "marketmosaic_sellers")
@Data
public class SellerEntity {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status")
    private SellerStatus sellerStatus = SellerStatus.NONE;
}