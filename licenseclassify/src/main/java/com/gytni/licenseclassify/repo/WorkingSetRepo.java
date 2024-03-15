package com.gytni.licenseclassify.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gytni.licenseclassify.model.WorkingSet;

public interface WorkingSetRepo extends CrudRepository<WorkingSet, UUID> {
    @Query("SELECT w.id FROM WorkingSet w WHERE w.hash = :hashValue")
    UUID findIdByHash(@Param("hashValue") String hashValue);
}
