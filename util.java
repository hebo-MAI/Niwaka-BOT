import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 汎用クラス
 * @author hebo-MAI
 * @since 2012/10/13
 * @version 1.0
 *
 */
public class util {
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	/**
	 * 呼び出したときの時間をエラー出力に表示する
	 */
	public static void print_time() {
		System.err.print(get_time());

	}

	public static String get_time() {
		Calendar c = Calendar.getInstance();

		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);

		return "[" + month + "/" + day + " " + hour + ":" + minute + ":" + second + "] ";
	}


	/** 全角数字を半角に変換します。
	 * http://www7a.biglobe.ne.jp/~java-master/samples/string/HankakuNumberToZenkakuNumber.html
	 * @param s 変換元文字列
	 * @return 変換後文字列
	 */
	public static String zenkakuNumToHankaku(String s) {
		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= '０' && c <= '９') {
				sb.setCharAt(i, (char)(c - '０' + '0'));
			}
		}
		return sb.toString();
	}

	/**
	 * 文字列が入っていない要素をリストから取り除く
	 * @param list 入力
	 * @return 出力
	 */
	public static ArrayList<String> removeBlankElement(ArrayList<String> list) {
		for(Iterator<String> i = list.iterator(); i.hasNext();) {
			if (i.next().length() < 2) i.remove();
		}
		return list;
	}

	/**
	 * ファイルの行数をカウントする
	 * @param responseFile : 行数をカウントするファイル
	 * @return ファイルの行数
	 */
	public static int count_line(File responseFile) {
		FileInputStream fis;
		BufferedReader br = null;
		String line = null;
		try {
			fis = new FileInputStream(responseFile);
			br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
		} catch (Exception e) {
			print_time();
			e.printStackTrace();
		}
		ArrayList<String> list = new ArrayList<String>();
		try {
			while((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -10;
		}
		return list.size();
	}

	/**
	 * ファイルを読み込み、改行ごとに区切ったものをリストに入れて返す
	 * @param filename : 読み込むファイル
	 * @return 改行ごとに区切られた文字列のリスト
	 */
	public static ArrayList<String> file_to_list(File filename) {
		FileInputStream fis;
		BufferedReader br = null;
		String line = null;
		try {
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
		} catch (Exception e) {
			print_time();
			e.printStackTrace();
		}
		ArrayList<String> list = new ArrayList<String>();
		try {
			while((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	public static ArrayList<String> file_to_list(String filename) {
		FileInputStream fis;
		BufferedReader br = null;
		String line = null;
		try {
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
		} catch (Exception e) {
			print_time();
			e.printStackTrace();
		}
		ArrayList<String> list = new ArrayList<String>();
		try {
			while((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	/**
	 * 入力のリストを、要素ごとに改行した文字列として出力する
	 * @param list 入力
	 * @return それぞれ改行された文字列
	 */
	public static String list_to_string(ArrayList<String> list) {
		String str = "";
		for(Iterator<String> i = list.iterator(); i.hasNext(); str += LINE_SEPARATOR) {
			str += i.next();
		}
		return str.substring(0, str.length()-LINE_SEPARATOR.length());
	}

	/**
	 *  入力文字列を改行文字("\n")に従って分割する
	 * @param str : 入力する文字列
	 * @return 文字列のリスト
	 */
	public static ArrayList<String> separate_string(String str){
		ArrayList<String> list = new ArrayList<String>();
		String[] strAry = str.split("\\n");
		int k = strAry.length;
		for(int i=0 ; i<k ; i++){
			list.add(strAry[i]);
		}
		return list;
	}

	/**
	 * 改行を表す"\n"を含む文字列の改行を、実際の改行文字と置換する。
	 * @param str : "\n"を含む入力文字列
	 * @return "\n"を改行に置換した文字列
	 */
	public static String replace_new_line(String str) {
		Pattern p = Pattern.compile("\\\\n");
		Matcher m = p.matcher(str);
		if(m.find()) str = m.replaceAll(LINE_SEPARATOR);
		return str;
	}
}
