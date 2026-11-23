package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.domain.model.CropStatus;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.CropJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface CropJpaRepository extends JpaRepository<CropJpaEntity, Long> {

    Page<CropJpaEntity> findByParcelId(Long parcelId, Pageable pageable);

    @Query("""
            select c from CropJpaEntity c
            where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            """)
    Page<CropJpaEntity> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("""
            select count(c) from CropJpaEntity c
            where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
              and c.status in :statuses
            """)
    long countByOwnerIdAndStatusIn(@Param("ownerId") Long ownerId,
                                   @Param("statuses") Collection<CropStatus> statuses);

    @Query("""
            select (count(c) > 0) from CropJpaEntity c
            where c.id = :cropId
              and c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            """)
    boolean existsByIdAndOwnerId(@Param("cropId") Long cropId, @Param("ownerId") Long ownerId);
}
