package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ActivityJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityJpaRepository extends JpaRepository<ActivityJpaEntity, Long> {

    Page<ActivityJpaEntity> findByCropId(Long cropId, Pageable pageable);

    @Query("""
            select a from ActivityJpaEntity a
            where a.cropId in (
                select c.id from CropJpaEntity c
                where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            )
            """)
    Page<ActivityJpaEntity> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("""
            select (count(a) > 0) from ActivityJpaEntity a
            where a.id = :activityId
              and a.cropId in (
                  select c.id from CropJpaEntity c
                  where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
              )
            """)
    boolean existsByIdAndOwnerId(@Param("activityId") Long activityId, @Param("ownerId") Long ownerId);

    @Query("""
            select count(a) from ActivityJpaEntity a
            where a.cropId in (
                select c.id from CropJpaEntity c
                where c.parcelId in (select p.id from ParcelJpaEntity p where p.ownerId = :ownerId)
            )
            """)
    long countByOwnerId(@Param("ownerId") Long ownerId);
}
