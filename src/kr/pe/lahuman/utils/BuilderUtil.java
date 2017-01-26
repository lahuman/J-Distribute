package kr.pe.lahuman.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuilderUtil {
	private BuilderUtil(){};

	/**
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static boolean makeDirs(String filePath) throws IOException{
		File makeDir = new File(filePath.substring(0, filePath.lastIndexOf("/") ));
		return makeDir.mkdirs();
	}

	/**
	 * @return
	 */
	public static String getDateTime(){
		return format(new Date(), "yyyyMMddHHmmss");
	}

	/**
	 * - "yyyyMM";
	 * - "yyMMdd";
	 * - "yyyyMMdd";
	 * - "yyyyMMddHHmmss";
	 * - "yyyyMMddHHmmssSSS";
	 * - "HHmmss";
	 * - "EE";
	 * - "F";
	 * @param inDate
	 * @param format
	 * @return
	 */
	public static String format(Date inDate, String format) {
		if (inDate == null)
			return "";

		if (format == null || format.length() <= 0) {
			format = "yyyyMMddHHmmss";
		}

		SimpleDateFormat SDF = new SimpleDateFormat(format);

		return SDF.format(inDate);
	}
}
