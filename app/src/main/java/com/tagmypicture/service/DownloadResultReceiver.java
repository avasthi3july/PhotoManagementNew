package com.tagmypicture.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by kavasthi on 4/12/2017.
 */

public class DownloadResultReceiver extends ResultReceiver {
    private Receiver mResultReceiver;

    public void setmResultReceiver(Receiver mResultReceiver) {
        this.mResultReceiver = mResultReceiver;
    }

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */


    public DownloadResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mResultReceiver != null)
            mResultReceiver.onReceiveResult(resultCode, resultData);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

}
