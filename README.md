# PrinterDome
## 佳博USB打印

  最近公司对接好多打印机，最后找到佳博。对接的时候有很多的坑，在没有技术对接的情况下我只能一个一个坑的探
  首先有必要讲明一下必须完成和注重的地方
  
  ### 准备工作（必要）
  
  主要是文档上说的，看文档真的很有必要。
  #### 1-sdk大致分为两部分，打印机与客显,不使用客显可以不添加so。如果只考虑打印就只把libs文件的jar拷贝的自己的项目
  同样这也是我要实现的部分。
  
  #### 2-注册服务和权限
  
     <?xml version="1.0" encoding="utf-8"?>
     <manifest xmlns:android="http://schemas.android.com/apk/res/android"
     package="com.sample"
     android:versionCode="1"
     android:versionName="1.0" >
             <uses-sdk
             android:minSdkVersion="14"
             android:targetSdkVersion="22" />
             <uses-permission android:name="android.permission.READ_PHONE_STATE" />
             <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
             <uses-permission android:name="android.permission.INTERNET" />
             <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
             <uses-permission android:name="android.permission.BLUETOOTH" />
             <uses-permission android:name="android.hardware.usb.accessory" />
             <uses-permission android:name="android.permission.WAKE_LOCK" />
             <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
             <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
             <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
             <uses-permission android:name="android.permission.GET_TASKS" />
             <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
             <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
             <uses-permission android:name="android.permission.WRITE_SETTINGS" />
             <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
             <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
             <uses-feature android:name="android.hardware.usb.host" />
         <application
         android:allowBackup="true"
         android:icon="@drawable/launcher"
         android:label="@string/app_name"
         android:theme="@style/AppTheme" >
             <service
             android:name="com.gprinter.service.GpPrintService"
             android:enabled="true"
             android:exported="true"
             android:label="GpPrintService" >
                 <intent-filter>
                 <action android:name="com.gprinter.aidl.GpPrintService" />
                 </intent-filter>
             </service>
             <service android:name="com.gprinter.service.AllService" >
             </service>
         </application>
     </manifest>

####  3-添加aidl 文件

其内容大致为


    package com.gprinter.aidl;
        interface GpService{
        int openPort(int PrinterId,int PortType,String DeviceName,int PortNumber);
        void closePort(int PrinterId);
        int getPrinterConnectStatus(int PrinterId);
        int printeTestPage(int PrinterId);
        void queryPrinterStatus(int PrinterId,int Timesout,int requestCode);
        int getPrinterCommandType(int PrinterId);
        int sendEscCommand(int PrinterId, String b64);
        int sendLabelCommand(int PrinterId, String b64);
        void isUserExperience(boolean userExperience);
        String getClientID();
        int setServerIP(String ip, int port);
    }
    
 ####  4-启动并绑定PrinterPrintService 服务
 
       private PrinterServiceConnection conn = null;
       class PrinterServiceConnection implements ServiceConnection {
           @Override
           public void onServiceDisconnected(ComponentName name) {
           Log.i("ServiceConnection", "onServiceDisconnected() called");
           mGpService = null;
           }
           @Override
           public void onServiceConnected(ComponentName name, IBinder service) {
           mGpService = GpService.Stub.asInterface(service);
           }
       }
       @Override
       public void onCreate(Bundle savedInstanceState) {
       conn = new PrinterServiceConnection();
       Intent intent = new Intent(this, GpPrintService.class);
       bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
       }
       
 #### 5-使用打印服务
 
 到了这一步我就开始跳坑了，一步两步似魔鬼的步伐，我就开始一步一步把自己遇到的坑的解决方法贴出来
 
 1、USB打印调用int openPort(int PrinterId,int PortType,String DeviceName,int PortNumber);
 
   该接口的作用主要是打开客户端打开客户端与打印机通讯端口，该接口会通过广播返回PrinterId的打印机的连接状态。这就是我遇到的第一个坑，如果仔细看文档的朋友可以看到‘DeviceName’该字段需要获取USB链接设备的名字的
   我该怎么获得呢，静下心深挖以后，终于找到了方法。
   
   