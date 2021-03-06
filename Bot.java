import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;
import java.util.Random;


public class Bot {

	static TwitterAction ta = new TwitterAction();
	static TwitterResponse tr = new TwitterResponse();

	// バックグラウンド実行用
	public void bot(){
		try {
			//多重起動をチェックする
			FileChannel fc = new FileOutputStream(new File("LockFile")).getChannel();
			FileLock lock = fc.tryLock();
			if (lock == null) {
				//既に起動している場合は終了する
				System.out.println("Error : already running.");
				return;
			}
		} catch (Exception e) {
		}
		//多重起動でなければログシステムにログを追加
		Log.info("Starting BOT");


		// インスタンスを呼び出し、自動で非同期処理を行う
		@SuppressWarnings("unused")
		UserTimelineStream uts = new UserTimelineStream();

		Random rnd = new Random();
		Calendar cal;
		int hour,minute;
		int random_time;
		while (true){
			random_time = 15 + util.dice(10, 3);
			for(int i=0;i<random_time;i++){
				try {
					Thread.sleep(60000+rnd.nextInt(5000));
				} catch(Exception e) {
				}
				tr.makeReply();
			}
			cal = Calendar.getInstance();
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
			if (hour>=19 || hour<=2 || (hour==12 && minute<=30)) {
				tr.tw();
				try {
					Thread.sleep(100000+rnd.nextInt(10000));
				} catch(Exception e){
				}
			}
			//autoRefollow();
		}
	}

}
