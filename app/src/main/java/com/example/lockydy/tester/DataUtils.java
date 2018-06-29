package com.example.lockydy.tester;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

//对时间进行格式化
//确认我们发送聊天内容的时间
public class DataUtils {

    //屏蔽可能发出的错误信息
    @SuppressLint("SimpleDateFormat")
    //调用内置类实现功能
    public static String dateString(Date date)
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}