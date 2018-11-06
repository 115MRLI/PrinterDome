package printer.test.printerdome;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements PrinterUtils.PrinterCallBack {
    private PrinterUtils printerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        printerUtils = new PrinterUtils(this);
        printerUtils.registerPrinterReceiver();
        printerUtils.onPrinterCallBack(this);
        printerUtils.onUSBprinterCallBack(new PrinterUtils.USBprinterCallBack() {
            @Override
            public void cannotFind() {
                Log.e("MainActivity", "找不到打印设备");
                printerUtils.showToast("找不到打印设备");
            }

            @Override
            public void beSucceed() {
                Log.e("MainActivity", "链接成功");
                printerUtils.showToast("链接成功");
            }

            @Override
            public void beDefeated(String details) {
                Log.e("MainActivity", "链接失败");
                printerUtils.showToast(details);
            }
        });
        findViewById(R.id.printer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    printerUtils.getPrinterStatus();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void beSucceed() {
        Log.e("MainActivity", "链接成功");
        printerUtils.showToast("链接成功");
        printerUtils.sendReceipt();
    }

    @Override
    public void beDefeated(String details) {
        Log.e("MainActivity", "链接失败");
        printerUtils.showToast(details);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerUtils.unPrinterService();
    }
}
