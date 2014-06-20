package com.df.serial.dafeng_serial_debug;

import java.util.ArrayList;
import java.util.List;
import com.example.dafeng_serial_debug.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SerialConfig {
	private SerialDebug mSerialDebug;
	private SerialController mSerialController;
	private RecvThread mRecvThread;
	private SendLoopThread mSendLoopThread;
	private boolean mIsDestroyed = false;

	private String serialVlaue;
	private String serialBaudRate;
	private boolean serialSwitch = false;
	private boolean receiveNewLine;
	private boolean receiveHexDisplay;
	private boolean sendHexFormat;
	private boolean sendLoop;
	private int sendLoopTime = 1000; // default value
	private String sendMsg;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == RecvThread.MSG_RECV_TAG) {
				byte[] data = msg.getData().getByteArray(
						RecvThread.TAG_DATA_FLAG);
				processRecvData(data);
				msg.getData().remove(RecvThread.TAG_DATA_FLAG);
				msg.getData().clear();
			}
		}
	};

	class RecvThread extends Thread {
		public static final String TAG_DATA_FLAG = "data";
		public static final int MSG_RECV_TAG = 1;

		@Override
		public void run() {
			while (!mIsDestroyed) {
				if (isSerialSwitch()) { // switcher on
					byte[] bts = new byte[1024];
					int len = mSerialController.read(bts);
					if (len > 0) {
						// Log.i("RecvThread", "data len:" + len);
						Message msg = new Message();
						msg.what = MSG_RECV_TAG;
						Bundle data = new Bundle();
						byte[] recvByte = new byte[len];
						System.arraycopy(bts, 0, recvByte, 0, len);
						data.putByteArray(TAG_DATA_FLAG, recvByte);
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
				}
			}
		}
	}

	class SendLoopThread extends Thread {
		public boolean isPause = true;

		public byte[] getHexBytes(String data) {
			// TODO Auto-generated method stub
			data = data.trim();
			String[] bts = data.split(" ");
			List<Byte> listBts = new ArrayList<Byte>();
			for (int i = 0; i < bts.length; ++i) {
				bts[i] = bts[i].trim();
				bts[i] = bts[i].replace("0x", "");
				bts[i] = bts[i].replace("0X", "");
				if (bts[i].length() < 1) {
					continue;
				}
				if (bts[i].length() > 2) {
					return null;
				}
				int[] hex = { 0, 0 };
				for (int j = 0; j < bts[i].length(); ++j) {
					char c = bts[i].charAt(j);
					if (c >= '0' && c <= '9') {
						c = (char) (c - '0');
					} else if (c >= 'a' && c <= 'f') {
						c = (char) (c - 'a' + 10);
					} else if (c >= 'A' && c <= 'F') {
						c = (char) (c - 'A' + 10);
					} else {
						return null;
					}
					if (bts[i].length() == 1) {
						hex[1] = (c & 0xf);
					} else {
						hex[j] = (c & 0xf);
					}
				}
				listBts.add((byte) (hex[0] * 16 + hex[1]));
			}
			byte[] retBytes = new byte[listBts.size()];
			for (int i = 0; i < retBytes.length; ++i) {
				retBytes[i] = listBts.get(i);
			}
			return retBytes;
		}

		@Override
		public void run() {
			while (!mIsDestroyed) {
				if (isPause) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					String data = getSendMsg();
					int time = getSendLoopTime();
					// System.out.print("sendTime:" + time);
					if (isSendHexFormat()) {
						byte[] bts = getHexBytes(data);
						if (bts != null) {
							while (!isPause) {
								try {
									if (mIsDestroyed) {
										break;
									}
									Thread.sleep(time);
									sendData(bts);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} else {
							isPause = true;
						}
					} else {
						while (!isPause) {
							try {
								if (mIsDestroyed) {
									break;
								}
								Thread.sleep(time);
								sendData(data);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public SerialConfig(SerialDebug serialDebug) {
		mSerialDebug = serialDebug;
		mSerialController = new SerialController();
		mRecvThread = new RecvThread();
		mRecvThread.start();
		mSendLoopThread = new SendLoopThread();
		mSendLoopThread.start();
	}

	public void sendData(byte[] bts) {
		// TODO Auto-generated method stub
		mSerialController.write(bts);
	}

	public void onDestroy() {
		mIsDestroyed = true;
		mSerialController.close();
	}

	public String getSerialVlaue() {
		return serialVlaue;
	}

	public void setSerialVlaue(String serialVlaue) {
		this.serialVlaue = serialVlaue;
		if (serialSwitch) {
			mSerialController.reopen(/* /dev/serial1 */this.getSerialVlaue(),
					Integer.parseInt(this.getSerialBaudRate()));
		}
	}

	public String getSerialBaudRate() {
		return serialBaudRate;
	}

	public void setSerialBaudRate(String serialBaudRate) {
		this.serialBaudRate = serialBaudRate;
		if (serialSwitch) {
			mSerialController.reopen(this.getSerialVlaue(),
					Integer.parseInt(this.getSerialBaudRate()));
		}
	}

	public boolean isReceiveNewLine() {
		return receiveNewLine;
	}

	public void setReceiveNewLine(boolean receiveNewLine) {
		this.receiveNewLine = receiveNewLine;
	}

	public boolean isReceiveHexDisplay() {
		return receiveHexDisplay;
	}

	public void setReceiveHexDisplay(boolean receiveHexDisplay) {
		this.receiveHexDisplay = receiveHexDisplay;
	}

	public boolean isSendHexFormat() {
		return sendHexFormat;
	}

	public void setSendHexFormat(boolean sendHexFormat) {
		this.sendHexFormat = sendHexFormat;
	}

	public boolean isSendLoop() {
		return sendLoop;
	}

	public void setSendLoop(boolean sendLoop) {
		this.sendLoop = sendLoop;
	}

	public int getSendLoopTime() {
		return sendLoopTime;
	}

	public void setSendLoopTime(int sendLoopTime) {
		this.sendLoopTime = sendLoopTime;
	}

	public boolean isSerialSwitch() {
		return serialSwitch;
	}

	public void setSerialSwitch(boolean serialSwitch) {
		this.serialSwitch = serialSwitch;
		if (this.serialSwitch) {
			mSerialController.open(this.getSerialVlaue(),
					Integer.parseInt(this.getSerialBaudRate()));
		} else {
			mSerialController.close();
		}
	}

	public String getSendMsg() {
		return sendMsg;
	}

	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
		if (this.isSerialSwitch()) {
			if (this.sendMsg != null && this.sendMsg.length() > 0) {
				if (mSerialDebug.isSendAreadConfigEnable()) {
					if (isSendHexFormat()) {
						if (mSendLoopThread.getHexBytes(this.getSendMsg()) == null) {
							alert(getString(R.string.send_msg_is_not_hex_format_data));
							return;
						}
					}
					/** if send loop **/
					if (this.isSendLoop()) {
						/** send character is valid **/
						mSendLoopThread.isPause = false;
						mSerialDebug.setSendAreadConfigEnable(false);
					} else {
						if (isSendHexFormat()) {
							sendData(mSendLoopThread.getHexBytes(this
									.getSendMsg()));
						} else {
							sendData(this.getSendMsg());
						}
					}

				} else {
					mSendLoopThread.isPause = true;
					mSerialDebug.setSendAreadConfigEnable(true);
				}
			} else {
				alert(getString(R.string.send_not_empty));
			}
		} else {
			alert(getString(R.string.serial_not_opened));
		}
	}

	public void setSendParam(String msg, boolean isHexDis, boolean isSendLoop,
			int sendTimeDuration) {

	}

	public String getString(int resId) {
		return mSerialDebug.getString(resId);
	}

	public void alert(String msg) {
		Dialog alertDialog = new AlertDialog.Builder(mSerialDebug)
				.setMessage(msg)
				.setPositiveButton(getString(R.string.confirm), null).create();
		alertDialog.show();
	}

	public void config() {

	}

	public void sendData(String msg) {
		mSerialController.write(msg);
	}

	private static String toHex(byte data) {
		String format[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		return format[(data & 0xff) / 16] + format[(data & 0xff) % 16];
	}

	private void processRecvData(byte[] data) {
		String disStr = null;
		if (this.isReceiveHexDisplay()) {
			disStr = "";
			for (int i = 0; i < data.length; ++i) {
				if (this.isReceiveNewLine()) {
					if (i > 0) {
						disStr += " ";
					}
				} else {
					disStr += " ";
				}
				disStr += toHex(data[i]);
			}
		} else {
			disStr = new String(data);
		}
		if (this.isReceiveNewLine()) {
			disStr += "\n";
		}
		mSerialDebug.appendRecvMsgDisplay(disStr);
	}
}
