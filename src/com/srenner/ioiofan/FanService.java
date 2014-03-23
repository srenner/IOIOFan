package com.srenner.ioiofan;

import ioio.lib.api.DigitalOutput;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class FanService extends IOIOService {

	private final IBinder mBinder = new IOIOBinder();
	private int mCurrentRPM = 0;
	private int mCurrentPWM = 0;
	private LoopMode mLoopMode;
	private Handler mHandler;
	private String mMessage = "";
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private PwmOutput mPWM;
			private PulseInput mTachSignal;
			private DigitalOutput mStatusLED;
			private boolean ledOn = false;
	
			@Override
			protected void setup() throws ConnectionLostException, InterruptedException {
				mPWM = ioio_.openPwmOutput(1, 25000);
				mTachSignal = ioio_.openPulseInput(new Spec(2), ClockRate.RATE_62KHz, PulseMode.FREQ, true);
				mMessage = "IOIO connection established";
				mStatusLED = ioio_.openDigitalOutput(0, true);
			}
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				ledOn = !ledOn;
				try {
					mStatusLED.write(ledOn);
					switch(mLoopMode) {
						case CALIBRATE: {
							calibrate();
							mLoopMode = LoopMode.NORMAL;
							break;
						}
						case STOP: {
							//ioio_.waitForDisconnect();
							ioio_.disconnect();
							stopForeground(true);
							return;
							
						}
						case NORMAL: {
							// fall through
						}
						default: {
							mPWM.setPulseWidth(mCurrentPWM);
							mCurrentRPM = Math.round(mTachSignal.getFrequency() * 30);
						}
					}
					Thread.sleep(100);
				}
				catch(Exception ex) {
					mMessage = ex.getMessage();
				}
			}
			
			private void calibrate() {
				int baselineSpeed = 0;
				try {
					mPWM.setPulseWidth(0);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mMessage = "Waiting for fan to settle";
						}
					});
					int previousRPM = Math.round(mTachSignal.getFrequency() * 30);
					mCurrentRPM = previousRPM;
					for(int i = 0; i < 10; i++) {
						
						Thread.sleep(1000);
						mCurrentRPM = Math.round(mTachSignal.getFrequency() * 30);
						if((mCurrentRPM * 1.02) >= previousRPM) {
							Thread.sleep(1000); // for extra settling beyond the % threshold
							baselineSpeed = Math.round(mTachSignal.getFrequency() * 30);
							mMessage = "Fan settled at " + String.valueOf(baselineSpeed);
							break;
						}
						else {
							previousRPM = mCurrentRPM;
						}
					}
					
					int maxRPM;
					int maxPWM;
					int[] speedMatrix = new int[101];
					double[] diffMatrix = new double[101];
					speedMatrix[0] = baselineSpeed;
					for(int i = 1; i <= 100; i++) {
						mPWM.setPulseWidth(i);
						Thread.sleep(300);
						speedMatrix[i] = Math.round(mTachSignal.getFrequency() * 30);
						diffMatrix[i] = (double)speedMatrix[i-1]/(double)speedMatrix[i];
						
					}
					@SuppressWarnings("unused")
					String stopHere = "breakpoint";
					
				} catch (ConnectionLostException e) {
					e.printStackTrace();
					mMessage = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
					mMessage = e.getMessage();
				}
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
	
	public LoopMode getLoopMode() {
		return mLoopMode;
	}

	public void setLoopMode(LoopMode loopMode) {
		mLoopMode = loopMode;
		//mMessage = loopMode.name();
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//call super.onStart because it starts the IOIOAndroidApplicationHelper 
		//and super.onStartCommand is not implemented
		super.onStart(intent, startId);
		handleStartup(intent);
		return START_STICKY;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		handleStartup(intent);
	}	

	private void handleStartup(Intent intent) {
		mHandler = new Handler();
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
				.setContentIntent(pi)
				.build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			startForeground(1, notification);
			setLoopMode(LoopMode.NORMAL);
		}		
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, R.string.service_stopped_text, Toast.LENGTH_SHORT).show();
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