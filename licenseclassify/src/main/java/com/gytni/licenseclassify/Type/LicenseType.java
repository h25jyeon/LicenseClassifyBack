package com.gytni.licenseclassify.Type;

public enum LicenseType {
    FREE("free"), SHAREWARE("shareware"), COMMERCIAL("commercial"), ETC("etc"), NONE("none");

    private String text;

    LicenseType(String text) {
        this.text = text;
    }

    public static LicenseType find(String text) {
        for (LicenseType lt : LicenseType.values())
            if (lt.text.equalsIgnoreCase(text))
                return lt;
        return NONE;
    }

}
