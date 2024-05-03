package com.gytni.licenseclassify.model;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CSVUploadPattern {
    // 업로드 한 csv 파일 바인딩 객체
    
    @CsvBindByName(column = "제어판 이름")
    private String productName;

    @CsvBindByName(column = "저작권사")
    private String publisher;

    @CsvBindByName(column = "수집량")
    private int collectedCount;
}
