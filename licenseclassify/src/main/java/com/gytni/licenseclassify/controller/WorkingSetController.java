package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gytni.licenseclassify.model.CSVUploadPattern;
import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.model.WorkingSet;
import com.gytni.licenseclassify.repo.ProductPatternRepo;
import com.gytni.licenseclassify.repo.WorkingSetRepo;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/working-set")    
public class WorkingSetController {
    @Autowired
    private WorkingSetRepo workingSetRepo;
    @Autowired
    private ProductPatternRepo productPatternRepo;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @GetMapping("")
    private ResponseEntity<List<WorkingSet>> getMethodName() {
        Iterable<WorkingSet> wsIter = workingSetRepo.findAll();
        List<WorkingSet> wss = new ArrayList<>();
        wsIter.forEach(wss::add);
        
        if (wss.isEmpty())
            return ResponseEntity.noContent().build();
        else{
            for (WorkingSet ws : wss) 
                ws.setName( (int)(getClassifyPerc(ws.getId()) * 100) + "%\t" + ws.getName());
            return ResponseEntity.ok(wss);
        }
    }

    @PostMapping("")
    private ResponseEntity<UUID> saveWorkingSet(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) {
        log.info("saveWorkingSet 호출");
        if (file.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try (InputStreamReader reader = getEncodedReader(file)) {
            if (reader == null) throw new IOException("Failed to create InputStreamReader for the file.");
            
            String fileContentHash = DigestUtils.md5Hex(file.getInputStream());
            UUID existingId = workingSetRepo.findIdByHash(fileContentHash);
            
            if (existingId != null) return ResponseEntity.ok(existingId);
            else {
                WorkingSet ws = new WorkingSet();
                ws.setName(fileName);
                ws.setHash(fileContentHash);
                ws = workingSetRepo.save(ws);

                saveProductPattern(new CsvToBeanBuilder<CSVUploadPattern>(reader).withType(CSVUploadPattern.class).build().parse(), ws);
                ws = workingSetRepo.save(ws);
                return ResponseEntity.ok(ws.getId());
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private InputStreamReader getEncodedReader(MultipartFile file) {
        
        try {
            CharsetDetector cd = new CharsetDetector();
            cd.setText(file.getInputStream().readAllBytes());
            cd.enableInputFilter(true);
            CharsetMatch cm = cd.detect();
            String charset = cm.getName();
            return (charset.equalsIgnoreCase("UTF-8"))
                    ? new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)
                    : new InputStreamReader(file.getInputStream(), charset);
            
        } catch (IOException e) {
            log.error("file charset detector fail {}", e);
        }
        return null;
    }

    private void saveProductPattern(List<CSVUploadPattern> patterns, WorkingSet ws) {
        
        int addNum = 0;
        int ignoreNum = 0;

        try {
            for (CSVUploadPattern pattern : patterns) {
                if (pattern.getProductName() != null && pattern.getPublisher() != null) {
                    ProductPattern pp = new ProductPattern();
                    pp.setPatterns(jsonMapper.writeValueAsString(pattern));
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

    private double getClassifyPerc(UUID workingSetId) {
        List<ProductPattern> patterns = productPatternRepo.findByWorkingSetId(workingSetId);
        long unclassifiedFalseCount = patterns.stream().filter(pattern -> !pattern.isUnclassified()).count();
        return (double) unclassifiedFalseCount / patterns.size();
    }

}
