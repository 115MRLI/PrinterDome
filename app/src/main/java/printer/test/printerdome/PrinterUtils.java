package printer.test.printerdome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.PortParameters;
import com.gprinter.service.GpPrintService;

import java.util.HashMap;
import java.util.Iterator;

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
        }
    }

    /**
     * 启动打印服务
     */
    private void connection() {
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
    boolean checkUsbDevicePidVid(UsbDevice dev) {
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
    public void initUSBDevices() {
        int rel = 0;
        if (TextUtils.isEmpty(DeviceName)) {
            showToast("请检查打印机设备是否连接");
            return;
        }
        try {
            rel = mGpService.openPort(PrinterId, PortParameters.USB, DeviceName, 0);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            switch (r) {
                case SUCCESS://正常
                    Log.e(DEBUG_TAG, "正常");
                    break;
                case FAILED://失败
                    Log.e(DEBUG_TAG, "失败");
                    break;
                case TIMEOUT:// 超时
                    Log.e(DEBUG_TAG, "超时");
                    break;
                case INVALID_DEVICE_PARAMETERS://无效的参数
                    Log.e(DEBUG_TAG, "无效的参数");
                    break;
                case DEVICE_ALREADY_OPEN://端口已经打开
                    Log.e(DEBUG_TAG, "端口已经打开");
                    break;
                case INVALID_PORT_NUMBER://无效的端口号
                    Log.e(DEBUG_TAG, "无效的端口号");
                    break;
                case INVALID_IP_ADDRESS://无效的 ip 地址
                    Log.e(DEBUG_TAG, "无效的 ip 地址");
                    break;
                case INVALID_CALLBACK_OBJECT://无效的回调
                    Log.e(DEBUG_TAG, "无效的回调");
                    break;
                case BLUETOOTH_IS_NOT_SUPPORT://设备不支持蓝牙
                    Log.e(DEBUG_TAG, "设备不支持蓝牙");
                    break;
                case OPEN_BLUETOOTH: //请打开蓝牙
                    Log.e(DEBUG_TAG, "请打开蓝牙");
                    break;
                case PORT_IS_NOT_OPEN://端口未打开
                    Log.e(DEBUG_TAG, "端口未打开");
                    break;
                case INVALID_BLUETOOTH_ADDRESS://无效的蓝牙地址
                    Log.e(DEBUG_TAG, "无效的蓝牙地址");
                    break;
                case PORT_IS_DISCONNECT://端口连接断开
                    Log.e(DEBUG_TAG, "端口连接断开");
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    /**
     * 吐丝方法
     *
     * @param message 吐丝内容
     */
    public static void showToast(final String message) {
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
