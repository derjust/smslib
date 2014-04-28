package org.smslib.gateway.proxy.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class SendTestMessageResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public SendTestMessageResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveSendTestMessageResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveSendTestMessageResult(resultCode, resultData);
        }
    }

}
