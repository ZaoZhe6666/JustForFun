package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String LocalHost = "172.24.38.234";
    private static String TestLog = "TestLog";
    public static String YAYA_PATH = "yaya/DCIM/SOAY";
    public static String BACK_PATH = "yaya/DCIM/BACK";
    public static String BACK_DATA_PATH = "yaya/DCIM/BACK/data";
    public static String BACK_TMP_PATH = "yaya/DCIM/BACK/data/thumb";
    public static String BACK_DIAGNO_PATH = "yaya/DCIM/BACK/data/diagno";

    public static int port = 5000;

    private boolean hasLogin = false;

    private static File photo;

    private static int TAKECAMERA = 100;
    private static int LOGININTENT = 200;
    private static int REGISINTENT = 300;
    private static int WATCHINTENT = 400;
    private static int SENDPICINTENT = 500;
    private static int CUTPICINTENT = 600;

    private static int NOTLOGIN = 201;

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_main);

        hasLogin = false;
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImage.setVisibility(View.INVISIBLE);


        // 设置服务器地址及端口号
        Button btn_SetPort = (Button)findViewById(R.id.tabbutton_set);
        btn_SetPort.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "dialog button listen");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View changeServerView = factory.inflate(R.layout.change_server, null);

                final EditText inputServer = (EditText) changeServerView.findViewById(R.id.text_server);
                final EditText inputPort = (EditText) changeServerView.findViewById(R.id.text_port);

                Log.d(TestLog, "init var");

                inputServer.setHint(LocalHost);
                inputPort.setHint("" + port);

                Log.d(TestLog, "init hint over");

                builder.setTitle("修改服务器信息");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setView(changeServerView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String iServer = inputServer.getText().toString();
                        String iPort = inputPort.getText().toString();

                        Log.d(TestLog, "the change :" + iServer + "/" + iPort);

                        // 合法性审查
                        if(inputCheckServer(iServer)) {
                            Log.d(TestLog, "change Server");
                            LocalHost = iServer;
//							outServer.setText("当前服务器：" + iServer);
                        }
                        int inputPort;
                        try {
                            if((inputPort = inputCheckPort(iPort)) != -1) {
                                Log.d(TestLog, "change Port");
                                port = inputPort;
//								outPort.setText(out);
                            }
                        }catch(Exception e) {
                            Log.d(TestLog, e.getMessage());
                        }
                        Log.d(TestLog, "After the change :" + LocalHost + "/" + port);
                    }
                    private boolean inputCheckServer(String iServer) {
                        // 参考资料https://blog.csdn.net/chaiqunxing51/article/details/50975961/
                        if(iServer == null || iServer.length() == 0) { // 基础检验
                            return false;
                        }
                        String[] parts = iServer.split("\\.");
                        if(parts.length != 4) { // 四段ip设置
                            return false;
                        }
                        for(int i = 0; i < 4; i++) {
                            try {
                                int n = Integer.parseInt(parts[i]);
                                if(n< 0 || n > 255) return false; // ip数检验
                            }catch(NumberFormatException e) {
                                return false; // 非法字符检验
                            }
                        }
                        return true;
                    }
                    private int inputCheckPort(String iPort) {
                        try {
                            int port = Integer.parseInt(iPort);
                            if(1024 < port && port < 65535) {
                                return port;
                            }
                        }catch(NumberFormatException e) {
                        }
                        return -1;
                    }
                    private int inputCheckColor(String iColor) {
                        try {
                            String regex="^#[A-Fa-f0-9]{6}$";
                            if(iColor.matches(regex)) {
                                return 0;
                            }
                        }catch(Exception e) {
                        }
                        return -1;
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });

        // 查看相册功能
        Button watchButton = (Button) findViewById(R.id.tabbutton_see);
        watchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d(TestLog, "View the Album");

                // 先刷新 后浏览

                File file = preCreateDir(YAYA_PATH);

                scanDir(MainActivity.this, file.getAbsolutePath());

                File backDir = preCreateDir(BACK_PATH);
                scanDir(MainActivity.this, backDir.getAbsolutePath());

                Log.d(TestLog, "Send Broadcast");

                String intentact = "";
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
                    intentact = Intent.ACTION_PICK;
                } else {//4.4版本后
                    intentact = Intent.ACTION_GET_CONTENT;
                }
                Intent intent = new Intent(intentact);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                Log.d(TestLog, "Look the Album");
                startActivityForResult(intent, WATCHINTENT);

            }

        });

        // 主界面拍照按钮
        Button cameraButton = (Button) findViewById(R.id.tabbutton_take);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Took photo");

                Log.d(TestLog, "Check If App is installed");
                if(!CallYaYa.checkYaYaExist(MainActivity.this)){
                    // 未安装YaYa APP - 提示及跳转下载页面

                    Log.d(TestLog, "YaYa App is not installed");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("出错误啦");
                    builder.setMessage("未下载辅助APP！");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setNegativeButton("点击下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // 跳转到下载页面
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse("https://android.myapp.com/myapp/detail.htm?apkName=com.wifidevice.coantec.activity#");
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    });
                    builder.setPositiveButton("稍后再说", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }

                Intent intent = new Intent();
                //包名 包名+类名（全路径）
                ComponentName comp = new ComponentName("com.wifidevice.coantec.activity","com.methnm.coantec.activity.MainActivity");
                intent.setComponent(comp);
                intent.setAction("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("data", "123");
                startActivity(intent);
            }
        });

        // 主界面上传图像功能
        Button sendPic = (Button) findViewById(R.id.tabbutton_update);
        sendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换至查看已有相册事件
                File dir = preCreateDir(YAYA_PATH);

                // 先刷新后选择
                scanDir(MainActivity.this, dir.getAbsolutePath());

                String intentact = "";
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
                    intentact = Intent.ACTION_PICK;
                } else {//4.4版本后
                    intentact = Intent.ACTION_GET_CONTENT;
                }
                Intent intent = new Intent(intentact);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                startActivityForResult(intent, SENDPICINTENT);
            }
        });

        // 查看历史记录功能
        ImageView checkHistory = (ImageView) findViewById(R.id.img_tab_history);
        checkHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CheckHistoryActivity.class);
                startActivity(intent);
            }
        });

        // 查看图片完毕
        ImageView imageRecover = (ImageView)findViewById(R.id.ivImage);
        imageRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivImage.setVisibility(View.INVISIBLE);
            }
        });
    }


    protected void onActivityResult(int requestCode, int result, Intent data) {
        Log.d(TestLog, "requeseCode = " + requestCode);
        if(requestCode == TAKECAMERA){
            // 拍照功能已改为调用已有YaYa APP对应功能
        }
        else if(requestCode == WATCHINTENT) {
            Log.d(TestLog, "show select pic:");
            // 显示图片
            if(data == null){
                Log.d(TestLog, "Select Pic Cancel");
                return;
            }
            Uri uri = data.getData();
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            startActivity(intent);
        }
        else if(requestCode == SENDPICINTENT){ // 向服务器上传图片 阶段1：裁剪图片
            Log.d(TestLog, "SEND PIC INTENT 1");
            // 查看已有相册图片 -> 建立连接发送图片 -> 接收图片
            Uri uri = data.getData();
//            String sendPath = UriDeal.Uri2Path(MainActivity.this, uri);
//            String sendPath = uri.getPath();
            String sendPath = UriDeal.getFilePathFromContentUri(uri, this.getContentResolver());
            Log.d(TestLog, "img path " + sendPath);
            if(sendPath == null){
                Log.d(TestLog, "illegal img path : null");
                return;
            }
            cropPic(sendPath);
        }

        else if(requestCode == CUTPICINTENT) { // 向服务器上传图片 阶段2：真正上传图片
            Log.d(TestLog, "SEND PIC INTENT 2");
            Log.d(TestLog, "img path " + photo.getAbsolutePath());
            String filePath = photo.getAbsolutePath();

            // 保存截取图片
            if (data != null) {
                Bitmap bitmap = data.getExtras().getParcelable("data");
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);

                try {
                    File fileCutSave = new File(filePath);

                    //裁剪后删除拍照的照片
                    if (fileCutSave.exists()) {
                        fileCutSave.delete();
                        Log.d(TestLog, "Delete Pic :" + fileCutSave.getAbsolutePath());
                    }

                    // 重新保存
                    FileOutputStream out = new FileOutputStream(fileCutSave);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.d(TestLog, "ERROR IN SAVE CUT PIC:" + e.getMessage());
                }
            }
            scanFile(MainActivity.this, filePath);
            Log.d(TestLog, "UPDATE CUT PIC:" + filePath);
            // 发送图片
            new Thread(new SocketSendGetThread(new File(filePath))).start();
        }
    }

    public static void setPhoto(File file){
        photo = file;
    }


    public final int CROP_PHOTO = 10;
    public final int ACTION_TAKE_PHOTO = 20;


    /**
     * 获取本应用在系统的存储目录
     */
    public static String getAppFile(Context context, String uniqueName) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
                && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getParent();
        } else {
            cachePath = context.getCacheDir().getParent();
        }
        return cachePath + File.separator + uniqueName;
    }

    /**
     * 跳转到系统裁剪图片页面
     * @param imagePath 需要裁剪的图片路径
     */
    private void cropPic(String imagePath) {
        File file = new File(imagePath);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "console.live.camera.fileprovider", file);
            intent.setDataAndType(contentUri, "image/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "image/*");
        }
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0.1);
        intent.putExtra("aspectY", 0.1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        this.photo = file;
        startActivityForResult(intent, CUTPICINTENT);
    }

    private File preCreateDir(String path){
        File dir = new File(Environment.getExternalStorageDirectory(), path);
        if(dir.exists() && dir.isFile()) {
            dir.delete();
        }
        if(!dir.exists()) {
            dir.mkdirs();
        }
        Log.d(TestLog, "pre Create Dir:" + dir.getAbsolutePath());
        return dir;
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0) {
                Log.d(TestLog, "in handle Message!");
                Bitmap bitmap = (Bitmap) msg.obj;
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);
            }
            else if(msg.what <= 5){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String congraText = "";
                if(msg.what == 1) congraText = "注册成功";
                else if(msg.what == 2) {
                    congraText = "登陆成功";
                    hasLogin = true;
                }
                builder.setTitle("恭喜！") ;
                builder.setMessage(congraText);
                builder.setPositiveButton("确定",null );
                builder.show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("出错误啦！") ;
                String errorText = "";
                if(msg.what == 1) errorText = "用户名不存在";
                else if(msg.what == 2) errorText = "用户名已被注册";
                else if(msg.what == 3) errorText = "未识别到本机摄像头";
                else if(msg.what == 404) errorText = "与服务器" + LocalHost + ":" + port + "连接失败";
                builder.setMessage(errorText);
                builder.setPositiveButton("确定",null );
                builder.show();
            }
        }
    };

    // 通过Socket进行图片收发
    public class SocketSendGetThread implements Runnable{
        private File file;
        public SocketSendGetThread(File file) {
            this.file = file;
        }
        @Override
        public void run() {
            Log.d(TestLog, "SocketSendImg");
            Socket socket;
            try {
                // 创建Socket 指定服务器IP和端口号
                socket = new Socket(LocalHost, port);

                // 创建InputStream用于读取文件
                InputStream inputFile = new FileInputStream(file);

                // 创建Socket的InputStream用来接收数据
                InputStream inputConnect = socket.getInputStream();

                // 创建Socket的OutputStream用于发送数据
                OutputStream outputConnect = socket.getOutputStream();

                // 发送识别码
                outputConnect.write("Picture".getBytes());
                outputConnect.flush();

                // send分隔 div 1
                inputConnect.read(new byte[10]);

                // 发送文件大小
                long fileSize = inputFile.available();
                String fileSizeStr = fileSize + "";
                outputConnect.write(fileSizeStr.getBytes());
                outputConnect.flush();

                // send分隔 div 2
                inputConnect.read(new byte[10]);

                //将本地文件转为byte数组
                byte buffer[] = new byte[4 * 1024];
                int tmp = 0;
                // 循环读取文件
                while((tmp = inputFile.read(buffer)) != -1) {
                    outputConnect.write(buffer, 0, tmp);
                }

                // 发送读取数据到服务端
                outputConnect.flush();

                // 关闭输入流
                inputFile.close();

                // 通过socket与RequestURL建立连接，并接受一张图片存到本地
                Log.d(TestLog, "SocketGetImg");

                // 接收返回码
                byte symCodeBuff[] = new byte[200];
                int symCode = inputConnect.read(symCodeBuff);
                String symCodeStr = new String(symCodeBuff, 0, symCode);
                symCode = Integer.valueOf(symCodeStr);
                Log.d(TestLog, "Sym Code is " + symCode);

                // 返回码表明有误
                if(symCode != 0) {
                    // 设置返回信息
                    android.os.Message message = Message.obtain();
                    message.obj = null;
                    message.what = symCode;
                    Log.d(TestLog, "message is ok");
                    handler.sendMessage(message);
                    Log.d(TestLog, "handler is ok");

                    inputConnect.close();
                    outputConnect.close();
                    socket.close();
                    return;
                }

                // 发送分隔符
                outputConnect.write("BreakTime".getBytes());
                outputConnect.flush();

                // 定位输出路径
                File dir = preCreateDir(BACK_PATH);
                File dir_data = preCreateDir(BACK_DATA_PATH);
                File dir_thumb = preCreateDir(BACK_TMP_PATH);
                File dir_diagno = preCreateDir(BACK_DIAGNO_PATH);

                // 使用时间作为输出
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timePath =  dateFormat.format(date);
                String filePath = dir.getAbsolutePath() + "/Receive_" + timePath + ".jpg";
                FileOutputStream outputStream = new FileOutputStream(filePath);

                // 读取接收文件大小
                byte piclenBuff[] = new byte[200];
                int picLen = inputConnect.read(piclenBuff);
                String picLenStr = new String(piclenBuff, 0, picLen);
                picLen = Integer.valueOf(picLenStr);
                Log.d(TestLog, "fileSize is:" + picLen);

                // 发送确认信息
                outputConnect.write("receive".getBytes());
                outputConnect.flush();

                // 读取接收文件
                byte buffer2[] = new byte[picLen];
                int offset = 0;
                while(offset < picLen) {
                    int len = inputConnect.read(buffer2, offset, picLen - offset);
                    Log.d(TestLog, "" + len);
                    outputStream.write(buffer2, offset, len);
                    offset += len;
                }
                Log.d(TestLog, "yeah");

                // 发送图片接收确认信息
                outputConnect.write("pic receive".getBytes());
                outputConnect.flush();

                // 存储评价信息
                byte buffer3[] = new byte[1024];
                int diagLen = inputConnect.read(buffer3);
                String diag = new String(buffer3, 0, diagLen);
                Log.d(TestLog, diag);
                File diagFile = new File(dir_diagno.getAbsolutePath() + "/Diagno_" + timePath + ".txt");
                diagFile.createNewFile();
                FileWriter diagFW = new FileWriter(diagFile);
                BufferedWriter diagBW = new BufferedWriter(diagFW);
                diagBW.write(diag + "\n");
                diagBW.close();
                diagFW.close();


                inputConnect.close();
                outputStream.close();

                // 关闭连接
                socket.close();
                Log.d(TestLog, "Get Img success.The result is " + filePath);
                if(filePath.equals("")) return;

                // 创建显示用bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer2, 0, offset);
                Log.d(TestLog, "bitmap is ok");

                // 存储缩略图
                String thumbPath = dir_thumb.getAbsolutePath() + "/Thumb_" + timePath + ".jpg";
                BufferedOutputStream buffStream = new BufferedOutputStream(new FileOutputStream(new File(thumbPath)));
                Bitmap bitmap_thumb = Bitmap.createScaledBitmap(bitmap, 100 , 100, true);
                bitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, buffStream);
                buffStream.flush();
                buffStream.close();

                // 存储时间戳信息
                String historyPath = dir_data.getAbsolutePath() + "/History.txt";
                File historyFile = new File(historyPath);
                if(historyFile.exists() && historyFile.isDirectory()){
                    historyFile.delete();
                    historyFile.createNewFile();
                }
                else if(!historyFile.exists()){
                    historyFile.createNewFile();
                }

                FileWriter fw = new FileWriter(historyFile, true);
                BufferedWriter bw=new BufferedWriter(fw);
                bw.write(timePath + "\n");
                bw.close();
                fw.close();

                android.os.Message message = Message.obtain();
                message.obj = bitmap;
                message.what = 0;
                Log.d(TestLog, "message is ok");
                handler.sendMessage(message);
                Log.d(TestLog, "handler is ok");

            }catch(Exception e) {
                Log.d(TestLog, "catch error:" + e.getMessage());
            }

        }

    }

    // 文件刷新
    public static void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    // 文件夹刷新
    public static void scanDir(Context context, String dir) {
        File[] files = new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        if(files == null) return;

        String[] paths = new String[files.length];
        for (int co = 0; co < files.length; co++) {
            paths[co] = files[co].getAbsolutePath();
            Log.d(TestLog, "Scan File :" + files[co].getAbsolutePath());
            scanFile(context, paths[co]);
        }
    }

}
