package com.android.core;

import com.android.bean.User;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.utils.SpellUtil;
import com.android.PEApplication;
import com.koi.chat.R;
import com.android.model.CountryAreaCode;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * APP服务
 */
public class AppService {

    public static String getDistance(JSONObject data) {
        double m = getDistanceM(data);
        return fortmartDistance(m);
    }

    public static String fortmartDistance(double distance) {
        if (distance > 1000) {
            return String.format("%.1fkm", distance / 1000);
        }
        return String.format("%.2fm", distance);
    }

    public static double getDistanceM(JSONObject data) {
        return 0;
    }


    public final static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String readRawDatas(int rawId){
        try {
            InputStream is = PEApplication.INSTANCE.getResources().openRawResource(rawId);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String str = new String(buffer, "utf-8");
            return str.trim();
        } catch (Exception e) {
            ALog.e(e);
        }
        return null;
    }

    public static List<CountryAreaCode> getCountryAreaCodeDatas() {
        JSONArray countys = AppUtil.toJsonArray(readRawDatas(R.raw.country_area_code));
        List<CountryAreaCode> datas = new ArrayList<>();
        if (!AppUtil.isNull(countys)) {
            for (String letter : Constants.LETTERS) {
                if ("#".equals(letter)) {
                    continue;
                }
                for (int i = 0; i < countys.length(); i++) {
                    String[] array = countys.optString(i).split("-");
                    if (PEApplication.isEn()) {
                        String headerChar = array[0].substring(0, 1);
                        if (letter.toLowerCase().equals(headerChar.toLowerCase())) {
                            CountryAreaCode countryAreaCode = new CountryAreaCode();
                            countryAreaCode.countryNameEn = array[0];
                            countryAreaCode.countryNameCn = array[1];
                            countryAreaCode.areaCode = String.format("+%s", array[2]);
                            countryAreaCode.header = headerChar.toUpperCase();
                            datas.add(countryAreaCode);
                        }
                    } else {
                        String headerChar = SpellUtil.getPinYinFirstLetter(array[1]);
                        if (letter.toLowerCase().equals(headerChar.toLowerCase())) {
                            CountryAreaCode countryAreaCode = new CountryAreaCode();
                            countryAreaCode.countryNameEn = array[0];
                            countryAreaCode.countryNameCn = array[1];
                            countryAreaCode.areaCode = String.format("+%s", array[2]);
                            countryAreaCode.header = headerChar.toUpperCase();
                            datas.add(countryAreaCode);
                        }
                    }
                }
            }
        }
        Collections.sort(datas, new Comparator<CountryAreaCode>() {
            @Override
            public int compare(CountryAreaCode c1, CountryAreaCode c2) {
                return c1.header.compareTo(c2.header);
            }
        });

        String[] hotNames = PEApplication.isEn() ? CountryAreaCode.hotCountryNameEn : CountryAreaCode.hotCountryNameCn;
        List<CountryAreaCode> results = new ArrayList<>(hotNames.length + datas.size());
        for (int i = 0; i < hotNames.length; i++) {
            for (CountryAreaCode countryAreaCode : datas) {
                if (hotNames[i].equals(PEApplication.isEn() ? countryAreaCode.countryNameEn : countryAreaCode.countryNameCn)) {
                    CountryAreaCode hotCountryAreaCode = new CountryAreaCode();
                    hotCountryAreaCode.countryNameEn = countryAreaCode.countryNameEn;
                    hotCountryAreaCode.countryNameCn = countryAreaCode.countryNameCn;
                    hotCountryAreaCode.areaCode = countryAreaCode.areaCode;
                    hotCountryAreaCode.header = "#";
                    results.add(hotCountryAreaCode);
                    break;
                }
            }
        }
        results.addAll(datas);
        return results;
    }

    public static String showUserName(JSONObject user) {
        if (AppUtil.isNull(user)) {
            return "";
        }
        if (StringUtils.isNotBlank(user.optString("name")) && user.optString("name").equals("null") == false) {
            return user.optString("name");
        }
        return user.optString("login");
    }
    public static String showUserName(User user) {
        if (user==null) {
            return "";
        }
        if (StringUtils.isNotBlank(user.getName()) && "null".equals(user.getName()) == false) {
            return user.getName();
        }
        return user.getLogin();
    }
    public static List<User> parseContacts(JSONArray datas) {
        List<User> contacts = AppUtil.jsonArrayToList(datas, User.class);
        for (int i = 0; contacts != null && i < contacts.size(); i++) {
            User user = contacts.get(i);
            user.setPinyin(SpellUtil.getPinYin(showUserName(user)));
            String firstLetter=SpellUtil.getPinYinFirstLetter(showUserName(user));
            if (StringUtils.isBlank(firstLetter) || (StringUtils.isNotBlank(firstLetter) && "0123456789~`@#$%^&*()_+-={}|[]\\\\:\\\";'<>?,./ ".contains(firstLetter.substring(0, 1)))) {
                firstLetter = "#";
            }
            user.setFirstLetter(firstLetter);
        }
        Collections.sort(contacts, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                return c1.getPinyin().compareTo(c2.getPinyin());
            }
        });
        return contacts;
    }
}

