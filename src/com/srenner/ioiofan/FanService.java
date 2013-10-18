package com.srenner.ioiofan;

import ioio.lib.api.PulseInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalInput.Spec;
import ioio.lib.api.PulseInput.ClockRate;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class FanService extends IOIOService {

	private final IBinder mBinder = new IOIOBinder();
	private int mCurrentRPM = 0;
	private int mCurrentPWM = 0;
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private PwmOutput mPWM;
			private PulseInput mTachSignal;
	
			@Override
			protected void setup() throws ConnectionLostException, InterruptedException {
				mPWM = ioio_.openPwmOutput(1, 25000);
				mTachSignal = ioio_.openPulseInput(new Spec(2), ClockRate.RATE_62KHz, PulseMode.FREQ, true);
			}
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				mPWM.setPulseWidth(mCurrentPWM);
				mCurrentRPM = Math.round(mTachSignal.getFrequency() * 30);
				Thread.sleep(100);
			}
		};
	}

	public int getRPM() {
		return mCurrentRPM;
	}
	
	public void setPWM(int pwm) {
		mCurrentPWM = pwm;
	}
	
	public int getPWM() {
		return mCurrentPWM;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			stopSelf();
		} 
		else {
			// Service starting. Create a notification.
			Intent i = new Intent(this, MainActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
			Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("IOIOFan")
			.setContentText("Tap to open")
			//.setContentIntent(PendingIntent.getService(this, 0, new Intent(
			//		"stop", null, this, this.getClass()), 0))
			.setContentIntent(pi)
			.build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
		}
	}	

    public class IOIOBinder extends Binder {
        FanService getService() {
            return FanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
