package com.gytni.licenseclassify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gytni.licenseclassify.Type.ExceptionType;
import com.gytni.licenseclassify.model.ExceptionKeyword;
import com.gytni.licenseclassify.repo.ExceptionKeywordRepo;

@Service
public class ExceptionKeywordService {
    
    @Autowired
    private ExceptionKeywordRepo exceptionKeywordRepo;

    public boolean isKeywordExists(String productName, String publisher, ExceptionType type) {
        return exceptionKeywordRepo.findByTypeAndProductAndPublisher(type,productName,publisher).size() > 0 ? true : false;
    }

    public String checkIsException(String productName, String publisher) {

        List<ExceptionKeyword> exactMatchKeywords = exceptionKeywordRepo.findByTypeAndProductAndPublisher(ExceptionType.PUBLISHER_PRODUCT_EXACT_MATCH, productName, publisher);
        if (!exactMatchKeywords.isEmpty())          return ExceptionType.PUBLISHER_PRODUCT_EXACT_MATCH.getLabel();

        List<ExceptionKeyword> publisherExactMatchKeywords = exceptionKeywordRepo.findByTypeAndPublisher(ExceptionType.PUBLISHER_EXACT_MATCH, publisher);
        if (!publisherExactMatchKeywords.isEmpty()) return ExceptionType.PUBLISHER_EXACT_MATCH.getLabel();

        List<ExceptionKeyword> publisherMatchKeywords = exceptionKeywordRepo.findByTypeAndSearchTermContainingPublisher(publisher, ExceptionType.PUBLISHER_MATCH);
        if (!publisherMatchKeywords.isEmpty())      return ExceptionType.PUBLISHER_MATCH.getLabel();

        List<ExceptionKeyword> productMatchKeywords = exceptionKeywordRepo.findByTypeAndSearchTermContainingProduct(productName, ExceptionType.PRODUCT_MATCH);
        if (!productMatchKeywords.isEmpty())        return ExceptionType.PRODUCT_MATCH.getLabel();

        return ExceptionType.ETC.getLabel();
    }

}