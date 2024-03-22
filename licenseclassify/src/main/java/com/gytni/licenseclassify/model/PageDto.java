package com.gytni.licenseclassify.model;

import java.util.List;

import lombok.Data;

@Data
public class PageDto<T> {
    private T data;
    private PageInfo pageInfo;

    public PageDto(T data, PageInfo pageInfo) {
        this.data = data;
        this.pageInfo = pageInfo;
    }

    @SuppressWarnings("unchecked")
    public PageDto(List<T> data, PageInfo pageInfo) {
        this.data = (T) data;
        this.pageInfo = pageInfo;
    }
}
