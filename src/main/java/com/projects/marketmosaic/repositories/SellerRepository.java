package com.projects.marketmosaic.repositories;

import com.projects.marketmosaic.entity.SellerEntity;
import com.projects.marketmosaic.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<SellerEntity, Long> {
    List<SellerEntity> findBySellerStatus(SellerStatus status);
}
