package com.example.lockydy.tester;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.net.HttpURLConnection;


//发送http请求给服务器
//是我们最为重要的一步行为
//如果这一步处理不当我们的信息就无法进行传输 从而无法实现AI机器人
public class HttpUtils {


    //得到数据了 进行一下处理行为
    public static ChatMessage sendMessage(String message){
        ChatMessage chatMessage=new ChatMessage();
        String gsonResult=Togeter(message);
        Gson gson=new Gson();//利用gson来解析数据
        Result result;
        //下面执行活动
        if(gsonResult!=null){
            //数据返回
            try {
                result=gson.fromJson(gsonResult,Result.class);//获得解析数据
                chatMessage.setMessage(result.getText());//将解析好了的数据返回给我们的message进行显示
                //从而实现AI机器人的回答
            }catch (Exception e){
                chatMessage.setMessage("不好意思哦~，祥仔正在休息ing...如果想要和祥仔对话~请过一会儿再来吧~");
            }
        }
        chatMessage.setDate(new Date());//不管是否成功得到数据 都要显示时间 此处设置时间
        chatMessage.setType(ChatMessage.Type.INCOUNT);//此处标记这是得到的数据 从而得出处理方式
        return chatMessage;
    }

    //设置参数 发送信息返回url 这是我们要发的东西~ 把格式传给Together函数
    private  static String setParmat(String message){
        String url="";
        try{
            url=config.Url_key+"?"+"key="+config.App_key+"&info=" + URLEncoder.encode(message,"UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return url;
    }

    //Get发送我们的数据到服务器 请求服务器给我们发Json数据回来
    public  static String Togeter(String message){
        String result="";//初始化结果
        String url=setParmat(message);//设置参数
        //System.out.println("------------url = " + url);
        InputStream inner=null;//输入流用来检验结果
        ByteArrayOutputStream banner=null;//最终结果按数组输出
        try {
            URL urls=new URL(url);//得到地址
            //注意此时就是在进行数据发送了！
            //千万注意是Http不是Https 否则头文件不同 数据传输不同 从而无法得到最终结果
            HttpURLConnection connection=(HttpURLConnection)urls.openConnection();
            //设置超时时间 和请求的方式
            connection.setReadTimeout(3*1000);
            connection.setConnectTimeout(3*1000);
            connection.setRequestMethod("GET");
            //得到返回结果
            inner=connection.getInputStream();
            banner=new ByteArrayOutputStream();
            int len;
            byte[] buff=new byte[1024];
            //检验返回结果 并写入buff数组中 从而实现结果
            while ((len=inner.read(buff))!=-1){
                banner.write(buff,0,len);
            }
            banner.flush();
            result=new String(banner.toByteArray());
            //把得到的字节数组转换成string字符串发送给结果 得到我们所要的信息
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //文件流结束的时候一定要关闭!!不然程序会报错的!!!!
            if (inner!=null){
                try {
                    inner.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            if (banner!=null){
                try{
                    banner.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}