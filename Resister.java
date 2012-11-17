import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字列をファイルに追加で保存するクラス
 *
 * @author hebo-MAI
 * @since 2012/10/13
 * @version 1.0
 *
 */
public class Resister {

	private static final File RESPONSE_FILE = new File("resister.txt");
	private static final File TWEET_FILE = TwitterAction.TWEET_FILE;
	private static final String LINE_SEPARATOR = TwitterAction.LINE_SEPARATOR;
	private static final String NG_Word = "pino|ぴの";

	/**
	 * ツイートの候補のリストに入力文字列を登録する
	 * @param str : 登録したい入力文字列
	 * @return : 登録成功時は正数
	 * @throws : ResisterException 登録失敗の例外
	 */
	public static int resister_tweet(String str) throws ResisterException {
		Pattern p;
		Matcher m;
		p = Pattern.compile(NG_Word, Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			Log.warn("Containing wrong word. Couldn't resister the message.");
			throw new ResisterException("Containing wrong word");
		}

		//改行文字をすべて"\n"に置き換え、意図しない改行を防ぐ
		p = Pattern.compile(LINE_SEPARATOR);
		m = p.matcher(str);
		if (m.find()) m.replaceAll("\n");

		//ツイートをリストに登録する
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TWEET_FILE,true),"UTF-8"));
			bw.write(LINE_SEPARATOR + str);
			bw.close();
		} catch (IOException e) {
			util.print_time();
			e.printStackTrace();
			Log.warn("Couldn't resister message because of IOException.");
			throw new ResisterException("IOException");
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				util.print_time();
				e.printStackTrace();
				Log.warn("Couldn't resister message because of IOException.");
				throw new ResisterException("IOException");
			}
		}
		ArrayList<String> list = util.file_to_list(TWEET_FILE);
		util.removeBlankElement(list);
		//ツイートファイルを、空行を取り除いたファイルに更新する
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(TWEET_FILE)));
			pw.print(util.list_to_string(list));
			pw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		Log.info("Succeeded to resister the message.");
		return util.count_line(TWEET_FILE);
	}



	/**
	 * 特定のキーワードに反応するためのワード等を登録する
	 * @param str1 : 反応するキーワード
	 * @param str2 : 反応した時につぶやく内容
	 * @return : 登録成功時は正数
	 * @throws ResisterException 登録失敗の例外
	 */
	public static int resister_response(String str1,String str2) throws ResisterException {
		Pattern p;
		Matcher m1,m2;
		p = Pattern.compile(NG_Word, Pattern.CASE_INSENSITIVE);
		m1 = p.matcher(str1);
		m2 = p.matcher(str2);
		if (m1.find() || m2.find())  {
			Log.warn("Containing wrong word. Couldn't resister the message.");
			throw new ResisterException("Containing wrong word");
		}

		//改行文字をすべて"\n"に置き換え、意図しない改行を防ぐ
		p = Pattern.compile(LINE_SEPARATOR);
		m1 = p.matcher(str1);
		if (m1.find()) m1.replaceAll("\n");
		m2 = p.matcher(str2);
		if (m2.find()) m2.replaceAll("\n");

		//Log.info("Resistering the response : " + str1);
		//Log.info("Resistering the message : " + str2);
		
		//反応をリストに登録する
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TWEET_FILE,true),"UTF-8"));
			bw.write(LINE_SEPARATOR + str1 + LINE_SEPARATOR + str2);
			bw.close();
		} catch (IOException e) {
			util.print_time();
			e.printStackTrace();
			Log.warn("Couldn't resister message because of IOException.");
			throw new ResisterException("IOException");
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				util.print_time();
				e.printStackTrace();
				Log.warn("Cannot resister message because of IOException.");
				throw new ResisterException("IOException");
			}
		}

		Log.info("Succeeded to resister the message.");
		return util.count_line(RESPONSE_FILE)/2;
	}

}
