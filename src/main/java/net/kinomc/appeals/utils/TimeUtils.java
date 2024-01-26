package net.kinomc.appeals.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    /**
     * @return 相差月数的绝对值
     */
    public static int getMonthDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        int year = c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        int month = c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return Math.abs(year * 12 + month);
    }
}
