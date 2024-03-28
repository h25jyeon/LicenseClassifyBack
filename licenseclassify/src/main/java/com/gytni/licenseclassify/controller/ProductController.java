package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.checkerframework.checker.index.qual.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gytni.licenseclassify.Type.LicenseType;
import com.gytni.licenseclassify.model.PageDto;
import com.gytni.licenseclassify.model.PageInfo;
import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.repo.ProductPatternRepo;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/product-pattern")
public class ProductController {
    @Autowired
    private ProductPatternRepo productPatternRepo;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String[] headerRecord = {"ProductName", "Publisher", "exceptions", "FastText", "Llm", "LicenseType", "Evidences"};
    
    @GetMapping("")
    private ResponseEntity<List<ProductPattern>> getProductPatterns(@RequestParam(required = false) Boolean unclassified) {
        List<ProductPattern> pps = new ArrayList<>();
        
        if (unclassified != null) {
            PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("created").ascending().and(Sort.by("patterns").ascending()));
            pps = productPatternRepo.findByUnclassified(unclassified, pageRequest).getContent();
        }
        else 
            productPatternRepo.findAll().forEach(pps::add);

        return (pps.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(pps);
    }

    @PostMapping("")
    private ResponseEntity<String> updateLicenseTypeByAi(@RequestBody ProductPattern data) {
        log.info("updateLicenseTypeByAi Strart : {} ", data);
        
        ProductPattern pp = GetProductPatternFromRepo(data);
        if (pp != null) {
            pp.setExceptions(data.isExceptions());   
            pp.setFastText(data.getFastText());
            pp.setLlm(data.getLlm());
            pp.setEvidences(data.getEvidences());
            pp.setUnclassified(false);
            productPatternRepo.save(pp);
            log.info("updateLicenseTypeByAi 성공 : {}", pp.toString());
            return ResponseEntity.ok("success");
        }
        log.error("updateLicenseTypeByAi 실패");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exception")
    private ResponseEntity<String> updateIsException(@RequestBody ProductPattern data) {
        log.info("update exception Strart : {} ", data.getId());

        ProductPattern pp = GetProductPatternFromRepo(data);
        if (pp != null) {
            pp.setExceptions(data.isExceptions());   
            if (data.isExceptions()) pp.setUnclassified(false);
            productPatternRepo.save(pp);
            log.info("update exception 성공 : {}", pp.toString());
            return ResponseEntity.ok("success");
        }
        log.error("update exception 실패");
        return ResponseEntity.noContent().build();
    }

   @GetMapping("/{id}")
    private ResponseEntity<PageDto<ProductPattern>> GetProductPatternByWsId(
        @PathVariable UUID id, 
        @Positive @RequestParam int page, 
        @Positive @RequestParam int size,
        @RequestParam(required = false, defaultValue = "false") boolean classifiedOnly) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("patterns").ascending());

        Page<ProductPattern> productPatternPage = (classifiedOnly) ? productPatternRepo.findByWorkingSetIdAndUnclassifiedFalse(id, pageRequest) 
                                                                   : productPatternRepo.findByWorkingSetIdOrderByCreatedDesc(id, pageRequest);

        if (productPatternPage.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        PageInfo pageInfo = new PageInfo(
            productPatternPage.getNumber(), 
            productPatternPage.getSize(), 
            productPatternPage.getTotalElements(), 
            productPatternPage.getTotalPages(), 
            productPatternPage.isFirst(), 
            productPatternPage.isLast()
        );

        PageDto<ProductPattern> pageDto = new PageDto<>(productPatternPage.getContent(), pageInfo);

        return new ResponseEntity<>(pageDto, HttpStatus.OK);
    }
    
    @PutMapping("/{id}")
    private ResponseEntity<String> updateLicenseType(@PathVariable UUID id, @RequestParam("newOption") String newType ) {
        Optional<ProductPattern> opp =  productPatternRepo.findById(id);
        if (opp.isPresent()) {
            ProductPattern pp = opp.get();
            pp.setLicenseType(LicenseType.find(newType));
            productPatternRepo.save(pp);
            return ResponseEntity.ok(newType);
        }
        else
            return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    private ResponseEntity<byte[]> ExportDataByWsId(@PathVariable UUID id) {
        
        List<ProductPattern> pps = productPatternRepo.findByWorkingSetId(id);
        if (!pps.isEmpty()) {
            try (StringWriter sw = new StringWriter(); 
                CSVWriter writer = new CSVWriter(sw)) {
                
                writer.writeNext(headerRecord);
                
                pps.stream()
                    .map(this::convertToCsvFormat)
                    .map(data -> data.toArray(new String[0]))
                    .forEach(writer::writeNext);

                byte[] csvByte = sw.toString().getBytes("UTF-8");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "example.csv");
                headers.setContentLength(csvByte.length);

                return new ResponseEntity<>(csvByte, headers, HttpStatus.OK);

            } catch (IOException e) {
                log.error("Failed to write CSV data. Error message: {}", e.getMessage(), e);
            }
        }
        return ResponseEntity.noContent().build();
    }

    private List<String> convertToCsvFormat(ProductPattern pp) {
        
        List<String> data = new ArrayList<>();
        try {
            JsonNode pNode = objectMapper.readTree((pp.getPatterns() != null) ? pp.getPatterns() : "");
            JsonNode eviNode = objectMapper.readTree((pp.getEvidences() != null) ? pp.getEvidences() : "");
            data.add(pNode.path("productName").asText()); 
            data.add(pNode.path("publisher").asText());
            data.add(pp.isExceptions() ? "Yes" : "No" );
            data.add((pp.getFastText() != null) ? pp.getFastText().toString() : "");
            data.add((pp.getLlm() != null) ? pp.getLlm().toString() : "");
            data.add((pp.getLicenseType() != null) ? pp.getLicenseType().toString() : "");
            eviNode.forEach(node -> data.add(node.path("url").asText()));

        } catch (IOException e) {
            log.error("Failed to parse JSON data for ProductPattern. Error message: {}", e.getMessage());
        }
        return data;
    }

    private ProductPattern GetProductPatternFromRepo(ProductPattern data) {
        ProductPattern pp = null;

        if (data != null) {
            Optional<ProductPattern> opp = productPatternRepo.findById(data.getId());
            if (!opp.isEmpty()) {
                pp = opp.get();
            }
        }
        return pp;
    }

}
