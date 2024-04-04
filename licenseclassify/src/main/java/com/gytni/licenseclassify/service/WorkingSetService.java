package com.gytni.licenseclassify.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.repo.ProductPatternRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkingSetService {

    @Autowired
    private ProductPatternRepo productPatternRepo;

    public InputStreamReader getEncodedReader(MultipartFile file) {
        
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

    public double getClassifyPerc(UUID workingSetId) {
        List<ProductPattern> patterns = productPatternRepo.findByWorkingSetId(workingSetId);
        long unclassifiedFalseCount = patterns.stream().filter(pattern -> !pattern.isUnclassified()).count();
        return (double) unclassifiedFalseCount / patterns.size();
    }
}
