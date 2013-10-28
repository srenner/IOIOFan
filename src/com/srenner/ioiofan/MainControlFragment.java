package com.srenner.ioiofan;

import com.srenner.ioiofan.FanService.IOIOBinder;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainControlFragment extends Fragment {
    
	protected FanService mService;
    protected boolean mBound = false;
	
	protected SeekBar mSeekPWM;
	protected TextView mTvPWM;
	protected TextView mTvRPM;
	protected TextView mTvMessages;
	protected int mPWMValue;
	
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            IOIOBinder binder = (IOIOBinder) service;
            mService = binder.getService();
    		if(mService != null) {
    			mPWMValue = mService.getPWM();
    			mSeekPWM.setProgress(mPWMValue);
    		}
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_control, container, false);
		
		mSeekPWM = (SeekBar)v.findViewById(R.id.seekPWM);
		mTvPWM = (TextView)v.findViewById(R.id.tvPWM);
		mTvPWM.setText("0%");
		mTvRPM = (TextView)v.findViewById(R.id.tvRPM);
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
		Button btnCalibrate = (Button)v.findViewById(R.id.btnCalibrate);
		btnCalibrate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO call calibrate code from here
			}
		});
		
		Button btnExit = (Button)v.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService != null) {
					mService.stopSelf();
					NotificationManager nm = (NotificationManager)getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
					nm.cancel(0);
				}
				getActivity().finish();
			}
		});
		
		Context applicationContext = getActivity().getApplicationContext();
		Intent intent = new Intent(applicationContext, FanService.class);
		
		applicationContext.startService(intent);
		applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
		final Handler handler = new Handler();
		handler.post(new Runnable(){
		   @Override
		   public void run() {
			   if(mService != null) {
				   mTvRPM.setText(String.valueOf(mService.getRPM()));
				   mService.setPWM(mPWMValue);
			   }
		       handler.postDelayed(this, 100);
		   }
		});
		
		return v;
	}
}