package com.gytni.licenseclassify.dto;

import java.util.List;

import com.gytni.licenseclassify.model.PageInfo;

import lombok.Data;

@Data
public class ProductPatternDto<T> {
    private T data;
    private PageInfo pageInfo;

    public ProductPatternDto(T data, PageInfo pageInfo) {
        this.data = data;
        this.pageInfo = pageInfo;
    }

    @SuppressWarnings("unchecked")
    public ProductPatternDto(List<T> data, PageInfo pageInfo) {
        this.data = (T) data;
        this.pageInfo = pageInfo;
    }
}
