package com.gytni.licenseclassify.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gytni.licenseclassify.model.ProductPattern;

import jakarta.transaction.Transactional;

public interface ProductPatternRepo extends CrudRepository<ProductPattern, UUID> {
    List<ProductPattern> findByWorkingSetId(UUID id);
    List<ProductPattern> findByUnclassified(boolean unclassified);
    List<ProductPattern> findByWorkingSetIdOrderByCreatedDesc(UUID workingSetId);
    List<ProductPattern> findByWorkingSetIdAndUnclassifiedFalse(UUID workingSetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductPattern p WHERE p.workingSetId = :workingSetId")
    void deleteByWorkingSetId(@Param("workingSetId") UUID workingSetId);
}
