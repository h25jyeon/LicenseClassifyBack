package com.gytni.licenseclassify.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.gytni.licenseclassify.model.ProductPattern;

public interface ProductPatternRepo extends CrudRepository<ProductPattern, UUID> {
    List<ProductPattern> findByWorkingSetId(UUID id);
    Page<ProductPattern> findByUnclassified(boolean unclassified, Pageable pageable);
    Page<ProductPattern> findByWorkingSetIdOrderByCreatedDesc(UUID workingSetId, Pageable pageable);
    Page<ProductPattern> findByWorkingSetIdAndUnclassifiedFalse(UUID workingSetId, Pageable pageable);
}
