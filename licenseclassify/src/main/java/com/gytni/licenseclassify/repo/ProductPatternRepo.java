package com.gytni.licenseclassify.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gytni.licenseclassify.model.ProductPattern;

import jakarta.transaction.Transactional;

public interface ProductPatternRepo extends JpaRepository<ProductPattern, UUID>, JpaSpecificationExecutor<ProductPattern> {
    List<ProductPattern> findByWorkingSetId(UUID id);
    List<ProductPattern> findByUnclassifiedOrderByCreatedAsc(boolean unclassified);
    List<ProductPattern> findByWorkingSetIdOrderByCreatedDesc(UUID workingSetId);
    List<ProductPattern> findByWorkingSetIdAndUnclassifiedFalse(UUID workingSetId);

    Page<ProductPattern> findByWorkingSetId(UUID id, Pageable pageable);
    Page<ProductPattern> findByUnclassified(boolean unclassified, Pageable pageable);
    Page<ProductPattern> findByUnclassifiedAndWorkingSetId(Boolean unclassified, UUID workingSetId, Pageable pageable);
    Page<ProductPattern> findAll(Pageable pageable);

    long countByWorkingSetIdAndUnclassifiedFalse(UUID workingSetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductPattern p WHERE p.workingSetId = :workingSetId")
    void deleteByWorkingSetId(@Param("workingSetId") UUID workingSetId);
}
