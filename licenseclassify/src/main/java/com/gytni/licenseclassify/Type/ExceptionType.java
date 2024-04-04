package com.gytni.licenseclassify.Type;

public enum ExceptionType {
    PUBLISHER_PRODUCT_EXACT_MATCH("D"), PUBLISHER_EXACT_MATCH("C"), PUBLISHER_MATCH("B"),  PRODUCT_MATCH("A"), ETC("N");
    
    private String label;

    ExceptionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
