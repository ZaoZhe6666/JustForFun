package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CheckHistoryActivity extends Activity{
    private static String TestLog = "TestLog";
    private MyAdapter adapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_check);
        Log.d(TestLog, "Check History Activity");

        ListView listView = (ListView) findViewById(R.id.history_list);
        List<HistoryLog> historyList = new ArrayList<>();

        // 逐行读取History.txt中内容
        File HistoryFile = new File(Environment.getExternalStorageDirectory(),MainActivity.BACK_DATA_PATH + "/History.txt");
        Log.d(TestLog, "Begin to Read " + HistoryFile.getAbsolutePath());
        try {
            if(HistoryFile.exists()){
                BufferedReader hisReader = new BufferedReader(new InputStreamReader(new FileInputStream(HistoryFile),"UTF-8"));
                String lineTime = null;
                while ((lineTime = hisReader.readLine()) != null) {
                    Log.d(TestLog, "lineTime is:" + lineTime);
                    HistoryLog his = new HistoryLog(lineTime);
                    historyList.add(his);
                }
                hisReader.close();
            }
            else{
                Log.d(TestLog, "History.txt not exist");
            }
        } catch (Exception e) {
            Log.d(TestLog, "Error when Read History.txt : " + e.getMessage());
        }

        Log.d(TestLog, "the history log has :" + historyList.size() + " items");
        adapter = new MyAdapter(historyList, CheckHistoryActivity.this);
        listView.setAdapter(adapter);
    }

    public class MyAdapter extends BaseAdapter {

        private List<HistoryLog> historyItemsList;
        private LayoutInflater inflater;

        public MyAdapter() {
        }

        public MyAdapter(List<HistoryLog> historyItemsList, Context context) {
            this.historyItemsList = historyItemsList;
            this.inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return historyItemsList == null ? 0 : historyItemsList.size();
        }

        @Override
        public HistoryLog getItem(int position) {
            Log.d(TestLog, "Get position:" + position);
            return historyItemsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //加载布局为一个视图
            View view = inflater.inflate(R.layout.history_items, null);
            HistoryLog historyLog = getItem(position);

            //在view视图中查找控件
            ImageView image_photo = (ImageView) view.findViewById(R.id.history_pic);
            TextView tv_date = (TextView) view.findViewById(R.id.history_time);
            View item = (View) view.findViewById(R.id.history_item);

            // 设置缩略图显示
            if(historyLog.getThumbPicPath() == ""){
                Log.d(TestLog, "The Thumb Pic is not exist");
                image_photo.setImageResource(historyLog.getInitPic());
            }
            else{
                Log.d(TestLog, "The Thumb Pic is set");
                image_photo.setImageBitmap(BitmapFactory.decodeFile(historyLog.getThumbPicPath()));
            }

            // 设置时间戳显示
            tv_date.setText(String.valueOf(historyLog.getdate()));

            // 设置点击弹窗
            item.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d(TestLog, "onClick history item: " + historyLog.getdate());
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckHistoryActivity.this);
                    LayoutInflater factory = LayoutInflater.from(CheckHistoryActivity.this);
                    View historyCheckView = factory.inflate(R.layout.history_display, null);

                    TextView dis_time = (TextView) historyCheckView.findViewById(R.id.history_dis_time);
                    TextView dis_else = (TextView) historyCheckView.findViewById(R.id.history_dis_else);
                    ImageView dis_pic = (ImageView) historyCheckView.findViewById(R.id.history_dis_pic);

                    Log.d(TestLog, "init var");

                    dis_time.setText("上传时间:" + historyLog.getdate());
                    dis_else.setText("诊断结果:" + historyLog.getDiagno());
                    if(historyLog.getPicPath() == ""){
                        dis_pic.setImageResource(historyLog.getInitPic());
                    }
                    else{
                        dis_pic.setImageBitmap(BitmapFactory.decodeFile(historyLog.getPicPath()));
                    }


                    Log.d(TestLog, "init hint over");

                    builder.setTitle("历史记录");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setView(historyCheckView);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
            return view;
        }

    }
}
