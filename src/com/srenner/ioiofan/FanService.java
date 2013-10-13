package com.srenner.ioiofan;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
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
			private DigitalOutput mLED;
			private PwmOutput mPWM;
			private PulseInput mTachSignal;
	
			@Override
			protected void setup() throws ConnectionLostException, InterruptedException {
				mPWM = ioio_.openPwmOutput(1, 25000);
				Spec s = new Spec(2);
				mTachSignal = ioio_.openPulseInput(s, ClockRate.RATE_62KHz, PulseMode.FREQ, true);
				mLED = ioio_.openDigitalOutput(IOIO.LED_PIN);
			}
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				
				
				mPWM.setPulseWidth(mCurrentPWM);
				mCurrentRPM = Math.round(mTachSignal.getFrequency() * 30);
				
				//flash LED to test that service is running
				mLED.write(false);
				Thread.sleep(500);
				mLED.write(true);
				Thread.sleep(500);
			}
		};
	}

	public int getRPM() {
		return mCurrentRPM;
	}
	
	public void setPWM(int pwm) {
		mCurrentPWM = pwm;
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
			
			Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("IOIOFan Service")
			.setContentText("Click to stop")
			.setContentIntent(PendingIntent.getService(this, 0, new Intent(
					"stop", null, this, this.getClass()), 0))
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
