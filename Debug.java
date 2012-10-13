import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * デバッグ用
 * @author hebo-MAI
 *
 */
public class Debug {
	/**
	 * デバッグ用
	 */
	public static void debug() {
		ArrayList<String> list = util.file_to_list(new File("test.txt"));
		list = util.removeBlankElement(list);
		System.out.println("***DEBUG TEST***");
		String str = util.list_to_string(list);
		System.out.print(str);
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("test2.txt")));
			pw.print(str);
			pw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
