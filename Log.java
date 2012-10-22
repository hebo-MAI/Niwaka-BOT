import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 行動の記録を残す
 * @author hebo-MAI
 * @version 0.01
 *
 */
/* TODO : ログを残す箇所の調整。現状ではほとんどログが残らない。
 * 		: ツイートやリプライの生成、リツイートやお気に入り登録等にログを残したい。
 */
public class Log {
	static final File LOG_FILE = new File("log.txt");
	static final String LINE_SEPARATOR = util.LINE_SEPARATOR;
	static final boolean generate_log = false;

	/**
	 * ログファイルの末尾に入力文字列を追加した後、改行する。
	 * @param str : 入力文字列
	 */
	public static void addLog(String str) {
		BufferedWriter bw = null;
		if(!generate_log)	return;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOG_FILE,true),"UTF-8"));
			bw.write(util.get_time());
			bw.write(str + LINE_SEPARATOR);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
