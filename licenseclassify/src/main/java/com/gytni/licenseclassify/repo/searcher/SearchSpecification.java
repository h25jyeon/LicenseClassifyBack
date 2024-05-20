package com.gytni.licenseclassify.repo.searcher;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SearchSpecification<T> implements Specification<T> {

    private SearchCriteria criteria;

    public SearchSpecification(final SearchCriteria searchCriteria) {
        super();
        this.criteria = searchCriteria;
    }
    
    private Expression<String> jsonExtract(CriteriaBuilder cb, Expression<?> x, String path) {
        return cb.function("JSON_UNQUOTE", String.class, 
               cb.function("JSON_EXTRACT", String.class, x, cb.literal(path)));
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Expression<String> productNamePath = jsonExtract(builder, root.get("patterns"), "$.productName");
        Expression<String> publisherPath = jsonExtract(builder, root.get("patterns"), "$.publisher");
        
        Predicate searchPredicate = builder.conjunction(); 
        Predicate reviewNeededPredicate = builder.conjunction(); 
        Predicate classifiedPredicate = builder.conjunction(); 
        Predicate isExceptionPredicate = builder.conjunction(); 
    
        if (criteria.getSearchKeyword() != null && !criteria.getSearchKeyword().isEmpty()) {
            String searchKeywordLower = criteria.getSearchKeyword().toLowerCase();
            Predicate productNamePredicate = builder.like(builder.lower(productNamePath), "%" + searchKeywordLower + "%");
            Predicate publisherPredicate = builder.like(builder.lower(publisherPath), "%" + searchKeywordLower + "%");
            searchPredicate = builder.or(productNamePredicate, publisherPredicate); 
        }
        
        if ((criteria.isReviewNeeded())) reviewNeededPredicate =  builder.isNull(root.get("licenseType")) ;
        if (criteria.isClassified()) classifiedPredicate = builder.equal(root.get("unclassified"), false); 
        if (!criteria.isException()) isExceptionPredicate = builder.equal(root.get("exceptions"), false);
        
        UUID workingSetId = criteria.getWorkingSetId();
        Predicate workingSetIdPredicate = builder.equal(root.get("workingSetId"), workingSetId);
    
        return builder.and(
            searchPredicate,
            reviewNeededPredicate,
            classifiedPredicate,
            isExceptionPredicate,
            workingSetIdPredicate
        );
    }
}
