package com.zhangche.attendancerecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.sql.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CalendarView mCalendarView;
    private TextView mTxtDate;
    private TextView textArea;
    protected ArrayList<String> records = new ArrayList<>();
    protected ArrayList<String> unpassedRecords = new ArrayList<>();
    protected ArrayList<String> passedRecords = new ArrayList<>();
    protected boolean isServerBusy = false;
    protected String today = "";
    protected int[] day2Monitor = {1, 8, 15, 22, 29};
    protected ImageView[] resultBtns = new ImageView[5];
    protected int[] resourceIds = {
            R.drawable.result_0,
            R.drawable.result_1,
            R.drawable.result_2,
            R.drawable.result_3,
            R.drawable.result_4,
            R.drawable.result_5,
            R.drawable.result_6,
            R.drawable.result_7,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtDate = findViewById(R.id.txt_date);
        mCalendarView = findViewById(R.id.calendarView);
        textArea = findViewById(R.id.textArea);
        resultBtns[0] = findViewById(R.id.condition_0);
        resultBtns[1] = findViewById(R.id.condition_1);
        resultBtns[2] = findViewById(R.id.condition_2);
        resultBtns[3] = findViewById(R.id.condition_3);
        resultBtns[4] = findViewById(R.id.condition_4);

        // 设置已选的日期
//        mCalendarView.setSelectDate(initData());
//        today = mCalendarView.getSelectDate().get(0);
        today = getToday();

        // 指定显示的日期, 如当前月的下个月
        Calendar calendar = mCalendarView.getCalendar();
        calendar.add(Calendar.MONTH, 0);
        mCalendarView.setCalendar(calendar);

        // 设置字体
        mCalendarView.setTypeface(Typeface.SERIF);

        // 设置日期状态改变监听
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, boolean select, int year, int month, int day) {
                Log.e(TAG, "select: " + select);
                Log.e(TAG, "year: " + year);
                Log.e(TAG, "month,: " + (month + 1));
                Log.e(TAG, "day: " + day);

                createDialog(false, select, formatTime(year, month + 1, day));
            }

            @Override
            public void onSelectedMonthChange(@NonNull CalendarView view, int year, int month) {
                setCurDate();
                parseRecords();
            }
        });
        // 设置是否能够改变日期状态
        mCalendarView.setChangeDateStatus(true);

        // 设置日期点击监听
//        mCalendarView.setOnDataClickListener(new CalendarView.OnDataClickListener() {
//            @Override
//            public void onDataClick(@NonNull CalendarView view, int year, int month, int day) {
//                Log.e(TAG, "year: " + year);
//                Log.e(TAG, "month,: " + month);
//                Log.e(TAG, "day: " + day);
//            }
//        });
        // 设置是否能够点击
        mCalendarView.setClickable(true);

        setCurDate();
        loadRecordsFromLocal();
        parseRecords();
    }

    private List<String> initData() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat(mCalendarView.getDateFormatPattern(), Locale.CHINA);
        sdf.format(calendar.getTime());
        dates.add(sdf.format(calendar.getTime()));
        return dates;
    }

    private String getToday() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.CHINA);
        sdf.format(calendar.getTime());
        return sdf.format(calendar.getTime());
    }

    public void next(View v) {
        mCalendarView.nextMonth();
        setCurDate();
        parseRecords();
    }

    public void last(View v) {
        mCalendarView.lastMonth();
        setCurDate();
        parseRecords();
    }

    private void setCurDate() {
        mTxtDate.setText(mCalendarView.getYear() + "年" + (mCalendarView.getMonth() + 1) + "月");
    }

    protected ArrayList<String> getWeekDays(String targetDay) {
        ArrayList<String> days = new ArrayList<>();
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            cal.setTime(sdf.parse(targetDay));
            // 获得当前日期是一个星期的第几天
            int day = cal.get(Calendar.DAY_OF_WEEK);
            // 获取该周第一天
            cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day - 1);
            for (int i = 0; i < 7; i++) {
                cal.add(Calendar.DATE, 1);
                days.add(sdf.format(cal.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return days;
    }

    private void createDialog(boolean isComplement, final boolean select, final String date) {
        String type = "打卡";
        if (isComplement)
            type = "补签";
        if (!select)
            type = "取消打卡";

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setIcon(null);
        alertDialog.setTitle(type);
        alertDialog.setMessage("确定" + type + date + "吗？");
        alertDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Log.d("zhangche", "ok");
                        if (select) {
                            records.add(date);
                        } else {
                            removeFromRecords(date);
                        }
                        parseRecords();
                    }
                });
        alertDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        print(select);
                        if (!select) {
                            //CalendatView will auto control ArrayList
//                            records.add(date);
                        } else {
                            removeFromRecords(date);
                        }
                        parseRecords();
                    }
                });
        // 显示
        alertDialog.show();
    }

    protected void parseRecords() {
        save2Local();
        sync2Server();
        textArea.setText("今天是" + today);
        textArea.append("\n打卡记录：\n");
        for (String record : records) {
            textArea.append(record + "  ");
        }
        //mCalendarView.setSelectDate1(records);

        passedRecords.clear();
        unpassedRecords.clear();

        for (int i = 0; i < day2Monitor.length; i++) {
            ArrayList<String> list = getWeekDays(
                    formatTime(mCalendarView.getYear(), mCalendarView.getMonth() + 1, day2Monitor[i]));
            int days = 0;

            printArrayList(list, true);
            printArrayList(records, true);
            ArrayList<String> recordedDays = new ArrayList<>();
            for (String day : list) {
                if (records.contains(day)) {
                    days++;
                    recordedDays.add(day);
                }
            }
            if (days > 2) {
                passedRecords.addAll(recordedDays);
            } else {
                unpassedRecords.addAll(recordedDays);
            }
            resultBtns[i].setImageResource(resourceIds[days]);
            Log.d(TAG, i + " week has " + days);
        }
        mCalendarView.setSelectDate1(unpassedRecords);
        mCalendarView.setSelectDate2(passedRecords);
    }

    protected void removeFromRecords(String record) {
        for (int var = 0; var < records.size(); var++) {
            Log.d(TAG, "act " + record + "/" + var + "/" + records.get(var));
            if (records.get(var).equals(record)) {
                records.remove(var);
                Log.d(TAG, "remove " + record);
            }
        }
    }

    protected void removeSameRecord() {
        ArrayList<String> tempList = new ArrayList();
        Log.d(TAG, "total is " + records.size());
        for (int var = 0; var < records.size(); var++) {
            if (tempList.contains(records.get(var))) {
                Log.d(TAG, "remove same" + records.get(var));
                records.remove(var);
            } else {
                tempList.add(records.get(var));
//                Log.d(TAG,"not same " + records.get(var));
            }
        }
        Collections.sort(records);
        Log.d(TAG, "after is " + records.size());
    }

    private String formatTime(int year, int month, int day) {
        String result = "";
        if (month < 10)
            result = result + year + "0" + month;
        else
            result = result + year + month;

        if (day < 10)
            result = result + "0" + day;
        else
            result = result + day;

        return result;
    }


    protected void sync2Server() {
        if (isServerBusy)
            return;
        isServerBusy = true;
        final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        final String DB_URL = "jdbc:mysql://60.205.191.159:3306/songzhen?useSSL=false";

        // 数据库的用户名与密码，需要根据自己的设置
//    static final String USER = "root";
//    static final String PASS = "ZChe142287";
        final String USER = "owner";
        final String PASS = "1_X?8U4T=wUB";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                Statement stmt = null;
                try {
                    // 注册 JDBC 驱动
                    Class.forName(JDBC_DRIVER);

                    // 打开链接
                    System.out.println("连接数据库...");
                    conn = DriverManager.getConnection(DB_URL, USER, PASS);

                    // 执行查询
                    System.out.println(" 实例化Statement对象...");
                    stmt = conn.createStatement();
                    String sql;
//                    sql = "SELECT record from AttendanceRecord";
//                    ResultSet rs = stmt.executeQuery(sql);
//
//                    ArrayList<String> result = new ArrayList<>();
//                    // 展开结果集数据库
//                    while(rs.next()){
//                        // 通过字段检索
//                        String record = rs.getString("record");
//
//                        result.add(record);
//                        // 输出数据
//                        System.out.print("record: " + record);
//                    }
//                    // 完成后关闭
//                    rs.close();

                    sql = "delete from AttendanceRecord where _id >= 0";
                    stmt.executeUpdate(sql);
                    for (String record : records) {
//                        Log.d("zhangche",record);
//                        Log.d("zhangche",record + "/" + records.size());
                        sql = "insert into AttendanceRecord (record) values (" + record + ")";
//                        Log.d("zhangche",sql);
                        stmt.executeUpdate(sql);

                    }
                } catch (SQLDataException se) {
                    se.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stmt != null) stmt.close();
                    } catch (SQLException se2) {
                    }// 什么都不做
                    try {
                        if (conn != null) conn.close();
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }
                    isServerBusy = false;
                }

            }
        }).start();
    }

    private void loadRecordsFromLocal() {
        SharedPreferences sp = MainActivity.this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String savedRecords = sp.getString("records", "");
        records.clear();
        for (String record : savedRecords.split(" ")) {
            if (record.equals(""))
                continue;
            records.add(record);
            Log.d("zhangche", record + "/" + records.size());
        }
    }

    private void save2Local() {
        SharedPreferences sp = MainActivity.this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        removeSameRecord();
        String record2Save = "";
        for (String record : records) {
            record2Save += record + " ";
        }
        Log.d(TAG, record2Save);
        editor.putString("records", record2Save);
        record2Save = "";
        for (String record : unpassedRecords) {
            record2Save += record + " ";
        }
        Log.d(TAG, record2Save);
        editor.putString("unpassedRecords", record2Save);
        record2Save = "";
        for (String record : passedRecords) {
            record2Save += record + " ";
        }
        Log.d(TAG, record2Save);
        editor.putString("passedRecords", record2Save);
        editor.apply();
    }

    public void printArrayList(ArrayList list, boolean singleLine) {
        if (singleLine) {
            String result = "";
            for (Object item : list) {
                result += item.toString() + " ";
            }
            Log.d(TAG, result);
        } else {
            for (int i = 0; i < list.size(); i++) {
                Log.d(TAG, "List num " + i + ":" + list.get(i).toString());
            }
        }
    }

    public void print(Object str) {
        Log.d(TAG, "" + str.toString());
    }
}
