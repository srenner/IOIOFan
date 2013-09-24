package com.srenner.ioiofan;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalInput.Spec;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.ClockRate;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends IOIOActivity {

	protected SeekBar mSeekPWM;
	protected TextView mTvPWM;
	protected TextView mTvRPM;
	protected TextView mTvMessages;
	protected int mPWMValue;
	
	protected static int PIN_PWM = 1;
	protected static int PIN_RPM = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSeekPWM = (SeekBar)findViewById(R.id.seekPWM);
		mTvPWM = (TextView)findViewById(R.id.tvPWM);
		mTvPWM.setText("0%");
		
		mTvRPM = (TextView)findViewById(R.id.tvRPM);
		
		mTvMessages = (TextView)findViewById(R.id.tvMessages);
		mPWMValue = -1;
		mSeekPWM.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				mTvPWM.setText(String.valueOf(mSeekPWM.getProgress()) + "%");
				mPWMValue = mSeekPWM.getProgress();
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void setTextViewText(int textViewResourceID, String text) {
		TextView tv = (TextView)findViewById(textViewResourceID);
		tv.setText(text);
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput mLED;
		private PwmOutput mPWM;
		private PulseInput mTachSignal;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			mPWM = ioio_.openPwmOutput(PIN_PWM, 25000);
			
			
			
			//mTachSignal = ioio_.openPulseInput(PIN_RPM, PulseMode.FREQ);
			Spec s = new Spec(PIN_RPM);
			mTachSignal = ioio_.openPulseInput(s, ClockRate.RATE_16MHz, PulseMode.FREQ, true);
			
			//mTachSignal.waitPulseGetDuration()
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop()  {
			try {
				int val = mSeekPWM.getProgress();
				mPWM.setPulseWidth(val);
				
				
				float myBoat = mTachSignal.getFrequency();
				
				mTvRPM.setText(String.valueOf(myBoat));
				
				
				Thread.sleep(100);
			} catch (InterruptedException e) {
			} catch (ConnectionLostException e) {
			} catch (Exception e) {
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

}
