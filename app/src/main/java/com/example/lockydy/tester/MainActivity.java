package com.example.lockydy.tester;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import utils.JsonParser;

public class MainActivity extends Activity {

    private com.iflytek.cloud.SpeechRecognizer speechRecognizer;//创建一个语音听写对象
    private RecognizerDialog recognizerDialog;//语音听写界面
    private boolean isShowDialog = true;//是否显示听写UI
    private SharedPreferences sharedPreferences;//缓存
    private HashMap<String, String> hashMap = new LinkedHashMap<>();//存储听写结果
    private String mEngineType = null;//引擎类型（云端）
    private List<ChatMessage> list;//文本序列
    private ListView chat_listview;//聊天list
    private EditText chat_input;//文本框
    private Button chat_send;//发送键
    private MessageAdapter chatAdapter;//适配器
    private ChatMessage chatMessage=null;//聊天信息
    private int i=1;


    //主函数 给出初始化界面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initData();
        initDate();
        //设置一个Toast来提醒 后台服务功能开启
        Toast.makeText(MainActivity.this,"祥仔已经为您开启后台服务啦",Toast.LENGTH_LONG).show();
    }

    //页面 监听器 数据
    private void initView(){
        chat_listview=findViewById(R.id.list_view);
        chat_send=findViewById(R.id.send_button);
        chat_input=findViewById(R.id.input_message);
    }

    //监听函数
    private void initListener(){
        chat_send.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.send_button:
                {
                    chat();
                    break;
                }
            }
        }
    };
    //初始化数据
    private void initData(){
        list=new ArrayList<>();
        list.add(new ChatMessage("hello 我是祥仔 初次见面请多指教哦~",ChatMessage.Type.INCOUNT,new Date()));
        chatAdapter=new MessageAdapter(list);
        chat_listview.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();
    }

    private void initDate(){
        //此句代码应该放在application中的，这里为了方便就直接放这里了
        SpeechUtility.createUtility(this, "appid=5af691f1");
        recognizerDialog = new RecognizerDialog(this, initListener);
        speechRecognizer=com.iflytek.cloud.SpeechRecognizer.createRecognizer(this,initListener);
        sharedPreferences = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        //这里将引擎类型设置为云端 将数据发送到云端
        mEngineType = SpeechConstant.TYPE_CLOUD;
    }

    //发送信息聊天
    private void chat() {
        //1.判断是否输入内容 假如没有 就不同意他输出
        final String send_message = chat_input.getText().toString().trim();
        if (TextUtils.isEmpty(send_message)) {
            Toast.makeText(MainActivity.this, "sorry 没有输入任何消息 不可以给您发送哦", Toast.LENGTH_SHORT).show();
            return;
        }
        //监测信息输入并进行信息分类
        ChatMessage sendChatMessage = new ChatMessage();
        sendChatMessage.setMessage(send_message);
        sendChatMessage.setDate(new Date());
        sendChatMessage.setType(ChatMessage.Type.OUTCOUNT);//标记输出
        list.add(sendChatMessage);//文本中增加新信息
        chatAdapter.notifyDataSetChanged();//适配器更新
        chat_input.setText("");//文本框清空
        chat_listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //多线程启动
        new Thread(){
            public void run(){
                ChatMessage chat=HttpUtils.sendMessage(send_message);
                Message message=new Message();
                message.what=0X1;
                message.obj=chat;
                handler.sendMessage(message);
            }

        }.start();
    }

    //Thread类+Handler类处理多线程问题
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){

        public void handleMessage(android.os.Message msg){
            if (msg.what==0X1){
                if (msg.obj!=null){
                    chatMessage=(ChatMessage)msg.obj;
                }
                list.add(chatMessage);
                chatAdapter.notifyDataSetChanged();
            }
        }
    };


    //开始听写 初始化参数 并开始记录
    public void start(View view) {
        hashMap.clear();
        setParams();
        if (isShowDialog) {
            recognizerDialog.setListener(dialogListener);
            recognizerDialog.show();
        }
    }


    //结束听写
    public void stop(View view) {
        if (isShowDialog) {
            recognizerDialog.dismiss();
        } else {
           speechRecognizer.stopListening();
        }
    }
    //初始化监听器
    private InitListener initListener = new InitListener() {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Log.e("tag", "初始化失败，错误码" + i);
            }
        }
    };

    //监听器
    private RecognizerDialogListener dialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            if (recognizerResult != null) {
                Log.e("tag", "听写结果：" + recognizerResult.getResultString());
                printResult(recognizerResult);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e("tag", speechError.getPlainDescription(true));
        }
    };

    //输出结果，将返回的json字段解析并在textView中显示 同时保证UI能够处于List最底端
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hashMap.put(sn, text);
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : hashMap.keySet()) {
            resultBuffer.append(hashMap.get(key));
        }
        i++;//数据会从云端返回两次 因此需要进行一下处理
        if(i%2==0){
            i-=2;
            //System.out.println("此时的i值为"+i);
            chat_input.setText(resultBuffer.toString());
            chat();
        }
    }

    //设置参数
    private void setParams() {
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        //设置引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回数据类型
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //设置中文 普通话
        speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS,
                sharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS,
                sharedPreferences.getString("iat_vadeos_preference", "2000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        //为了保证结果正确 此时不设置
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT,
                sharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showMyDialog(); //点击BACK弹出对话框
        }
        return false;
    }

    //确认退出界面
    private void showMyDialog() {
        // 创建退出对话框
        AlertDialog isExit = new AlertDialog.Builder(this).create();
        // 设置对话框标题
        isExit.setTitle("From祥仔的小提醒");
        // 设置对话框消息
        isExit.setMessage("真的不想再和祥仔多聊一下下吗？");
        // 添加选择按钮并注册监听
        isExit.setButton("真的不想要聊下去了", listener);
        isExit.setButton2("那就再聊一会儿", listener);
        // 显示对话框
        isExit.show();
    }
    /**
     * 监听退出的button点击事件
     */
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    //后台运行提醒功能
    @Override
    protected void onUserLeaveHint() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder3=new Notification.Builder(this);
        Intent intent3=new Intent(this,MainActivity.class);
        PendingIntent pendingIntent3=PendingIntent.getActivity(this,0,intent3,0);
        builder3.setContentIntent(pendingIntent3);
        builder3.setSmallIcon(R.drawable.qiaoqiao);
        builder3.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.qiaoqiao));
        builder3.setAutoCancel(true);
        builder3.setContentTitle("来自祥仔的问候");
        builder3.setContentText("主人，我在后台运行，记得回来看看我");
        builder3.setWhen(System.currentTimeMillis());
        builder3.setSmallIcon(R.drawable.tn);
        builder3.setDefaults(Notification.DEFAULT_SOUND);//获取默认铃声
        builder3.setLights(Color.RED,1000,1000);//设置呼吸灯

        Intent XuanIntent=new Intent();
        XuanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent xuanpengdIntent=PendingIntent.getActivity(this,0,XuanIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        XuanIntent.setClass(this,MainActivity.class);
        builder3.setFullScreenIntent(xuanpengdIntent,true);
        mNotificationManager.notify(2,builder3.build());
        super.onUserLeaveHint();
    }
}