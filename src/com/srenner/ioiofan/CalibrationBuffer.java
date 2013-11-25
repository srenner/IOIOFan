package com.srenner.ioiofan;


public class CalibrationBuffer {

	private int mSize;
	private double mTolerance;
	private int[] mData;
	private int mPosition = 0;
	private boolean mFull = false;
	
	public CalibrationBuffer(int size, double tolerance) {
		mSize = size;
		mTolerance = tolerance;
		mData = new int[mSize];
		mPosition = 0;
		mFull = false;
	}
	
	public void push(int item) {
		if(!mFull) {
			for(int i = 0; i < mSize; i++) {
				if(mData[i] == 0) {
					mData[i] = item;
					mPosition = i;
					return;
				}
			}
		}
		mFull = true;
		mData[mPosition] = item;
		if(mPosition >= 1) {
			mPosition = mPosition--;
		}
		else {
			mPosition = mSize - 1;
		}
	}
	
	private void analyze() {
		int lowest = 0;
		int highest = 0;
		if(mSize > 1) {
			lowest = mData[0];
			highest = mData[0];
		}
		for(int i = 1; i < mSize; i++) {
			int current = mData[i];
			if(current < lowest) {
				lowest = current;
			}
			else if(current > highest) {
				highest = current;
			}
		}
		
		
		
		
	}
	
}
