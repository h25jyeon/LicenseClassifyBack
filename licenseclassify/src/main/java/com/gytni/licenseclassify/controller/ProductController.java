package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.checkerframework.checker.index.qual.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
import com.gytni.licenseclassify.annotation.RemoteIp;
import com.gytni.licenseclassify.dto.ProductPatternDto;
import com.gytni.licenseclassify.model.ProductPattern;
import com.gytni.licenseclassify.model.WorkingSet;
import com.gytni.licenseclassify.repo.ProductPatternRepo;
import com.gytni.licenseclassify.repo.WorkingSetRepo;
import com.gytni.licenseclassify.repo.searcher.SearchBuilder;
import com.gytni.licenseclassify.service.ProductPatternService;
import com.opencsv.CSVWriter;

import jakarta.servlet.http.HttpServletRequest;
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
    private ResponseEntity<Page<ProductPattern>> getProductPatterns(@RequestParam(required = false) Boolean unclassified,
                                                                    @RequestParam(required = false) UUID workingSetId,
                                                                    @RequestParam(required = false, defaultValue = "1") @Positive int page,
                                                                    @RequestParam(required = false, defaultValue = "100") @Positive int size, 
                                                                    HttpServletRequest request) {
        
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("get product patterns Request from IP : {}", clientIp);


        log.info("Get product patterns request received. page : {} size : {}", page, size);
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Order.asc("created")));
    
        if (unclassified != null && workingSetId != null) {
            log.info("Fetching product patterns that are unclassified: {} and belong to working set ID: {}", unclassified, workingSetId);
            return ResponseEntity.ok(productPatternRepo.findByUnclassifiedAndWorkingSetId(unclassified, workingSetId, pageable));
        } else if (unclassified != null) {
            log.info("Fetching product patterns that are unclassified: {}", unclassified);
            return ResponseEntity.ok(productPatternRepo.findByUnclassified(unclassified, pageable));
        } else if (workingSetId != null) {
            log.info("Fetching product patterns that belong to working set ID: {}", workingSetId);
            return ResponseEntity.ok(productPatternRepo.findByWorkingSetId(workingSetId, pageable));
        } else {
            log.info("Fetching all product patterns.");
            return ResponseEntity.ok(productPatternRepo.findAll(pageable));
        }
    }
    

    @PostMapping("")
    private ResponseEntity<String> updateLicenseTypeByAi(@RequestBody ProductPattern data, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("updateLicenseTypeByAi Strart  ID : {}, IP : {} ", data.getId(), clientIp);
        
        ProductPattern pp = productPatternService.getProductPatternFromRepo(data);
        if (pp != null) {
            log.info("llm : {}, evidence len : {}", data.getLlm(), (data.getEvidences() != null) ? data.getEvidences().toString().length() : 0);
            pp.setMdbResults(data.getMdbResults());
            pp.setLlm(data.getLlm());
            pp.setEvidences(data.getEvidences());
            pp.setUnclassified(false);
            productPatternRepo.save(pp);
            log.info("updateLicenseTypeByAi 성공 : id : {}, llm : {}", pp.getId(), pp.getLlm());
            return ResponseEntity.ok("success");
        }
        log.error("updateLicenseTypeByAi 실패");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exception")
    private ResponseEntity<String> updateIsException(@RequestBody ProductPattern data, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("update exception Strart  ID : {}, IP : {}", data.getId(), clientIp);

        ProductPattern pp = productPatternService.getProductPatternFromRepo(data);
        if (pp != null) {
            pp.setMdbResults(data.getMdbResults());
            pp.setExceptionKeyword(data.getExceptionKeyword());
            pp.setExceptions(data.isExceptions());   
            if (data.isExceptions()) pp.setUnclassified(false);
            productPatternRepo.save(pp);
            log.info("update exception 성공 : {}", pp.toString());
            return ResponseEntity.ok("success");
        }
        log.error("update exception 실패");
        return ResponseEntity.noContent().build();
    }

    
    @PostMapping("/mdb")
    private ResponseEntity<ProductPattern> updateMdbResults(@RequestBody ProductPattern data, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("update Mdb Results Strart  ID : {}, IP : {}", data.getId(), clientIp);

        ProductPattern pp = productPatternService.getProductPatternFromRepo(data);
        if (pp != null) {
            pp.setMdbResults(data.getMdbResults());
            productPatternRepo.save(pp);
            log.info("update Mdb Results 성공 : {}", pp.toString());
            return ResponseEntity.ok(pp);
        }
        log.error("update Mdb Results 실패");
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id
     * @param keyword
     * @param page
     * @param size
     * @param classified
     * @param reviewNeeded
     * @param isException
     */
    /* @GetMapping("/{id}")
    private ResponseEntity<ProductPatternDto<ProductPattern>> GetProductPatternByWsId(
        @PathVariable UUID id,
        @RequestParam(required = false) String keyword,
        @Positive @RequestParam int page, 
        @Positive @RequestParam int size,
        @RequestParam(required = false, defaultValue = "false") boolean classified,
        @RequestParam(required = false, defaultValue = "false") boolean reviewNeeded,
        @RequestParam(required = false, defaultValue = "false") boolean isException, 
        HttpServletRequest request,
        @RemoteIp String clientIp) {

        log.info("ProductPattern Request from ws ID : {}, IP : {}", id, clientIp);
        
        List<ProductPattern> filteredPatterns = classified ? productPatternRepo.findByWorkingSetIdAndUnclassifiedFalse(id) : 
                                                             productPatternRepo.findByWorkingSetIdOrderByCreatedDesc(id);
        
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

        if (keyword != null && !keyword.isBlank()) {
            filteredPatterns = filteredPatterns.stream()
                .filter(pattern -> productPatternService.isKeywordPresent(pattern, keyword))
                .collect(Collectors.toList());
        }
    
        filteredPatterns.sort(Comparator.comparing(ProductPattern::getPatterns)); 

        ProductPatternDto<ProductPattern> pageDto = productPatternService.convertToPageDto(filteredPatterns, page, size);
        return new ResponseEntity<>(pageDto, HttpStatus.OK);
    }    
 */
    
 
    @GetMapping("/{id}")
    private ResponseEntity<Page<ProductPattern>> GetProductPatternByWsId (
        @PathVariable UUID id,
        @RequestParam(required = false) String keyword,
        @Positive @RequestParam int page, 
        @Positive @RequestParam int size,
        @RequestParam(required = false, defaultValue = "false") boolean classified,
        @RequestParam(required = false, defaultValue = "false") boolean reviewNeeded,
        @RequestParam(required = false, defaultValue = "false") boolean isException, 
        HttpServletRequest request,
        @RemoteIp String clientIp) {

        log.info("ProductPattern Request2 from ws ID : {}, IP : {}", id, clientIp);

        SearchBuilder<ProductPattern> searchBuilder = SearchBuilder.builder();
        searchBuilder.with("patterns", id, keyword, reviewNeeded, isException, classified);
        
        // ProductName으로 정렬 (patterns 는 productName으로 시작함)
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Order.asc("patterns")));
        Page<ProductPattern> filteredPatterns = productPatternRepo.findAll(searchBuilder.build(), pageable);

        return new ResponseEntity<>(filteredPatterns, HttpStatus.OK);
    }    
    
    @PutMapping("/{id}")
    private ResponseEntity<String> updateLicenseType(@PathVariable UUID id, @RequestParam("newOption") String newType, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("update exception Strart ID : {}, IP : {}", id, clientIp);

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

    @PutMapping("/score/{id}")
    private ResponseEntity<ProductPattern> updateEvidenceScore (@PathVariable UUID id, 
                                                               @RequestParam("score") int score, 
                                                               @RequestParam("index") int index, 
                                                               HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("Request to update score: score: {}, index: {}, for ProductPattern ID: {}, IP : {}", score, index, id, clientIp);
        
        Optional<ProductPattern> optionalProductPattern = productPatternRepo.findById(id);
        
        if (optionalProductPattern.isPresent()) {
            ProductPattern pp = optionalProductPattern.get();
            pp.getEvidences().get(index).setScore(score);
            productPatternRepo.save(pp);
            log.info("Successfully updated evidence score for ProductPattern ID={}", id);
            return ResponseEntity.ok(pp);
        } else {
            log.warn("Failed to find ProductPattern with ID={}", id);
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/download/{id}")
    private ResponseEntity<byte[]> ExportDataByWsId(@PathVariable UUID id, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("download ws ID : {}, IP : {}", id, clientIp);
        
        List<ProductPattern> pps = productPatternRepo.findByWorkingSetId(id);
        pps.sort(Comparator.comparing(ProductPattern::getPatterns)); 
        
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
                headers.setContentDispositionFormData("attachment", "Data.csv");
                headers.setContentLength(csvByte.length);

                return new ResponseEntity<>(csvByte, headers, HttpStatus.OK);

            } catch (IOException e) {
                log.error("Failed to write CSV data. Error message: {}", e.getMessage(), e);
            }
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<String> deleteProductPatternById(@PathVariable UUID id, HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        log.info("Delete Request to Product Pattern ID : {}, IP : {}", id, clientIp);

        try {
            if (id != null && productPatternRepo.findById(id) != null) {
                UUID wsId = productPatternRepo.findById(id).get().getWorkingSetId();
                WorkingSet ws = workingSetRepo.findById(wsId).get();
                productPatternRepo.deleteById(id);
                ws.setAdded(ws.getAdded() - 1);   
                workingSetRepo.save(ws);
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
