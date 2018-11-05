# PrinterDome
## 佳博USB打印

  最近公司对接好多打印机，最后找到佳博。对接的时候有很多的坑，在没有技术对接的情况下我只能一个一个坑的探
  首先有必要讲明一下必须完成和注重的地方
  
  ### 准备工作（必要）
  
  主要是文档上说的，看文档真的很有必要。
  1-sdk 大致分为两部分，打印机与客显,不使用客显可以不添加so。如果只考虑打印就只把libs文件的jar拷贝的自己的项目
  同样这也是我要实现的部分。
  2-注册服务和权限
  
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

