package printer.test.printerdome;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 线程工具类
 *
 */

public class ThreadUtils {

	private static Handler sHandler = new Handler(Looper.getMainLooper());

	private static Executor sExecutor = Executors.newSingleThreadExecutor();

	/**
	 * 在子线程执行
	 * 
	 * @param runnable
	 */
	public static void runOnSubThread(Runnable runnable) {
		sExecutor.execute(runnable);
	}

	/**
	 * 在主线程回调
	 * 
	 * @param runnable
	 */
	public static void runOnMainThread(Runnable runnable) {
		sHandler.post(runnable);
	}

}