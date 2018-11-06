package printer.test.printerdome;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.command.GpUtils;
import com.gprinter.command.LabelCommand;
import com.gprinter.io.PortParameters;
import com.gprinter.service.GpPrintService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import static com.gprinter.service.GpPrintService.PrinterId;


/**
 * 打印工具类
 */
public class PrinterUtils {
    private static Context context;
    private String DEBUG_TAG = "PrinterUtils";
    private String DeviceName = "";
    private GpService mGpService;
    private PrinterServiceConnection conn = null;
    //打印机状态监听
    private USBprinterCallBack usBprinterCallBack = null;
    //打印状态监听
    private PrinterCallBack callBack = null;

    //查询请求码
    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;

    private PortParameters mPortParam[] = new PortParameters[GpPrintService.MAX_PRINTER_CNT];

    public PrinterUtils(Context context) {
        this.context = context;
        DeviceName = getUsbDevices();
        connection();
    }

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.i(DEBUG_TAG, "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);

            initUSBDevices();
        }
    }


    /**
     * 启动打印服务
     */
    public void connection() {
        conn = new PrinterServiceConnection();
        Log.i(DEBUG_TAG, "connection");
        Intent intent = new Intent(context, GpPrintService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService

    }

    /**
     * 获取USB打印机的名字
     *
     * @return 返回的打印设备的名字， noDevices：没有获取到任何打印设备
     */
    private String getUsbDevices() {
        String usbname = "";
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = devices.values().iterator();
        int count = devices.size();
        Log.d(DEBUG_TAG, "count " + count);
        if (count > 0) {
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                String devicename = device.getDeviceName();
                if (checkUsbDevicePidVid(device)) {
                    Log.e(DEBUG_TAG, "DevicesName " + devicename);
                    usbname = devicename;
                }
            }
        } else {
            Log.e(DEBUG_TAG, "noDevices ");
            usbname = "noDevices";
        }
        return usbname;
    }

    /**
     * 判断是否是USB打印机
     *
     * @param dev
     * @return
     */
    private boolean checkUsbDevicePidVid(UsbDevice dev) {
        int pid = dev.getProductId();
        int vid = dev.getVendorId();
        boolean rel = false;
        if ((vid == 34918 && pid == 256) || (vid == 1137 && pid == 85) || (vid == 6790 && pid == 30084) || (vid == 26728 && pid == 256) || (vid == 26728 && pid == 512) || (vid == 26728 && pid == 256) || (vid == 26728 && pid == 768) || (vid == 26728 && pid == 1024) || (vid == 26728 && pid == 1280) || (vid == 26728 && pid == 1536)) {
            rel = true;
        }
        return rel;
    }

    /**
     * 链接USB打印设备
     */
    private void initUSBDevices() {
        int rel = 0;
        if (TextUtils.isEmpty(DeviceName)) {
            if (usBprinterCallBack != null) {
                usBprinterCallBack.cannotFind();
            }
            return;
        }
        try {
            rel = mGpService.openPort(PrinterId, PortParameters.USB, DeviceName, 0);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            switch (r) {
                case SUCCESS://正常
                    Log.e(DEBUG_TAG, "正常");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beSucceed();
                    }
                    break;
                case FAILED://失败
                    Log.e(DEBUG_TAG, "失败");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("失败");
                    }
                    break;
                case TIMEOUT:// 超时
                    Log.e(DEBUG_TAG, "超时");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("超时");
                    }
                    break;
                case INVALID_DEVICE_PARAMETERS://无效的参数
                    Log.e(DEBUG_TAG, "无效的参数");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("无效的参数");
                    }
                    break;
                case DEVICE_ALREADY_OPEN://端口已经打开
                    Log.e(DEBUG_TAG, "端口已经打开");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("端口已经打开");
                    }
                    break;
                case INVALID_PORT_NUMBER://无效的端口号
                    Log.e(DEBUG_TAG, "无效的端口号");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("无效的端口号");
                    }
                    break;
                case INVALID_IP_ADDRESS://无效的 ip 地址
                    Log.e(DEBUG_TAG, "无效的 ip 地址");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("无效的 ip 地址");
                    }
                    break;
                case INVALID_CALLBACK_OBJECT://无效的回调
                    Log.e(DEBUG_TAG, "无效的回调");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("无效的回调");
                    }
                    break;
                case BLUETOOTH_IS_NOT_SUPPORT://设备不支持蓝牙
                    Log.e(DEBUG_TAG, "设备不支持蓝牙");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("设备不支持蓝牙");
                    }
                    break;
                case OPEN_BLUETOOTH: //请打开蓝牙
                    Log.e(DEBUG_TAG, "请打开蓝牙");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("设备不支持蓝牙");
                    }
                    break;
                case PORT_IS_NOT_OPEN://端口未打开
                    Log.e(DEBUG_TAG, "端口未打开");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("端口未打开");
                    }
                    break;
                case INVALID_BLUETOOTH_ADDRESS://无效的蓝牙地址
                    Log.e(DEBUG_TAG, "无效的蓝牙地址");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("无效的蓝牙地址");
                    }
                    break;
                case PORT_IS_DISCONNECT://端口连接断开
                    Log.e(DEBUG_TAG, "端口连接断开");
                    if (usBprinterCallBack != null) {
                        usBprinterCallBack.beDefeated("端口连接断开");
                    }
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取打印机当前的连接状态
     *
     * @throws RemoteException
     */
    public void getPrinterStatus() throws RemoteException {
        mGpService.queryPrinterStatus(PrinterId, 500, MAIN_QUERY_PRINTER_STATUS);
    }

    /**
     * 断开客户端与打印机通讯端口  （断开的时候，如果需要再次连接的话，请确保打印机的状态已经是GpDevice.STATE_NONE 的状态）
     */
    public void unPrinterService() {
        if (conn != null) {
            context.unbindService(conn); // unBindService
        }
        context.unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 打印
     */
    public void sendReceipt() {

        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印居中
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
        esc.addText("Sample\n"); // 打印文字
        esc.addPrintAndLineFeed();

        /* 打印文字 */
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);// 取消倍高倍宽
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印左对齐
        esc.addText("Print text\n"); // 打印文字
        esc.addText("Welcome to use SMARNET printer!\n"); // 打印文字

        /* 打印繁体中文 需要打印机支持繁体字库 */
        String message = "佳博智匯票據打印機\n";
        // esc.addText(message,"BIG5");
        esc.addText(message, "GB2312");
        esc.addPrintAndLineFeed();

        /* 绝对位置 具体详细信息请查看GP58编程手册 */
        esc.addText("智汇");
        esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
        esc.addSetAbsolutePrintPosition((short) 6);
        esc.addText("网络");
        esc.addSetAbsolutePrintPosition((short) 10);
        esc.addText("设备");
        esc.addPrintAndLineFeed();


        /* 打印一维条码 */
        esc.addText("Print code128\n"); // 打印文字
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);//
        // 设置条码可识别字符位置在条码下方
        esc.addSetBarcodeHeight((byte) 60); // 设置条码高度为60点
        esc.addSetBarcodeWidth((byte) 1); // 设置条码单元宽度为1
        esc.addCODE128(esc.genCodeB("SMARNET")); // 打印Code128码
        esc.addPrintAndLineFeed();

        /*
         * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
         */
        esc.addText("Print QRcode\n"); // 打印文字
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); // 设置纠错等级
        esc.addSelectSizeOfModuleForQRCode((byte) 3);// 设置qrcode模块大小
        esc.addStoreQRCodeData("www.smarnet.cc");// 设置qrcode内容
        esc.addPrintQRCode();// 打印QRCode
        esc.addPrintAndLineFeed();

        /* 打印文字 */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印左对齐
        esc.addText("Completed!\r\n"); // 打印结束
        // 开钱箱
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        esc.addPrintAndFeedLines((byte) 8);

        Vector<Byte> datas = esc.getCommand(); // 发送数据
        byte[] bytes = GpUtils.ByteTo_byte(datas);
        String sss = Base64.encodeToString(bytes, Base64.DEFAULT);
        int rs;
        try {
            rs = mGpService.sendEscCommand(PrinterId, sss);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rs];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(context, GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 注册广播以监听打印状态
     */
    public void registerPrinterReceiver() {
        // 注册实时状态查询广播
        context.registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS));
        /**
         * 票据模式下，可注册该广播，在需要打印内容的最后加入addQueryPrinterStatus()，在打印完成后会接收到
         * action为GpCom.ACTION_DEVICE_STATUS的广播，特别用于连续打印，
         * 可参照该sample中的sendReceiptWithResponse方法与广播中的处理
         **/
        context.registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_RECEIPT_RESPONSE));
        /**
         * 标签模式下，可注册该广播，在需要打印内容的最后加入addQueryPrinterStatus(RESPONSE_MODE mode)
         * ，在打印完成后会接收到，action为GpCom.ACTION_LABEL_RESPONSE的广播，特别用于连续打印，
         * 可参照该sample中的sendLabelWithResponse方法与广播中的处理
         **/
        context.registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_LABEL_RESPONSE));
    }

    /**
     * 广播用以检测打印机状态
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("TAG", action);
            // GpCom.ACTION_DEVICE_REAL_STATUS 为广播的IntentFilter
            if (action.equals(GpCom.ACTION_DEVICE_REAL_STATUS)) {

                // 业务逻辑的请求码，对应哪里查询做什么操作
                int requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1);
                // 判断请求码，是则进行业务操作


                int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
                String str;
                if (status == GpCom.STATE_NO_ERR) {
                    str = "打印机正常";
                    if (callBack != null) {
                        callBack.beSucceed();
                    }
                } else {
                    str = "打印机 ";
                    if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
                        str += "脱机";
                        if (callBack != null) {
                            callBack.beDefeated(str);
                        }
                    }
                    if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
                        str += "缺纸";
                        if (callBack != null) {
                            callBack.beDefeated(str);
                        }
                    }
                    if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
                        str += "打印机开盖";
                        if (callBack != null) {
                            callBack.beDefeated(str);
                        }
                    }
                    if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
                        str += "打印机出错";
                        if (callBack != null) {
                            callBack.beDefeated(str);
                        }
                    }
                    if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
                        str += "查询超时";
                        if (callBack != null) {
                            callBack.beDefeated(str);
                        }
                    }
                }


            }
        }
    };

    /**
     * 吐丝方法
     *
     * @param message 吐丝内容
     */
    public void showToast(final String message) {
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    /**
     * USB打印链接状态监控接口
     */
    public interface USBprinterCallBack {
        /**
         * 未发现打印设备
         */
        void cannotFind();

        /**
         * 链接成功
         */
        void beSucceed();

        /**
         * 链接失败，
         *
         * @param details 失败原因
         */
        void beDefeated(String details);
    }

    /**
     * 设置监听
     *
     * @param usBprinterCallBack
     */
    public void onUSBprinterCallBack(USBprinterCallBack usBprinterCallBack) {
        this.usBprinterCallBack = usBprinterCallBack;
    }

    public interface PrinterCallBack {
        /**
         * 链接成功
         */
        void beSucceed();

        /**
         * 链接失败，
         *
         * @param details 失败原因
         */
        void beDefeated(String details);
    }

    /**
     * 设置监听
     *
     * @param callBack
     */
    public void onPrinterCallBack(PrinterCallBack callBack) {
        this.callBack = callBack;
    }

}
