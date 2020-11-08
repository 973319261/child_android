package com.android.model;

import java.io.Serializable;

/**
 * 地区
 */
public class CountryAreaCode implements Serializable {

    public String header;
    public String areaCode;
    public String countryNameEn;
    public String countryNameCn;

    public final static String[] hotCountryNameCn = {"中国", "美国", "澳大利亚"};

    public final static String[] hotCountryNameEn = {"China", "United States of America", "Australia"};

}
