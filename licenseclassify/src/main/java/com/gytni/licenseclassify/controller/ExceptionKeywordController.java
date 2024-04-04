package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gytni.licenseclassify.Type.ExceptionType;
import com.gytni.licenseclassify.model.CSVUploadPattern;
import com.gytni.licenseclassify.model.ExceptionKeyword;
import com.gytni.licenseclassify.repo.ExceptionKeywordRepo;
import com.gytni.licenseclassify.service.ExceptionKeywordService;
import com.gytni.licenseclassify.service.WorkingSetService;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/Exception")    
public class ExceptionKeywordController {

    @Autowired
    private ExceptionKeywordRepo exceptionKeywordRepo;

    @Autowired
    private ExceptionKeywordService exceptionKeywordService;

    @Autowired
    private WorkingSetService workingSetService;

   @PostMapping("")
    private ResponseEntity<Map<String, Integer>> addExceptionKeyword(@RequestParam("file") MultipartFile file, 
                                                                     @RequestParam("type") ExceptionType type) throws CsvValidationException {

        if (file.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        
        Map<String, Integer> response = new HashMap<>();
        int addNum = 0;
        int dupNum = 0;
        
        try (InputStreamReader reader = workingSetService.getEncodedReader(file)) {
            if (reader == null) throw new IOException("Failed to create InputStreamReader for the file");
            
            List<CSVUploadPattern> keywords = new CsvToBeanBuilder<CSVUploadPattern>(reader)
                    .withType(CSVUploadPattern.class).build().parse();
            log.info("keyword num : {} \n exception type : {}", keywords.size(), type);
            
            for (CSVUploadPattern keyword : keywords) {
                if (!exceptionKeywordService.isKeywordExists(keyword.getProductName(),keyword.getPublisher(), type)) {
                    ExceptionKeyword ek = new ExceptionKeyword();
                    ek.setProduct(keyword.getProductName());
                    ek.setPublisher(keyword.getPublisher());
                    ek.setType(type);
                    exceptionKeywordRepo.save(ek);
                    addNum++;
                } else {
                    dupNum++;
                    log.warn("keyword Exists : {}", keyword);
                }
            }
            
            response.put("added", addNum);
            response.put("duplicate", dupNum);
            
        } catch (IOException e) {
            log.error("Failed to read file", e);
        }
        
        return ResponseEntity.ok(response);
    }

}
