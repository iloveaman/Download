package utils.download;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SimpleTask implements Runnable {
    private static final int STATE_NEW = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_FINISH = 4;
    private static final int STATE_CANCELLED = 8;
    private static final int MSG_TASK_DONE = 1;
    private static SimpleTask.InternalHandler sHandler = null;
    private Thread mCurrentThread;
    private AtomicInteger mState = new AtomicInteger(1);

    public SimpleTask() {
    }

    public abstract void doInBackground();

    public abstract void onFinish(boolean var1);

    protected void onCancel() {
    }

    public void restart() {
        this.mState.set(1);
    }

    public void run() {
        if(this.mState.compareAndSet(1, 2)) {
            this.mCurrentThread = Thread.currentThread();
            this.doInBackground();
            sHandler.obtainMessage(1, this).sendToTarget();
        }
    }

    public boolean isCancelled() {
        return this.mState.get() == 8;
    }

    public boolean isDone() {
        return this.mState.get() == 4;
    }

    public void cancel() {
        if(this.mState.get() < 4) {
            if(this.mState.get() == 2 && null != this.mCurrentThread) {
                try {
                    this.mCurrentThread.interrupt();
                } catch (Exception var2) {
                    var2.printStackTrace();
                }
            }

            this.mState.set(8);
            this.onCancel();
        }
    }

    public static void post(Runnable r) {
        sHandler.post(r);
    }

    public static void postDelay(Runnable r, long delayMillis) {
        sHandler.postDelayed(r, delayMillis);
    }

    static {
        sHandler = new SimpleTask.InternalHandler(Looper.getMainLooper());
    }

    private static class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            SimpleTask work = (SimpleTask)msg.obj;
            switch(msg.what) {
            case 1:
                boolean isCanceled = work.isCancelled();
                work.mState.set(4);
                work.onFinish(isCanceled);
            default:
            }
        }
    }
}
