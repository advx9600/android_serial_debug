package com.df.serial.dafeng_serial_debug;

public class SerialController {

	static {
		System.loadLibrary("dafeng_serial_assistance");
	}
	private volatile int mFdRead = -1;
	private volatile int mFdWrite = -1;
	private String mDev;
	private int mBaudRate;

	/*
	 * @param dev as /dev/s3c_serial1
	 * 
	 * @param baudRate 9600,115200
	 */
	public int open(String dev, int baudRate) {
		mDev = dev;
		mBaudRate = baudRate;
		mFdRead = openSerial(dev, baudRate,10,30);
		mFdWrite = openSerial(dev, baudRate,10,30);
		return mFdRead;
	}

	public int reopen() {
		return reopen(mDev, mBaudRate);
	}

	public int reopen(String dev, int baudRate) {
		close();
		return open(dev, baudRate);
	}

	public void close() {
		closeSerial(mFdRead);
		closeSerial(mFdWrite);
		// mFd = -1;
	}

	public int write(byte[] bts, int leng) {
		return writeData(mFdWrite, bts, leng);
	}

	public int write(byte[] bts) {
		return write(bts, bts.length);
	}

	public int write(String str) {
		return write(str.getBytes(), str.getBytes().length);
	}

	// public int read(byte[] bts, int leng, int timeout) {
	// return readData(mFd, bts, leng, timeout);
	// }

	public int read(byte[] bts, int leng) {
		int len = readData(mFdRead, bts, leng, 1000);
		// the problem of linux serial 3, it's weird
		if (len == 0) {
			reopen();
		}
		return len;
	}

	/**
	 * return in one second
	 * 
	 * @param bts
	 * @return the data of number
	 */
	public int read(byte[] bts) {
		return read(bts, bts.length);
	}

	private native int openSerial(String dev, int baudRate, int vTime,
			int minCharater);

	private native void closeSerial(int fd);

	private native int writeData(int fd, byte[] bts, int leng);

	// this is block read,for not block read, the data may loss
	private native int readData(int fd, byte[] bts, int leng, int timeOut);
}
