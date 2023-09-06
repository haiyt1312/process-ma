package com.jbsv.processma.common;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Utils {
    public static Date convertDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            log.warn("Error parsing date: {}", e.getMessage());
        }
        return date;
    }

    public static boolean isNull(Object o) {
        if (o == "")
            return true;
        return false;
    }

    public static boolean isEmpty(Object... o) {
        if (o == null) return true;
        for (Object obj : o) {
            if (obj == null) return true;
        }
        return false;
    }
}
