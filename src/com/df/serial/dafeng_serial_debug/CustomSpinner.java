package com.df.serial.dafeng_serial_debug;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class CustomSpinner extends Spinner {
	private static final String TAG = "DF_CustomSpinner";
	private Context mContext;
	private List<String> listValues;
	private List<String> listNames;

	private static void loge(String msg) {
		Log.e(TAG, msg + "");
	}

	public CustomSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public void ConstructSpinner(List<String> displays, List<String> values) {
		listNames = displays;
		listValues = values;
		buildSpinner(android.R.layout.simple_spinner_item);
	}

	public void ConstructSpinner(String[] displays, String[] values) {
		listNames = new ArrayList<String>();
		listValues = new ArrayList<String>();
		for (int i = 0; i < displays.length; ++i) {
			listNames.add(displays[i]);
		}
		for (int i = 0; i < values.length; ++i) {
			listValues.add(values[i]);
		}
		buildSpinner(android.R.layout.simple_spinner_item);
	}

	public String getSelectedValue() {
		String value = "";
		value = listValues.get(getSelectedItemPosition());
		return value;
	}

	public void setValue(String value) {
		for (int i = 0; i < listValues.size(); i++) {
			if (value.equals(listValues.get(i))) {
				this.setSelection(i);
				break;
			}
		}
	}

	private void buildSpinner(int textStyle) {
		if (listNames.size() != listValues.size()) {
			loge("listName size != listValues size " + listNames.size()
					+ " != " + listValues.size());
			return;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				textStyle, listNames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setAdapter(adapter);
	}
}