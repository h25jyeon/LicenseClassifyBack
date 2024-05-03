package com.gytni.licenseclassify.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gytni.licenseclassify.Type.ExceptionType;
import com.gytni.licenseclassify.model.CSVUploadPattern;
import com.gytni.licenseclassify.model.ExceptionKeyword;
import com.gytni.licenseclassify.repo.ExceptionKeywordRepo;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExceptionKeywordService {
    
    @Autowired
    private ExceptionKeywordRepo exceptionKeywordRepo;

    // <<publisher, product>, id>
    private Map<Pair<String, String>, UUID> publisherProductExactKeyword = new HashMap<>();
    private Map<String, UUID> publisherExactKeyword = new HashMap<>();
    private Map<String, UUID> publisherKeyword = new HashMap<>();
    private Map<String, UUID> productKeyword = new HashMap<>();

    @PostConstruct
    private void initialize() {
        log.info("Starting to load exception keywords from database");
        List<ExceptionKeyword> allExactMatchKeywords = exceptionKeywordRepo.findByType(ExceptionType.PUBLISHER_PRODUCT_EXACT_MATCH);
        List<ExceptionKeyword> publisherExactMatchKeywords = exceptionKeywordRepo.findByType(ExceptionType.PUBLISHER_EXACT_MATCH);
        List<ExceptionKeyword> publisherKeywords = exceptionKeywordRepo.findByType(ExceptionType.PUBLISHER_MATCH);
        List<ExceptionKeyword> productKeywords = exceptionKeywordRepo.findByType(ExceptionType.PRODUCT_MATCH);

        Instant start = Instant.now();

        allExactMatchKeywords.forEach(ek -> publisherProductExactKeyword.put(Pair.of(ek.getPublisher(), ek.getProduct()), ek.getId()));
        publisherExactMatchKeywords.forEach(ek -> publisherExactKeyword.put(ek.getPublisher(), ek.getId()));
        publisherKeywords.forEach(ek -> publisherKeyword.put(ek.getPublisher(), ek.getId()));
        productKeywords.forEach(ek -> productKeyword.put(ek.getProduct(), ek.getId()));

        Instant finish = Instant.now(); 
        long timeElapsed = Duration.between(start, finish).toMillis();  
        log.info("Loaded keywords into the set in {} ms.", timeElapsed);
        log.info("Loaded {} PUBLISHER_PRODUCT_EXACT_MATCH keywords", publisherProductExactKeyword.size());
        log.info("Loaded {} PUBLISHER_EXACT_MATCH keywords", publisherExactKeyword.size());
        log.info("Loaded {} PUBLISHER_MATCH keysords", publisherKeyword.size());
        log.info("Loaded {} PRODUCT_MATCH keysords ", productKeyword.size());
    }

    public boolean isKeywordExists(String productName, String publisher, ExceptionType type) {
        return exceptionKeywordRepo.findByTypeAndProductAndPublisher(type,productName,publisher).size() > 0 ? true : false;
    }

    public UUID checkIsException(CSVUploadPattern productPattern) {
        
        String productName = productPattern.getProductName();
        String publisher = productPattern.getPublisher();
        Pair<String, String> key = Pair.of(publisher, productName);
        
        if (publisherProductExactKeyword.containsKey(key)) return publisherProductExactKeyword.get(key);

        if (publisherExactKeyword.containsKey(publisher)) return publisherExactKeyword.get(publisher);

        for ( Entry<String, UUID> ek : publisherKeyword.entrySet()) if (publisher.indexOf(ek.getKey()) > -1) return ek.getValue();

        for ( Entry<String, UUID> ek : publisherKeyword.entrySet()) if (publisher.indexOf(ek.getKey()) > -1) return ek.getValue();   
        
        return null;
    }
    
    public ExceptionKeyword getExceptionKeywordById(UUID id) {
        return id != null ? exceptionKeywordRepo.findById(id).get() : null;
    }

}
