package com.zhangche.attendancerecorder;

import android.os.Build;
import android.util.Log;

import java.sql.*;
import java.util.ArrayList;

public class DateBaseHelper {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://60.205.191.159:3306/songzhen?useSSL=false";

    // 数据库的用户名与密码，需要根据自己的设置
//    static final String USER = "root";
//    static final String PASS = "ZChe142287";
    static final String USER = "owner";
    static final String PASS = "1_X?8U4T=wUB";

    static Connection conn = null;
    static Statement stmt = null;
    public static boolean isInitDone = false;

    public DateBaseHelper() {
        String snNo = Build.SERIAL;
        Log.d("zhangche", "sn=" + snNo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectInit();
                    isInitDone = true;
                }catch (SQLDataException se) {
                    se.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void connectInit() throws Exception {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            for(String record:getRecord())
                Log.d("zhangche",record);
        System.out.println("Done!");
    }
    public void addRecord(String record) throws Exception{
        waitForInitDone();
        String sql;
        sql = "insert into AttendanceRecord (record) values " + record;
//        ResultSet rs = stmt.executeQuery(sql);
        stmt.executeUpdate(sql);
    }
    public void deleteRecord(String record) throws Exception{
        waitForInitDone();
        String sql;
        sql = "delete from AttendanceRecord where record = " + record;
        stmt.executeUpdate(sql);
    }
    public static ArrayList<String> getRecord() throws Exception{
        waitForInitDone();
        String sql;
        sql = "SELECT record from AttendanceRecord";
        ResultSet rs = stmt.executeQuery(sql);

        ArrayList<String> result = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            String record = rs.getString("record");

            result.add(record);
            // 输出数据
            System.out.print("record: " + record);
        }
        // 完成后关闭
        rs.close();

        return result;
    }
    public void closeDateBase() throws Exception {
        try{
            if(stmt!=null) stmt.close();
        }catch(SQLException se2){
        }// 什么都不做
        try{
            if(conn!=null) conn.close();
        }catch(SQLException se){
            se.printStackTrace();
        }
    }
    public static void waitForInitDone() {
        while (!isInitDone) {
            try {
                Thread.sleep(100);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(conn == null)
            Log.d("zhangche","conn is null");

            if(stmt == null)
                Log.d("zhangche","stmt is null");
    }
}
