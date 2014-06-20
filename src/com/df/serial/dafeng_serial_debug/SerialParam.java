package com.df.serial.dafeng_serial_debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

public class SerialParam {
	private static final String[] serialContain = { "serial", "ttySAC","ttyMAX" };
	
	public static final String SAVE_FILE_NAME= Environment.getExternalStorageDirectory()+"/dafeng_serila_recv.txt";

	public static String[] Get_Serial_Names() {
		File file = new File("/dev/");
		String[] fileNames = file.list();
		List<Integer> listNum = new ArrayList<Integer>();
		for (int i=0;i<fileNames.length;i++){
			for (int j=0;j<serialContain.length;j++){
				if (fileNames[i].contains(serialContain[j])){					
					listNum.add(i);
					break;
				}
			}
		}
		
		if (listNum.size() <1)
			return null;
		
		String[] names = new String[listNum.size()];
		for (int i=0;i<listNum.size();i++)
			names[i]=fileNames[listNum.get(i)];
		
		return names;
	}

	public static String[] Get_Serial_Values() {
		String[] values =Get_Serial_Names();
		
		if (values ==null) return null;
		
		for (int i=0;i<values.length;i++){
			values[i]="/dev/"+values[i];
		}
		return values;
	}

	public static final String Serial_BaudRate_Name[] = { "115200", "9600" };
	public static final String Serial_BaudRate_Value[] = { "115200", "9600" };

}