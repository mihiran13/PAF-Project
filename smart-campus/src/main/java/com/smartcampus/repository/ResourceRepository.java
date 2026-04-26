package com.smartcampus.repository;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findByStatus(ResourceStatus status, Pageable pageable);

    Page<Resource> findByType(ResourceType type, Pageable pageable);

    Page<Resource> findByTypeAndStatus(ResourceType type, ResourceStatus status, Pageable pageable);

    Page<Resource> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    Page<Resource> findByCapacityGreaterThanEqual(Integer capacity, Pageable pageable);

    Page<Resource> findByCapacityLessThanEqual(Integer capacity, Pageable pageable);

    List<Resource> findByStatusAndType(ResourceStatus status, ResourceType type);

    // Advanced Dynamic Search
    @Query("SELECT r FROM Resource r WHERE " +
            "(:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            +
            "AND (:type IS NULL OR r.type = :type) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    Page<Resource> findAdvanced(
            @Param("keyword") String keyword,
            @Param("type") ResourceType type,
            @Param("status") ResourceStatus status,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable);
}
