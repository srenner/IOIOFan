package com.srenner.ioiofan;

import com.srenner.ioiofan.FanService.IOIOBinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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
    			try {
    			mPWMValue = mService.getPWM();
    			mSeekPWM.setProgress(mPWMValue);
    			}
    			catch(Exception ex) {
    				String stop = "asdf";
    			}
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
		
		Context applicationContext = getActivity().getApplicationContext();
		Intent intent = new Intent(applicationContext, FanService.class);
		//PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		PendingIntent pi = PendingIntent.getService(applicationContext, 0, intent, 0);
		
		try {
			NotificationManager nm = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new NotificationCompat.Builder(applicationContext)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("IOIOFan")
			.setContentText("Tap to open")
			.setContentIntent(pi)
			.build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			//nm.notify(1, notification);
			
			//mService.startForeground(1, notification);
			
			applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			applicationContext.startService(intent);
			
		}
		catch(Exception ex) {
			String stop = "asdf";
		}
		
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
					mService.stopForeground(true);
					//mService.stopSelf();
				}
				getActivity().finish();
			}
		});
		
		return v;
	}
}