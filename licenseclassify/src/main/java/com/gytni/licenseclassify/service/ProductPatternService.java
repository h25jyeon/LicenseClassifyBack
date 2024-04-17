package com.gytni.licenseclassify.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gytni.licenseclassify.Type.LicenseType;
import com.gytni.licenseclassify.model.CSVUploadPattern;
import com.gytni.licenseclassify.model.PageDto;
import com.gytni.licenseclassify.model.PageInfo;
import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.model.WorkingSet;
import com.gytni.licenseclassify.repo.ProductPatternRepo;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductPatternService {
    
    @Autowired
    private ProductPatternRepo productPatternRepo;

    @Autowired
    private FastTextProbabilityService fastTextProbabilityService;

    @Autowired
    private ExceptionKeywordService exceptionKeywordService;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private ObjectMapper objectMapper = new ObjectMapper();

    public void saveProductPattern(List<CSVUploadPattern> patterns, WorkingSet ws) {
        
        int addNum = 0; 
        int ignoreNum = 0;

        try {
            for (CSVUploadPattern pattern : patterns) {
                if (pattern.getProductName() != null && pattern.getPublisher() != null) {
                    ProductPattern pp = new ProductPattern();
                    String ft = fastTextProbabilityService.probability(pattern);
                    pp.setFastText(LicenseType.find(ft));
                    pp.setPatterns(jsonMapper.writeValueAsString(pattern));
                    String et = exceptionKeywordService.checkIsException(pattern);
                    pp.setExceptionType(et);
                    pp.setExceptions(!et.equals("N"));
                    pp.setUnclassified(!pp.isExceptions());
                    pp.setWorkingSetId(ws.getId());
                    productPatternRepo.save(pp);
                    addNum++;
                } else 
                    ignoreNum++;
            }
            ws.setAdded(addNum);
            ws.setIgnored(ignoreNum);
        } catch (JsonProcessingException e) {
            log.error("Failed to map pattern to JSON. {}", e);
        }
    }

    public List<String> convertToCsvFormat(ProductPattern pp) {
        
        List<String> data = new ArrayList<>();
        try {
            JsonNode pNode = objectMapper.readTree((pp.getPatterns() != null) ? pp.getPatterns() : "");
            JsonNode eviNode = objectMapper.readTree((pp.getEvidences() != null) ? pp.getEvidences() : "");
            data.add(pNode.path("productName").asText()); 
            data.add(pNode.path("publisher").asText());
            data.add(pp.getExceptionType());
            data.add((pp.getFastText() != null) ? pp.getFastText().toString() : "");
            data.add((pp.getLlm() != null) ? pp.getLlm().toString() : "");
            data.add((pp.getLicenseType() != null) ? pp.getLicenseType().toString() : "");
            eviNode.forEach(node -> data.add(node.path("url").asText()));

        } catch (IOException e) {
            log.error("Failed to parse JSON data for ProductPattern. Error message: {}", e.getMessage());
        }
        return data;
    }

    public ProductPattern getProductPatternFromRepo(ProductPattern data) {
        ProductPattern pp = null;

        if (data != null) {
            Optional<ProductPattern> opp = productPatternRepo.findById(data.getId());
            if (!opp.isEmpty()) {
                pp = opp.get();
            }
        }
        return pp;
    }

    public PageDto<ProductPattern> convertToPageDto(List<ProductPattern> productPatterns, int page, int size) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("created").ascending().and(Sort.by("patterns").ascending()));

        int start = Math.min((int) pageRequest.getOffset(), productPatterns.size());
        int end = Math.min((start + pageRequest.getPageSize()), productPatterns.size());
        List<ProductPattern> pageContent = productPatterns.subList(start, end);
    
        PageInfo pageInfo = new PageInfo(
            page, 
            size,
            productPatterns.size(),
            (int) Math.ceil((double) productPatterns.size() / size),
            page == 1, 
            page == (int) Math.ceil((double) productPatterns.size() / size) 
        );
    
        return new PageDto<>(pageContent, pageInfo);
    }

    @Transactional
    public void deleteByWorkingSetId(UUID workingSetId) {
        productPatternRepo.deleteByWorkingSetId(workingSetId);
    }
}
