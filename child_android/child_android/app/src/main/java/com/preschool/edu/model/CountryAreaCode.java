package com.preschool.edu.model;

import java.io.Serializable;

/**
 * Created by jac_cheng on 2017/12/30.
 */

public class CountryAreaCode implements Serializable {

    public String header;
    public String areaCode;
    public String countryNameEn;
    public String countryNameCn;

    public final static String[] hotCountryNameCn = {"中国", "美国", "澳大利亚"};

    public final static String[] hotCountryNameEn = {"China", "United States of America", "Australia"};

}
