package com.android.utils;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateUtil {
    public final static long MIN_AUDIO_LENGTH = 1000;//毫秒

    /**
     * 获取数时间差
     * @param start
     * @param end
     * @return
     */
    public static long getSecondBetween(long start, long end) {
        return end - start;
    }

    public static String getMondayOfThisWeek() {
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayofweek == 0)
            dayofweek = 7;
        c.add(Calendar.DATE, -dayofweek + 1);
        return DATE_FORMAT.format(c.getTime());
    }

    public static String getSundayOfThisWeek() {
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayofweek == 0)
            dayofweek = 7;
        c.add(Calendar.DATE, -dayofweek + 7);
        return DATE_FORMAT.format(c.getTime());
    }

    public static String getMondayOfLastWeek() {
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayofweek == 0)
            dayofweek = 7;
        c.add(Calendar.DATE, -dayofweek - 1);
        return DATE_FORMAT.format(c.getTime());
    }

    public static String getSundayOfLastWeek() {
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayofweek == 0)
            dayofweek = 7;
        c.add(Calendar.DATE, -dayofweek - 7);
        return DATE_FORMAT.format(c.getTime());
    }

    /**
     * 得到上月最后一天
     *
     * @return
     */
    public static String getLastDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, days);
        String result = DATE_FORMAT.format(cal.getTime());
        return result;
    }

    /**
     * 得到上月第一天
     *
     * @return
     */
    public static String getFristDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, days);
        String result = DATE_FORMAT.format(cal.getTime());
        return result;
    }

    /**
     * 得到上月最后一天
     *
     * @return
     */
    public static String getLastDateOfLastMonth() {
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.MONDAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, days);
        String result = DATE_FORMAT.format(cal.getTime());
        return result;
    }

    /**
     * 得到本月第一天
     *
     * @return
     */
    public static String getFristDateOfLastMonth() {
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.MONDAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, days);
        String result = DATE_FORMAT.format(cal.getTime());
        return result;
    }

    public static Date tomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    public static Date monthAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    public static Date minuteAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        return cal.getTime();
    }

    static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String DATE_PATTERN = "yyyy-MM-dd";
    static String DATE_PATTERN_ZH = "yyyy年MM月dd日";
    static String SHOW_DATE_PATTERN = "yy-MM-dd";
    static String SHOW_DATE_TIME_PATTERN = "MM-dd HH:mm";
    static String TIME_PATTERN = "HH:mm:ss";
    static String SHOW_TIME_PATTERN = "HH:mm";
    static String SHOW_MONTH_DAY_PATTERN = "MM-dd";
    static String CAL_PATTERN = "MM月yyyy年";
    public static SimpleDateFormat D_DATE_TIME_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    public static SimpleDateFormat PART_DATE_TIME_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_TIME_PATTERN);
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
    public static SimpleDateFormat DATE_FORMAT_ZH = new SimpleDateFormat(DATE_PATTERN_ZH);
    public static SimpleDateFormat SHOW_DATE_FORMAT = new SimpleDateFormat(SHOW_DATE_PATTERN);
    public static SimpleDateFormat SHOW_DATE_TIME_FORMAT = new SimpleDateFormat(SHOW_DATE_TIME_PATTERN);
    public static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(TIME_PATTERN);
    public static SimpleDateFormat SHOW_TIME_FORMAT = new SimpleDateFormat(SHOW_TIME_PATTERN);
    public static SimpleDateFormat SHOW_MONTH_DAY_FORMAT = new SimpleDateFormat(SHOW_MONTH_DAY_PATTERN);
    public static SimpleDateFormat CAL_FORMAT = new SimpleDateFormat(CAL_PATTERN);
    public static SimpleDateFormat SHOW_CH_MONTH_DAY_FORMAT = new SimpleDateFormat("MM月dd日");
    public static SimpleDateFormat HUODONG_DATE_FORMAT = new SimpleDateFormat("MM月dd日 HH:mm");
    public static SimpleDateFormat MMDD_FORMAT = new SimpleDateFormat("MMddE");
    public static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd");
    public static SimpleDateFormat MMDD_FORMAT_NOWEEK = new SimpleDateFormat("MMdd");
    public static SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("E");
    public static SimpleDateFormat MMDD_WEEK_FORMAT = new SimpleDateFormat("MM/dd E");
    public static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat REFRESH_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static SimpleDateFormat COUPON_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static long tsParseToLong(String date) {
        try {
            return REFRESH_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static long parseToLong(String date) {
        try {
            return DATE_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static long dateTimeParseToLong(String date) {
        if (StringUtils.isBlank(date)) {
            return 0;
        }
        try {
            String[] dateParts = StringUtils.split(date, "-");
            if (dateParts != null && dateParts.length > 0) {
                return PART_DATE_TIME_FORMAT.parse(date).getTime();
            } else {
                return DATE_TIME_FORMAT.parse(date).getTime();
            }
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String formatMonth(String s) {
        try {
            return MONTH_FORMAT.format(MONTH_FORMAT.parse(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatCHMonthDay(String s) {
        try {
            return SHOW_CH_MONTH_DAY_FORMAT.format(DATE_TIME_FORMAT.parse(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatZhDate(String s) {
        try {
            return DATE_FORMAT_ZH.format(DATE_FORMAT_ZH.parse(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String format2ZhDate(String s) {
        try {
            return DATE_FORMAT_ZH.format(DATE_FORMAT.parse(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatShowDate(String time) {
        if (time == null) {
            return "";
        }
        try {
            return REFRESH_FORMAT.format(DATE_TIME_FORMAT.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatZhDate(long ts) {
        Date d = new Date(ts);
        return DATE_FORMAT_ZH.format(d);
    }

    public static String pullRefreshTime(String time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(time));
        return REFRESH_FORMAT.format(c.getTime());
    }

    public static String formatDate(long time) {
        Date date = new Date(time);
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(long time) {
        Date date = new Date(time);
        return REFRESH_FORMAT.format(date);
    }


    public static String formatAllDateTime(long time) {
        Date date = new Date(time);
        return DATE_TIME_FORMAT.format(date);
    }

    public static String formatShowTime(long time) {
        Date date = new Date(time);
        return SHOW_DATE_TIME_FORMAT.format(date);
    }

    public static String showDetailTime(long ts) {
        long rangeTs = System.currentTimeMillis() - ts;
        rangeTs = rangeTs / 1000;
        if (rangeTs < 60) {
            return "刚刚";
        } else if (rangeTs < 3600) {
            return String.format("%s 分钟前", rangeTs / 60);
        } else if (rangeTs < 3600 * 24) {
            return String.format("%d 小时前", rangeTs / 3600);
        } else {
            return formatShowTime(ts);
        }
    }

    public static String showMsgTime(long ts) {
        long rangeTs = System.currentTimeMillis() - ts;
        rangeTs = rangeTs / 1000;
        if (rangeTs < 3600 * 24) {
            return SHOW_TIME_FORMAT.format(new Date(ts));
        } else {
            return formatShowTime(ts);
        }
    }

    public static int getDaysBetweenLongTime(long t1, long t2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(t1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t2);
        return getDaysBetween(c1, c2);
    }

    /**
     * 获取相差天数
     * @param d1
     * @param d2
     * @return
     */
    public static int getDaysBetween(Calendar d1, Calendar d2) {
        if (d1.after(d2)) {
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }
        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        if (d1.get(Calendar.YEAR) != y2) {
            d1 = (Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    public static int getMonthsBetween(Calendar srcCal, Calendar dstCal) {

        if (srcCal.after(dstCal)) {
            Calendar swap = srcCal;
            srcCal = dstCal;
            dstCal = swap;
        }

        int year = dstCal.get(Calendar.YEAR) - srcCal.get(Calendar.YEAR);
        int month = dstCal.get(Calendar.MONTH) - srcCal.get(Calendar.MONTH);
        int day = dstCal.get(Calendar.DAY_OF_MONTH)
                - srcCal.get(Calendar.DAY_OF_MONTH);
        month += year * 12;
        month = (day < 0 ? month - 1 : month);
        return month;
    }

    /**
     * 获取相差年份
     * @param srcCal
     * @param dstCal
     * @return
     */
    public static int getYearsBetween(Calendar srcCal, Calendar dstCal) {

        if (srcCal.after(dstCal)) {
            Calendar swap = srcCal;
            srcCal = dstCal;
            dstCal = swap;
        }

        int year = dstCal.get(Calendar.YEAR) - srcCal.get(Calendar.YEAR);
        return year;
    }

    public static boolean dateBefore(Calendar date, Calendar compareDate) {
        return date.before(compareDate) && (getDaysBetween(date, compareDate) != 0);
    }

    public static boolean dateBeforeTomorrow(Calendar date) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        return date.before(c) && (getDaysBetween(date, c) != 0);
    }

    public static boolean dateBeforeToday(Calendar date) {
        Calendar c = Calendar.getInstance();
        return date.before(c) && (getDaysBetween(date, c) != 0);
    }

    public static boolean dateAfter(Calendar date, Calendar compareDate) {
        return date.after(compareDate) && (getDaysBetween(date, compareDate) != 0);
    }

    /**
     * 同年之间时间差计算
     *
     * @param string
     * @return
     */
    private static String parseDateBase(String string) {
        String result = "";
        try {
            Date date = DATE_TIME_FORMAT.parse(string);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.clear();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int cMonth = calendar.get(Calendar.MONTH);
            int cDay = calendar.get(Calendar.DAY_OF_MONTH) + 1;
            int cHour = calendar.get(Calendar.HOUR_OF_DAY);
            int cMinute = calendar.get(Calendar.MINUTE);
            if (month == cMonth) {
                if (day == cDay) {
                    if (hour == cHour) {
                        result = minute == cMinute ? "刚刚"
                                : (cMinute - minute) + "分钟前";
                    } else {
                        result = (cHour - hour) + "小时前";
                    }
                } else {
                    int dValue = Math.abs(cDay - day);
                    if (dValue == 1) {
                        result = "昨天";
                    } else if (dValue == 2) {
                        result = "前天";
                    } else {
                        result = month + "月" + day + "日";
                    }
                }
            } else {
                result = month + "月" + day + "日";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 不同年之间以普通方式显示输入年份
     *
     * @param string
     * @return
     */
    public static String parseCustomDate(String string) {
        String result = "";
        try {
            Date date = DATE_TIME_FORMAT.parse(string);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            calendar.clear();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int cYear = calendar.get(Calendar.YEAR);
            if (year == cYear) {
                result = parseDateBase(string);
            } else {
                result = DATE_TIME_FORMAT.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 不同年之间以中文方式显示输入年份
     *
     * @param string
     * @return
     */
    public static String parseDateForZH(String string) {
        String result = "";
        try {
            Date date = DATE_TIME_FORMAT.parse(string);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            calendar.clear();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int cYear = calendar.get(Calendar.YEAR);
            if (year == cYear) {
                result = parseDateBase(string);
            } else {
                result = DATE_FORMAT_ZH.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getToday() {
        return DATE_FORMAT.format(new Date());
    }


    public static String showTime(String time) {
        if (StringUtils.isBlank(time)) {
            return "";
        }
        return time;
    }
}
