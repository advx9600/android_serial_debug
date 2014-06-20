package com.df.serial.dafeng_serial_debug;

import com.example.dafeng_serial_debug.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SerialDebug extends Activity implements OnCheckedChangeListener,
		OnClickListener, OnItemSelectedListener {
	private CustomSpinner mSpinnerSerialNum;
	private CustomSpinner mSpinnerSerialBaudRate;
	private CheckBox mCheckSwitcher;
	private CheckBox mCheckRecvNewLine;
	private CheckBox mCheckRecvHexDis;
	private CheckBox mCheckSendHexFormat;
	private CheckBox mCheckSendLoop;
	private CheckBox mCheckSaveFile;
	private EditText mTextSendTime;
	private ScrollView mScrollRecvTextDis;
	private TextView mTextRecvMsgDis;
	private EditText mTextSendMsg;
	private Button mBtnClearDis;
	private Button mBtnSend;

	private SerialConfig mSerialConfig;

	private SerialVal mSerialVal;

	class SerialVal {
		private static final String PREFS_NAME = "serial_values";

		private static final String KEY_SERIAL_PORT = "serial_port";
		private static final String KEY_SERIAL_BAUD = "serial_baud_rate";
		private static final String KEY_SERIAL_SWITCHER = "serial_switcher";

		private static final String KEY_RECV_NEXTLINE = "recv_next_line";
		private static final String KEY_RECV_HEX_DIS = "recv_hex_display";

		private static final String KEY_SEND_HEX = "send_hex";
		private static final String KEY_SEND_LOOP = "send_loop";
		private static final String KEY_SEND_LOOP_TIME = "send_loop_time";
		private static final String KEY_SND_MSG = "send_msg";

		public static final int MODE = Activity.MODE_PRIVATE;
		private SharedPreferences mSettings;
		private SharedPreferences.Editor mEditor;

		private String serialPort;
		private String serialBaudRate;
		private boolean serialSwitcher;
		private boolean recvNextLine;
		private boolean recvHexDis;
		private boolean sendHex;
		private boolean sendLoop;
		private int sendLoopTime;
		private String sendMsg;

		public String getSerialPort() {
			serialPort = mSettings.getString(KEY_SERIAL_PORT, "");
			return serialPort;
		}

		public void setSerialPort(String serialPort) {
			this.serialPort = serialPort;
			mEditor.putString(KEY_SERIAL_PORT, this.serialPort);
			mEditor.commit();
		}

		public String getSerialBaudRate() {
			serialBaudRate = mSettings.getString(KEY_SERIAL_BAUD, "");
			return serialBaudRate;
		}

		public void setSerialBaudRate(String serialBaudRate) {
			this.serialBaudRate = serialBaudRate;
			mEditor.putString(KEY_SERIAL_BAUD, this.serialBaudRate);
			mEditor.commit();
		}

		public boolean isSerialSwitcher() {
			serialSwitcher = mSettings.getBoolean(KEY_SERIAL_SWITCHER, false);
			return serialSwitcher;
		}

		public void setSerialSwitcher(boolean serialSwitcher) {
			this.serialSwitcher = serialSwitcher;
			mEditor.putBoolean(KEY_SERIAL_SWITCHER, this.serialSwitcher);
			mEditor.commit();
		}

		public boolean isRecvNextLine() {
			recvNextLine = mSettings.getBoolean(KEY_RECV_NEXTLINE, false);
			return recvNextLine;
		}

		public void setRecvNextLine(boolean recvNextLine) {
			this.recvNextLine = recvNextLine;
			mEditor.putBoolean(KEY_RECV_NEXTLINE, this.recvNextLine);
			mEditor.commit();
		}

		public boolean isRecvHexDis() {
			recvHexDis = mSettings.getBoolean(KEY_RECV_HEX_DIS, false);
			return recvHexDis;
		}

		public void setRecvHexDis(boolean recvHexDis) {
			this.recvHexDis = recvHexDis;
			mEditor.putBoolean(KEY_RECV_HEX_DIS, this.recvHexDis);
			mEditor.commit();
		}

		public boolean isSendHex() {
			sendHex = mSettings.getBoolean(KEY_SEND_HEX, false);
			return sendHex;
		}

		public void setSendHex(boolean sendHex) {
			this.sendHex = sendHex;
			mEditor.putBoolean(KEY_SEND_HEX, this.sendHex);
			mEditor.commit();
		}

		public boolean isSendLoop() {
			sendLoop = mSettings.getBoolean(KEY_SEND_LOOP, false);
			return sendLoop;
		}

		public void setSendLoop(boolean loopSend) {
			this.sendLoop = loopSend;
			mEditor.putBoolean(KEY_SEND_LOOP, this.sendLoop);
			mEditor.commit();
		}

		public int getSendLoopTime() {
			sendLoopTime = mSettings.getInt(KEY_SEND_LOOP_TIME, 1000);
			return sendLoopTime;
		}

		public void setSendLoopTime(int sendLoopTime) {
			this.sendLoopTime = sendLoopTime;
			mEditor.putInt(KEY_SEND_LOOP_TIME, this.sendLoopTime);
			mEditor.commit();
		}

		public SerialVal() {
			mSettings = getSharedPreferences(PREFS_NAME, MODE);
			mEditor = mSettings.edit();
		}

		public String getSendMsg() {
			sendMsg = mSettings.getString(KEY_SND_MSG, "");
			return sendMsg;
		}

		public void setSendMsg(String sendMsg) {
			this.sendMsg = sendMsg;
			mEditor.putString(KEY_SND_MSG, this.sendMsg);
			mEditor.commit();
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_serial_debug);
		initCmp();
		startRun();
	}

	@Override
	public void onDestroy() {
		mSerialConfig.onDestroy();
		saveCurData();
		super.onDestroy();
	}

	private void initCmp() {
		/*** serial config ui ****/
		mSpinnerSerialNum = (CustomSpinner) findViewById(R.id.spinner_serial_num);
		mSpinnerSerialBaudRate = (CustomSpinner) findViewById(R.id.spinner_serial_baud_rate);
		mCheckSwitcher = (CheckBox) findViewById(R.id.check_switcher);
		/** serial recv config ui ***/
		mCheckRecvNewLine = (CheckBox) findViewById(R.id.check_recv_new_line);
		mCheckRecvHexDis = (CheckBox) findViewById(R.id.check_recv_hex_display);
		/** serial send config ui ***/
		mCheckSendHexFormat = (CheckBox) findViewById(R.id.check_send_hex_format);
		mCheckSendLoop = (CheckBox) findViewById(R.id.check_send_loop);
		mCheckSaveFile = (CheckBox) findViewById(R.id.check_save_file);
		mTextSendTime = (EditText) findViewById(R.id.text_send_loop_time);
		/** right layout ui ***/
		mScrollRecvTextDis = (ScrollView) findViewById(R.id.scroll_recv_text_dis);
		mTextRecvMsgDis = (TextView) findViewById(R.id.text_recv_msg_dis);
		mTextSendMsg = (EditText) findViewById(R.id.text_send_msg);
		mBtnClearDis = (Button) findViewById(R.id.btn_clear_dis);
		mBtnSend = (Button) findViewById(R.id.btn_send);

		mSerialConfig = new SerialConfig(this);

		mSerialVal = new SerialVal();

	}

	private void startRun() {
		mSpinnerSerialNum.ConstructSpinner(SerialParam.Get_Serial_Names(),
				SerialParam.Get_Serial_Values());
		mSpinnerSerialBaudRate.ConstructSpinner(
				SerialParam.Serial_BaudRate_Name,
				SerialParam.Serial_BaudRate_Value);
		mSpinnerSerialNum.setOnItemSelectedListener(this);
		mSpinnerSerialBaudRate.setOnItemSelectedListener(this);

		mCheckSwitcher.setOnCheckedChangeListener(this);
		mCheckRecvNewLine.setOnCheckedChangeListener(this);
		mCheckRecvHexDis.setOnCheckedChangeListener(this);
		/** send **/
		mCheckSendHexFormat.setOnCheckedChangeListener(this);
		mCheckSendLoop.setOnCheckedChangeListener(this);
		mCheckSaveFile.setOnCheckedChangeListener(this);

		mBtnClearDis.setOnClickListener(this);
		mBtnSend.setOnClickListener(this);

		setCurData();
	}

	private void saveCurData() {
		mSerialVal.setSerialPort(mSpinnerSerialNum.getSelectedValue());
		mSerialVal.setSerialBaudRate(mSpinnerSerialBaudRate.getSelectedValue());
		mSerialVal.setSerialSwitcher(mCheckSwitcher.isChecked());
		mSerialVal.setRecvNextLine(mCheckRecvNewLine.isChecked());
		mSerialVal.setRecvHexDis(mCheckRecvHexDis.isChecked());
		mSerialVal.setSendHex(mCheckSendHexFormat.isChecked());
		mSerialVal.setSendLoop(mCheckSendLoop.isChecked());
		mSerialVal.setSendLoopTime(Integer.parseInt(mTextSendTime.getText()
				.toString()));
		mSerialVal.setSendMsg(mTextSendMsg.getText().toString());
	}

	private void setCurData() {
		mSpinnerSerialNum.setValue(mSerialVal.getSerialPort());
		mSpinnerSerialBaudRate.setValue(mSerialVal.getSerialBaudRate());
		// mCheckSwitcher.setChecked(mSerialVal.isSerialSwitcher());
		mCheckRecvNewLine.setChecked(mSerialVal.isRecvNextLine());
		mCheckRecvHexDis.setChecked(mSerialVal.isRecvHexDis());
		mCheckSendHexFormat.setChecked(mSerialVal.isSendHex());
		mCheckSendLoop.setChecked(mSerialVal.isSendLoop());
		mTextSendTime.setText(mSerialVal.getSendLoopTime() + "");
		mTextSendMsg.setText(mSerialVal.getSendMsg());
		/** this is necessary **/
		// mTextSendTime.requestFocus();
		// mTextSendMsg.requestFocus();
	}

	public void toast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	public void appendRecvMsgDisplay(String msg) {
		if (mTextRecvMsgDis.getHeight() > mScrollRecvTextDis.getHeight()) {
			mTextRecvMsgDis.setText("");
		}
		mTextRecvMsgDis.append(msg);
	}

	public void setSendAreadConfigEnable(boolean enabled) {
		mSpinnerSerialNum.setEnabled(enabled);
		mSpinnerSerialBaudRate.setEnabled(enabled);
		mCheckSwitcher.setEnabled(enabled);
		mCheckSendHexFormat.setEnabled(enabled);
		mCheckSendLoop.setEnabled(enabled);
		mTextSendTime.setEnabled(enabled);
		mTextSendMsg.setEnabled(enabled);
		if (enabled) {
			mBtnSend.setText(R.string.send);
		} else {
			mBtnSend.setText(R.string.send_stop);
		}
	}

	public boolean isSendAreadConfigEnable() {
		return mCheckSendHexFormat.isEnabled() || mCheckSendLoop.isEnabled()
				|| mTextSendTime.isEnabled() || mTextSendMsg.isEnabled();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (parent == mSpinnerSerialNum) {
			mSerialConfig.setSerialVlaue(mSpinnerSerialNum.getSelectedValue());
		} else if (parent == mSpinnerSerialBaudRate) {
			mSerialConfig.setSerialBaudRate(mSpinnerSerialBaudRate
					.getSelectedValue());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView == mCheckSwitcher) {
			mSerialConfig.setSerialSwitch(isChecked);
		} else if (buttonView == mCheckRecvNewLine) {
			mSerialConfig.setReceiveNewLine(isChecked);
		} else if (buttonView == mCheckRecvHexDis) {
			mSerialConfig.setReceiveHexDisplay(isChecked);
		} else if (buttonView == mCheckSendHexFormat) {
			mSerialConfig.setSendHexFormat(isChecked);
		} else if (buttonView == mCheckSendLoop) {
			mSerialConfig.setSendLoop(isChecked);
		} else if (buttonView == mCheckSaveFile) {
			mSerialConfig.setSaveFile(isChecked);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mBtnClearDis) {
			mTextRecvMsgDis.setText("");
		} else if (v == mBtnSend) {
			mSerialConfig.setSendLoopTime(Integer.parseInt(mTextSendTime
					.getText().toString()));
			mSerialConfig.setSendMsg(mTextSendMsg.getText().toString());
		}
	}

}
