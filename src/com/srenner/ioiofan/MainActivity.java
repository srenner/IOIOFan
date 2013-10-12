package com.srenner.ioiofan;


import com.srenner.ioiofan.FanService.IOIOBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

    FanService mService;
    boolean mBound = false;


	
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
		mPWMValue = -1;
		mSeekPWM.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				mTvPWM.setText(String.valueOf(mSeekPWM.getProgress()) + "%");
				mPWMValue = mSeekPWM.getProgress();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});
		
		Button btnCalibrate = (Button)findViewById(R.id.button1);
		btnCalibrate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				int rpm = mService.getRPM();
				
			}});
		
		
        Intent intent = new Intent(this, FanService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		
	}
	
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            IOIOBinder binder = (IOIOBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


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
	


}
