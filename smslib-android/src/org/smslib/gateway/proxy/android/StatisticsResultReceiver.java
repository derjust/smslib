package org.smslib.gateway.proxy.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class StatisticsResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public StatisticsResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onStatisticsResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onStatisticsResult(resultCode, resultData);
        }
    }

}
