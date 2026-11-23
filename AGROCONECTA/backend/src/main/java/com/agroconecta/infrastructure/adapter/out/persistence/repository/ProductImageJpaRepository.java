package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {

    List<ProductImageJpaEntity> findByProductIdOrderByPositionAscIdAsc(Long productId);

    List<ProductImageJpaEntity> findByProductIdInOrderByPositionAscIdAsc(Collection<Long> productIds);

    long countByProductId(Long productId);
}
