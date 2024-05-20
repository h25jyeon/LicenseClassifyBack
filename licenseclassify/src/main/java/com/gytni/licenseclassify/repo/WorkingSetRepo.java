package com.gytni.licenseclassify.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gytni.licenseclassify.model.WorkingSet;

public interface WorkingSetRepo extends JpaRepository<WorkingSet, UUID> {
    @Query("SELECT w.id FROM WorkingSet w WHERE w.hash = :hashValue")
    UUID findIdByHash(@Param("hashValue") String hashValue);
}
