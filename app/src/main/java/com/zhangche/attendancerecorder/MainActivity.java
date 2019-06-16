package com.zhangche.attendancerecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.airsaid.calendarview.widget.CalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.sql.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CalendarView mCalendarView;
    private TextView mTxtDate;
    DateBaseHelper dateBaseHelper;
    private TextView textArea;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtDate = (TextView) findViewById(R.id.txt_date);
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        textArea = findViewById(R.id.textArea);

        // 设置已选的日期
        mCalendarView.setSelectDate(initData());

        // 指定显示的日期, 如当前月的下个月
        Calendar calendar = mCalendarView.getCalendar();
        calendar.add(Calendar.MONTH, 1);
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

                try {
                    if (select) {
                        Toast.makeText(getApplicationContext()
                                , "选中了：" + year + "年" + (month + 1) + "月" + day + "日", Toast.LENGTH_SHORT).show();
//                        dateBaseHelper = new DateBaseHelper();
//                        dateBaseHelper.addRecord(formatTime(year,(month + 1), day));
                        setAndReload(formatTime(year,(month + 1),day));
                        textArea.append(formatTime(year,(month + 1), day));
                    } else {
                        Toast.makeText(getApplicationContext()
                                , "取消选中了：" + year + "年" + (month + 1) + "月" + day + "日", Toast.LENGTH_SHORT).show();
//                        dateBaseHelper.deleteRecord(formatTime(year,(month + 1), day));
                        textArea.setText("");
                        for(String record:mCalendarView.getSelectDate()) {
                            textArea.append(record);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // 设置是否能够改变日期状态
        mCalendarView.setChangeDateStatus(true);

        // 设置日期点击监听
        mCalendarView.setOnDataClickListener(new CalendarView.OnDataClickListener() {
            @Override
            public void onDataClick(@NonNull CalendarView view, int year, int month, int day) {
                Log.e(TAG, "year: " + year);
                Log.e(TAG, "month,: " + (month + 1));
                Log.e(TAG, "day: " + day);
            }
        });
        // 设置是否能够点击
        mCalendarView.setClickable(true);

        setCurDate();
        try {
//            mCalendarView.setSelectDate(dateBaseHelper.getRecord());
            for(String record:mCalendarView.getSelectDate()) {
                textArea.append(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
//            mCalendarView.setSelectDate(dateBaseHelper.getRecord());
            for(String record:mCalendarView.getSelectDate()) {
                textArea.append(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> initData() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat(mCalendarView.getDateFormatPattern(), Locale.CHINA);
        sdf.format(calendar.getTime());
        dates.add(sdf.format(calendar.getTime()));
        return dates;
    }

    public void next(View v){
        mCalendarView.nextMonth();
        setCurDate();
    }

    public void last(View v){
        mCalendarView.lastMonth();
        setCurDate();
    }

    private void setCurDate(){
        mTxtDate.setText(mCalendarView.getYear() + "年" + (mCalendarView.getMonth() + 1) + "月");
    }

    private void createDialog(boolean isComplement) {
        String type = "打卡";
        if (isComplement) {
            type = "补签";
        } else {
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setIcon(null);
        alertDialog.setTitle(type);
        alertDialog.setMessage("确定" + type + "吗？");
        alertDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Log.d("zhangche","ok");
                    }
                });
        alertDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Log.d("zhangche","bad");
                    }
                });
        // 显示
        alertDialog.show();
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


    protected void setAndReload(final String record) {
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
                    conn = DriverManager.getConnection(DB_URL,USER,PASS);

                    // 执行查询
                    System.out.println(" 实例化Statement对象...");
                    stmt = conn.createStatement();
                    String sql;
                    sql = "insert into AttendanceRecord (record) values (" + record + ")";
//        ResultSet rs = stmt.executeQuery(sql);
                    stmt.executeUpdate(sql);
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

                }catch (SQLDataException se) {
                    se.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
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

            }
        }).start();
    }
}
