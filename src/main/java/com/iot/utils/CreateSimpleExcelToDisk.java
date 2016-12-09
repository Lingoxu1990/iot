package com.iot.utils;

import org.apache.poi.hssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateSimpleExcelToDisk {

    public static  void createExcel(String[] tableHeaders,String[] dataKeys,List<Map<String,String>> dataList,OutputStream out) throws Exception{
        // 第一步，创建一个webbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet("工作表一");
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        HSSFRow row = sheet.createRow(0);
        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

        for(int i = 0; i < tableHeaders.length; i++){
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(tableHeaders[i]);
            cell.setCellStyle(style);
        }

        if(dataList.size() == 0){
            HSSFRow dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("无数据");
        }

        for(int i = 0; i < dataList.size(); i++){
            HSSFRow dataRow = sheet.createRow((int) i + 1);
            Map<String,String> data = dataList.get(i);

            for(int j = 0; j < dataKeys.length; j++){
                if(data.get(dataKeys[j])==null || "".equals(data.get(dataKeys[j]))){
                    dataRow.createCell(j).setCellValue("");
                }else{
                    dataRow.createCell(j).setCellValue(data.get(dataKeys[j]));
                }
            }
        }

        wb.write(out);
    }

    public static void main(String[] args) throws Exception {
        String[] tableHeaders = new String[]{"名字","年龄","备注"};
        String[] dataNames = new String[]{"name","age","remark"};

        Map<String,String> data1 = new HashMap<String,String>();
        data1.put("name","H1");
        data1.put("age","H1");
        data1.put("remark","H1");

        Map<String,String> data2 = new HashMap<String,String>();
        data2.put("name","H2");
        data2.put("age","H2");
        data2.put("remark","H2");

        Map<String,String> data3 = new HashMap<String,String>();
        data3.put("name","H3");
        data3.put("age","H3");
        data3.put("remark","H3");

        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        list.add(data1);
        list.add(data2);
        list.add(data3);

        FileOutputStream fout = new FileOutputStream("/Users/black/work/code/idea/colorful-admin/students.xls");

        CreateSimpleExcelToDisk.createExcel(tableHeaders,dataNames,list,fout);

        fout.close();
    }
}