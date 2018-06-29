package com.example.lockydy.tester;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

//聊天信息适配器 起到了一个信息转换的作用
public class MessageAdapter extends BaseAdapter {
    private List<ChatMessage> list;
    //构造函数
    public MessageAdapter(List<ChatMessage> list){
        this.list=list;
    }

    @Override
    public int getCount(){
        return list.isEmpty()?0:list.size();
    }

    @Override
    public Object getItem(int position){
        return list.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemViewType(int position){
        ChatMessage chatMessage=list.get(position);
        //接收信息 0 发送消息是1
        if (chatMessage.getType()==ChatMessage.Type.INCOUNT){
            return 0;
        }
        else
            return 1;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }
    //判断信息来源是用户还是服务器 从而决定我要调用哪个框架进行显示
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View covertView, ViewGroup parent){
        ChatMessage chatMessage=list.get(position);
        if (covertView==null){
            ViewHolder viewHolder;
            if (getItemViewType(position)==0){
                covertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.left_layout,null);
                viewHolder=new ViewHolder();
                viewHolder.chat_time=covertView.findViewById(R.id.left_time);
                viewHolder.chat_message=covertView.findViewById(R.id.left_message);
            }
            else{
                covertView=LayoutInflater.from(parent.getContext()).inflate(R.layout.right_layout,null);
                viewHolder=new ViewHolder();
                viewHolder.chat_time=covertView.findViewById(R.id.right_time);
                viewHolder.chat_message=covertView.findViewById(R.id.right_message);
            }
            covertView.setTag(viewHolder);
        }
        ViewHolder vher=(ViewHolder)covertView.getTag();
        vher.chat_time.setText(DataUtils.dateString(chatMessage.getDate()));
        vher.chat_message.setText(chatMessage.getMessage());
        return covertView;
    }

    private class ViewHolder{
        private TextView chat_time,chat_message;
    }
}