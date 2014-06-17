package com.df.serial.dafeng_serial_debug;

import java.io.File;

public class SerialParam {
	private static final String[] serialContain = { "serial", "ttySAC" };

	public static String[] Get_Serial_Names() {
		File file = new File("/dev/");
		String[] fileNames = file.list();
		for (int num = 0; num < serialContain.length; ++num) {
			int size = 0;
			for (int i = 0; i < fileNames.length; ++i) {
				if (fileNames[i].contains(serialContain[num])) {
					size++;
				}
			}
			if (size == 0) {
				continue;
			}
			String[] names = new String[size];
			size = 0;
			for (int i = 0; i < fileNames.length; ++i) {
				if (fileNames[i].contains(serialContain[num])) {
					names[size++] = fileNames[i].substring(fileNames[i]
							.indexOf(serialContain[num]));
				}
			}
			return names;
		}

		return null;
	}

	public static String[] Get_Serial_Values() {
		File file = new File("/dev/");
		String[] fileNames = file.list();

		for (int num = 0; num < serialContain.length; ++num) {
			int size = 0;
			for (int i = 0; i < fileNames.length; ++i) {
				if (fileNames[i].contains(serialContain[num])) {
					size++;
				}
			}
			if (size == 0) {
				continue;
			}
			String[] values = new String[size];
			size = 0;
			for (int i = 0; i < fileNames.length; ++i) {
				if (fileNames[i].contains(serialContain[num])) {
					values[size++] = "/dev/" + fileNames[i];
				}
			}
			return values;
		}

		return null;
	}

	public static final String Serial_BaudRate_Name[] = { "115200", "9600" };
	public static final String Serial_BaudRate_Value[] = { "115200", "9600" };

}