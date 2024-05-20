package com.gytni.licenseclassify.repo.searcher;
import java.util.UUID;

import lombok.Getter;

@Getter
public class SearchCriteria {
    
    /* public SearchCriteria(String dataField, SearchOperationType operation, List<Object> values, boolean isOrOperation) {
        this.dataField = dataField;
        this.operation = operation;
        this.values = values;
        this.isOrOperation = isOrOperation;
    }

    private String dataField;
    private SearchOperationType operation;
    private List<Object> values;
    private boolean isOrOperation; */

    private UUID workingSetId;
    private String searchKeyword;
    private boolean reviewNeeded;
    private boolean isException;
    private boolean classified;
    
    public SearchCriteria(String dataField, UUID workingSetId, String searchKeyword, boolean reviewNeeded, boolean isException, boolean classified) {
        this.workingSetId = workingSetId;
        this.searchKeyword = searchKeyword;
        this.reviewNeeded = reviewNeeded;
        this.isException = isException;
        this.classified = classified;
    }
}