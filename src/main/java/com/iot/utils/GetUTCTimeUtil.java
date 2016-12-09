package com.iot.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author wang zhenfei 
 *
 */
public final class GetUTCTimeUtil {

    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm") ;

    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br /> 
     * 如果获取失败，返回null 
     * @return
     */
    public static String getUTCTimeStr(Calendar cal) {
        StringBuffer UTCTimeBuffer = new StringBuffer();


        // 2、取得时间偏移量：  
        int zoneOffset =28800000;

        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        System.out.println(dstOffset);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;

        String strMonth="";
        if(month<10){
            strMonth="0"+String.valueOf(month);
        }else {
            strMonth=String.valueOf(month);
        }


        int day = cal.get(Calendar.DAY_OF_MONTH);


        String strDay="";
        if (day<10){
            strDay="0"+String.valueOf(day);
        }else {
            strDay=String.valueOf(day);
        }

        int hour = cal.get(Calendar.HOUR_OF_DAY);


        String strHour="";
        if (hour<10){
            strHour="0"+String.valueOf(hour);
        }else {
            strHour=String.valueOf(hour);
        }


        int minute = cal.get(Calendar.MINUTE);


        String strMin="";

        if (minute<10){
            strMin="0"+String.valueOf(minute);
        }else {
            strMin=String.valueOf(minute);
        }

        int second = cal.get(Calendar.SECOND);

        String strSecond="";

        if (second<10){
            strSecond="0"+String.valueOf(second);
        }else {
            strSecond=String.valueOf(second);
        }



        UTCTimeBuffer.append(year).append("-").append(strMonth).append("-").append(strDay) ;
        UTCTimeBuffer.append(" ").append(strHour).append(":").append(strMin).append(":").append(strSecond) ;
        try{
            format.parse(UTCTimeBuffer.toString()) ;
            return UTCTimeBuffer.toString() ;
        }catch(ParseException e)
        {
            e.printStackTrace() ;
        }
        return null ;
    }

    /**
     * 将UTC时间转换为东八区时间
     * @param UTCTime
     * @return
     */
    public static String getLocalTimeFromUTC(String UTCTime){
        java.util.Date UTCDate = null ;
        String localTimeStr = null ;
        try {
            UTCDate = format.parse(UTCTime);
            format.setTimeZone(TimeZone.getTimeZone("GMT-8")) ;
            localTimeStr = format.format(UTCDate) ;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return localTimeStr ;
    }



    public static void main(String[] args) throws ParseException {
        String record_time = "2016-09-26 00:00:00";

        Calendar cal =  Calendar.getInstance();


        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date recordTime = simpleDateFormat.parse(record_time);


        cal.setTime(recordTime);
        String utcTime = getUTCTimeStr(cal);
        System.out.println(utcTime);


    }

}  