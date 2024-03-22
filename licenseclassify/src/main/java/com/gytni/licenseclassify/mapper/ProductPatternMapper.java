package com.gytni.licenseclassify.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import com.gytni.licenseclassify.model.PageDto;
import com.gytni.licenseclassify.model.PageInfo;
import com.gytni.licenseclassify.model.ProductPattern;

@Mapper
public interface ProductPatternMapper {
    ProductPatternMapper INSTANCE = Mappers.getMapper(ProductPatternMapper.class);
    
    default PageDto<List<ProductPattern>> toPageDto(Page<ProductPattern> page) {
        return new PageDto<List<ProductPattern>>(page.getContent(), toPageInfo(page));
    }

    default PageInfo toPageInfo(Page<ProductPattern> page) {
        return new PageInfo(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

}
