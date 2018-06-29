package com.example.lockydy.tester;

//映射出服务器返回给我的结果
public class Result {
    private int code;
    private String text;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
