package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.checkerframework.checker.index.qual.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gytni.licenseclassify.Type.LicenseType;
import com.gytni.licenseclassify.model.PageDto;
import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.repo.ProductPatternRepo;
import com.gytni.licenseclassify.repo.WorkingSetRepo;
import com.gytni.licenseclassify.service.ProductPatternService;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/product-pattern")
public class ProductController {
    @Autowired
    private ProductPatternRepo productPatternRepo;
    @Autowired
    private ProductPatternService productPatternService;
    @Autowired
    private WorkingSetRepo workingSetRepo;


    private String[] headerRecord = {"ProductName", "Publisher", "exceptionType", "FastText", "Llm", "LicenseType", "Evidences"};
    
    @GetMapping("")
    private ResponseEntity<PageDto<ProductPattern>> getProductPatterns(@RequestParam(required = false) Boolean unclassified,
                                                                        @RequestParam(required = false, defaultValue = "1")  @Positive int page,
                                                                        @RequestParam(required = false, defaultValue = "100") @Positive int size) {
        
        List<ProductPattern> pps = new ArrayList<>();
        if (unclassified != null) 
            pps = productPatternRepo.findByUnclassified(unclassified);
        else 
            productPatternRepo.findAll().forEach(pps::add);

        PageDto<ProductPattern> pageDto = productPatternService.convertToPageDto(pps, page, size);
        return (pageDto == null) ? ResponseEntity.noContent().build() : new ResponseEntity<>(pageDto, HttpStatus.OK);
    }

    @PostMapping("")
    private ResponseEntity<String> updateLicenseTypeByAi(@RequestBody ProductPattern data) {
        log.info("updateLicenseTypeByAi Strart : {} ", data);
        
        ProductPattern pp = productPatternService.getProductPatternFromRepo(data);
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

        ProductPattern pp = productPatternService.getProductPatternFromRepo(data);
        if (pp != null) {
            pp.setExceptionType(data.getExceptionType());
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
        @RequestParam(required = false, defaultValue = "false") boolean classified,
        @RequestParam(required = false, defaultValue = "false") boolean reviewNeeded,
        @RequestParam(required = false, defaultValue = "false") boolean isException) {
        
        List<ProductPattern> filteredPatterns = classified ? productPatternRepo.findByWorkingSetIdAndUnclassifiedFalse(id) : productPatternRepo.findByWorkingSetIdOrderByCreatedDesc(id);
        
        if (reviewNeeded) {
            filteredPatterns = filteredPatterns.stream()
                .filter(pattern -> pattern.getLicenseType() == LicenseType.NONE || pattern.getLicenseType() == null)
                .collect(Collectors.toList());
        }
        
        if (isException) {
            filteredPatterns = filteredPatterns.stream()
                .filter(pattern -> !pattern.isExceptions())
                .collect(Collectors.toList());
        }
    
        filteredPatterns.sort(Comparator.comparing(ProductPattern::getPatterns)); 

        PageDto<ProductPattern> pageDto = productPatternService.convertToPageDto(filteredPatterns, page, size);
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
                    .map(productPatternService::convertToCsvFormat)
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

    @DeleteMapping("/{id}")
    private ResponseEntity<String> deleteProductPatternById(@PathVariable UUID id) {
        try {
            if (id != null) {
                UUID wsId = productPatternRepo.findById(id).get().getWorkingSetId();
                productPatternRepo.deleteById(id);   
                if (productPatternRepo.findByWorkingSetId(wsId).size() < 1) 
                    workingSetRepo.deleteById(wsId);
                return ResponseEntity.noContent().build(); 
            }
            return ResponseEntity.notFound().build(); 
        } catch (Exception e) {
            log.error("ID : {} 삭제 중 오류 ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중에 오류가 발생했습니다."); 
        }
    }

    
}
