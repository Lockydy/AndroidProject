package com.example.lockydy.tester;

import java.lang.reflect.Type;
import java.util.Date;
/*
    聊天信息的实体类
    用来描述我们的所要显示的聊天信息
    同时这个信息也将上传至AI从而给出我们要的回应
    这个类只是简单的与聊天消息有关
 */
public class ChatMessage {

    private Date date;
    private String name;
    private String message;
    private Type type;// 枚举类型数据:0表示发送 1表示接受
    public ChatMessage(){}//系统的构造函数
    //构造函数二号
    public ChatMessage(String message, Type type, Date date){
        super();//继承
        this.message=message;
        this.type=type;
        this.date=date;
    }
    //getset方法
    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
    public Date getDate() {
        return date;
    }
    public enum Type{
        INCOUNT,OUTCOUNT
    }
}
