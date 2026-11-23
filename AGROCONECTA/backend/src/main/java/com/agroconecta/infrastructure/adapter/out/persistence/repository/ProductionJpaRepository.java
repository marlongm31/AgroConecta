package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionJpaRepository extends JpaRepository<ProductionJpaEntity, Long> {

    @Query("""
            select r from ProductionJpaEntity r
            where r.cropId in (
                select c.id from CropJpaEntity c
                where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            )
            """)
    Page<ProductionJpaEntity> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("""
            select count(r) from ProductionJpaEntity r
            where r.cropId in (
                select c.id from CropJpaEntity c
                where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            )
            """)
    long countByOwnerId(@Param("ownerId") Long ownerId);
}
