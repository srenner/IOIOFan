package com.srenner.ioiofan;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

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
		startService(new Intent(this, FanService.class));
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
	


}
