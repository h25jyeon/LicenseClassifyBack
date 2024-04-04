package com.gytni.licenseclassify.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gytni.licenseclassify.Type.ExceptionType;
import com.gytni.licenseclassify.model.ExceptionKeyword;

public interface ExceptionKeywordRepo extends CrudRepository<ExceptionKeyword, UUID> {
    
    List<ExceptionKeyword> findByPublisher(String publisher);

    List<ExceptionKeyword> findByProduct(String product);

    List<ExceptionKeyword> findByTypeAndProductAndPublisher( ExceptionType type, String product, String publisher);

    List<ExceptionKeyword> findByTypeAndPublisher(ExceptionType type, String publisher);

    @Query("SELECT ek FROM ExceptionKeyword ek WHERE ek.type = :type AND :searchTerm LIKE CONCAT('%', ek.publisher, '%')")
    List<ExceptionKeyword> findByTypeAndSearchTermContainingPublisher(String searchTerm, ExceptionType type);


    @Query("SELECT ek FROM ExceptionKeyword ek WHERE  ek.type = :type AND :searchTerm LIKE CONCAT('%', ek.product, '%')")
    List<ExceptionKeyword> findByTypeAndSearchTermContainingProduct(String searchTerm, ExceptionType type);

}
