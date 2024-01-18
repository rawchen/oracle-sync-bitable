package com.lundong.sync.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * @author RawChen
 * @date 2021-12-17 9:34
 */
public class FileUtil {
	/**
	 * 读取文件的内容第一行
	 *
	 * @param file 想要读取的文件对象
	 * @return 返回文件内容
	 */
	public static String textFileToString(File file) {
		String result = "";
		try {
			//构造一个BufferedReader类来读取文件
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			//使用readLine方法，一次读一行
			if ((s = br.readLine()) != null) {
				result = s;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String textFileToString() {
		return textFileToString(new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "TABLE_ID.data"));
	}

	public static int stringToTextFile(String tableId) {
		return stringToTextFile(tableId, new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "TABLE_ID.data"));
	}

	public static String textFileToStringOrder() {
		return textFileToString(new File(System.getProperty("user.dir") + File.separator + "TABLE_ID_ORDER.data"));
	}

	public static int stringToTextFileOrder(String tableId) {
		return stringToTextFile(tableId, new File(System.getProperty("user.dir") + File.separator + "TABLE_ID_ORDER.data"));
	}

	public static String textFileToStringGoods() {
		return textFileToString(new File(System.getProperty("user.dir") + File.separator + "TABLE_ID_GOODS.data"));
	}

	public static int stringToTextFileGoods(String tableId) {
		return stringToTextFile(tableId, new File(System.getProperty("user.dir") + File.separator + "TABLE_ID_GOODS.data"));
	}

	/**
	 * 写入字符串到指定存在的文本文件
	 *
	 * @param str
	 * @param file
	 * @return
	 */
	public static int stringToTextFile(String str, File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.write(str);
			fw.close();
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		return 1;
	}
}
