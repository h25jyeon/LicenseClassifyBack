package com.gytni.licenseclassify.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gytni.licenseclassify.annotation.RemoteIp;
import com.gytni.licenseclassify.model.CSVUploadPattern;
import com.gytni.licenseclassify.model.WorkingSet;
import com.gytni.licenseclassify.repo.ProductPatternRepo;
import com.gytni.licenseclassify.repo.WorkingSetRepo;
import com.gytni.licenseclassify.service.ProductPatternService;
import com.gytni.licenseclassify.service.WorkingSetService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.bean.CsvToBeanBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/working-set")
public class WorkingSetController {
    @Autowired
    private WorkingSetRepo workingSetRepo;

    @Autowired
    private WorkingSetService workingSetService;

    @Autowired
    private ProductPatternService productPatternService;

    @Autowired
    private ProductPatternRepo productPatternRepo;

    @GetMapping("")
    private ResponseEntity<List<WorkingSet>> getAllWorkingSet(HttpServletRequest request, @RemoteIp String clientIp) {

        log.info("get All WorkingSet Request IP : {}", clientIp);

        StopWatch sw = StopWatch.createStarted();

        try {
            List<WorkingSet> allWorkingSets = workingSetRepo.findAll();

            if (allWorkingSets.isEmpty()) return ResponseEntity.noContent().build(); // 빠른탈출

            for (WorkingSet ws : allWorkingSets) {

                int total = ws.getAdded();
                long classifiedSize = productPatternRepo.countByWorkingSetIdAndUnclassifiedFalse(ws.getId());
                
                double classifyPercentage = (double) classifiedSize / total * 100;
                String updatedName = String.format("%.0f%%\t%s\t(%d/%d)", classifyPercentage, ws.getName(), classifiedSize, total);
                ws.setName(updatedName); 
            } 
                
            return ResponseEntity.ok(allWorkingSets);
        } catch (Exception e) {
            log.error("Can not response becuase exception.", e);
            return ResponseEntity.internalServerError().build();
        } finally {
            log.info("Loaded All Workingset in {} ms.", sw.getTime(TimeUnit.MILLISECONDS)); 
        }
    }

    @PostMapping("")
    private ResponseEntity<WorkingSet> saveWorkingSet(@RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName) {
        log.info("saveWorkingSet 호출");
        if (file.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try (InputStreamReader reader = workingSetService.getEncodedReader(file)) {
            if (reader == null)
                throw new IOException("Failed to create InputStreamReader for the file.");

            String fileContentHash = DigestUtils.md5Hex(file.getInputStream());
            UUID existingId = workingSetRepo.findIdByHash(fileContentHash);

            if (existingId != null)
                return ResponseEntity.ok(workingSetRepo.findById(existingId).get());
            else {
                WorkingSet ws = new WorkingSet();
                ws.setName(fileName);
                ws.setHash(fileContentHash);
                ws = workingSetRepo.save(ws);

                RFC4180Parser rfc4180ParserForCsvToBean = new RFC4180ParserBuilder().build();
                CSVReaderBuilder csvReaderBuilderForCsvToBean = new CSVReaderBuilder(reader)
                        .withCSVParser(rfc4180ParserForCsvToBean);

                try (CSVReader csvReaderForCsvToBean = csvReaderBuilderForCsvToBean.build()) {
                    List<CSVUploadPattern> parsedData = new CsvToBeanBuilder<CSVUploadPattern>(csvReaderForCsvToBean)
                            .withType(CSVUploadPattern.class).build().parse();
                    productPatternService.saveProductPattern(parsedData, ws);
                }

                ws = workingSetRepo.save(ws);
                return ResponseEntity.ok(ws);
            }
        } catch (IOException e) {
            log.error("Fail to save working set : file={}", file, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Fail to save working set : file={}", file, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("")
    private ResponseEntity<String> deleteWorkingSet(@RequestParam("workingSetId") UUID id) {
        log.info("Delete request received for workingSetId: {}", id);

        try {
            productPatternService.deleteByWorkingSetId(id);
            workingSetRepo.deleteById(id);
            return ResponseEntity.ok("Working set with ID " + id + " has been successfully deleted.");
        } catch (EmptyResultDataAccessException e) {
            log.warn("Working set with ID {} not found for deletion.", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete working set with ID {}.", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete working set.");
        }
    }

}
