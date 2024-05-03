package com.gytni.licenseclassify.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductPatternMapper {
    ProductPatternMapper INSTANCE = Mappers.getMapper(ProductPatternMapper.class);
    
    /* default ProductPatternDto<List<ProductPattern>> toPageDto(Page<ProductPattern> page) {
        return new ProductPatternDto<List<ProductPattern>>(page.getContent(), toPageInfo(page));
    }

    default PageInfo toPageInfo(Page<ProductPattern> page) {
        return new PageInfo(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    } */

}
