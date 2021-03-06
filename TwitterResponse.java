import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


/**
 * ツイートに対する反応を生成し、ツイートを行うクラス
 *
 * @author hebo-MAI
 * @version 1.1
 *
 */
public class TwitterResponse extends TwitterAction {

	static final String CREATOR = "hebo_MAI";
	static final String BOT_NAME = "niwaka_bot";

	private final static String READLOG_FILE = "read_log.txt";
	private final static String REPLYLOG_FILE = "reply_log.txt";

	private final static String TARGET_NAME = "[(ぴの)|(pino)|(ｐｉｎｏ)|(ピノ)][(くん)君]";
	private final static String DECIMAL_NUMBER = "[0-9０１２３４５６７８９]+";

	private final static String NG_SOURCE = "twittbot\\.net";

	public long responseTimeline() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		FileReader in;
		long lastReadId = Long.MAX_VALUE;
		//*
		try {
			in = new FileReader(READLOG_FILE);
			BufferedReader br = new BufferedReader(in);
			try {
				lastReadId = Long.parseLong(br.readLine());
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
			} finally {
				in.close();
			}
		} catch (Exception e){
			util.print_time();
			e.printStackTrace();
		}
		//*/

		Paging paging = new Paging (lastReadId);

		ResponseList<Status> userTimeline = null;
		try {
			userTimeline = twitter.getHomeTimeline(paging);
		} catch (TwitterException e) {
			util.print_time();
			e.printStackTrace();
		}
		/*
		for(int i=0; i<userTimeline.size(); i++)
		System.out.println(userTimeline.get(i).getText());
		System.exit(500);
		//*/

		long id = 0;
		if (userTimeline != null) {
			for (Status utl : userTimeline) {
				if (id < utl.getId())	id = utl.getId();
				tl_reply(utl);
			}
			if (id > lastReadId) {
				File file = new File(READLOG_FILE);
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					pw.println(id);
					pw.close();
				} catch (Exception e) {
					util.print_time();
					e.printStackTrace();
				}
			}
		}
		return id;
	}

	public static int tl_reply(Status status) {
		FileReader in;
		long lastReadId = Long.MAX_VALUE;
		try {
			in = new FileReader(READLOG_FILE);
			BufferedReader br = new BufferedReader(in);
			try {
				lastReadId = Long.parseLong(br.readLine());
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
			} finally {
				in.close();
			}
		} catch (Exception e){
			util.print_time();
			e.printStackTrace();
		}
		if (status.getId()<=lastReadId) return -2;

		String str = status.getText();
		Pattern p;
		Matcher m;

		// 自身の名前を含むツイートを除外する
		p = Pattern.compile("@"+BOT_NAME, Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find())	return 1;

		//NG_SOURCEから投稿されたツイートに対しては反応しない
		p = Pattern.compile(NG_SOURCE, Pattern.CASE_INSENSITIVE);
		m = p.matcher(status.getSource());
		if (m.find())	return 2;

		p = Pattern.compile("[#|＃]ぴのくんはにわか",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 2;
			String reply = null;
			if (Math.random()<0.5)	reply = "@" + name + " おいそのハッシュタグ使うのやめろ";
			else					reply = "@" + name + " だからやめろ";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				tweet(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 12;
		}
		p = Pattern.compile("bot.*にわか|にわか.+bot",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 5;
			String reply = "@" + name + " すいません";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				tweet(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 15;
		}

		p = Pattern.compile(TARGET_NAME + ".*にわか" , Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 3;
			String reply = "@" + name + " 俺にわかじゃないよ";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				tweet(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 13;
		}
		p = Pattern.compile("にわか.*" + TARGET_NAME , Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 4;
			String reply = "@" + name + " にわかじゃないよ!";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				tweet(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 14;
		}
		return -1;
	}

	public void reply() {
		long lastPostId = Long.MAX_VALUE;
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		Paging paging = new Paging();

		FileReader in;
		try {
			in = new FileReader(REPLYLOG_FILE);
			BufferedReader br = new BufferedReader(in);
			try {
				lastPostId = Long.parseLong(br.readLine()) + 1;
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
			}
			br.close();
		} catch (Exception e){
			util.print_time();
			e.printStackTrace();
		}
		paging.setSinceId(lastPostId);

		ResponseList<Status> mentions = null;
		try {
			mentions = twitter.getMentionsTimeline();
		} catch (TwitterException e) {
			util.print_time();
			e.printStackTrace();
		}

		if (mentions != null) {
			long id = lastPostId;
			for (Status mention : mentions) {
				if (lastPostId < mention.getId()) {
					if (mention.getUser().getScreenName().equals(CREATOR)) {
						doReplyForCreator(mention);
					} else {
						doReply(mention);
					}
					if (id<mention.getId()) id = mention.getId();
				}
				if (id != lastPostId) {
					File file = new File(REPLYLOG_FILE);
					try {
						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
						pw.println(id);
						pw.close();
					} catch (Exception e) {
						util.print_time();
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * IDのツイートに対してリプライをする
	 * @param id リプライを送る先のツイートのID
	 */
	public void reply(long id) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		ResponseList<Status> mentions = null;

		try {
			mentions = twitter.getMentionsTimeline();
		} catch (TwitterException e) {
			util.print_time();
			e.printStackTrace();
		}

		if (mentions != null) {
			for (Status mention : mentions) {
				if (id == mention.getId() && mention.isRetweet() != true ) {
					if (mention.getUser().getScreenName().equals(CREATOR)) {
						doReplyForCreator(mention);
					} else {
						doReply(mention);
					}
				}
			}
		}
	}

	public static int doReply(Status mention) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		String str = mention.getText();
		long id = mention.getId();
		String name = "@" + mention.getUser().getScreenName();
		//自分自身にはリプライしない
		if (name.equals("@"+BOT_NAME)) return 100;
		String reply_str = null;
		Pattern p;
		Matcher m;

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?(「.+」$|登録(して)?([ 　]?「)?)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			p = Pattern.compile("@"+BOT_NAME+"[ 　]",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			int count = -1;
			try {
				count = Resister.resister_tweet(m.replaceAll(""));
				reply_str = name + " 登録しました。 (" + count + ")";
			} catch (ResisterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				reply_str = name + " 登録に失敗しました。(error 0)";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 100;
		}

		p = Pattern.compile("^@"+BOT_NAME+"[ 　]?(つぶや|ツイート)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			ta.tw();
			return 20;
		}

		p = Pattern.compile("^@"+BOT_NAME+"[ 　]?(うるさい|黙れ)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			try {
				delete(mention.getInReplyToStatusId());
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			return 200;
		}

		p = Pattern.compile("^@"+BOT_NAME+"[ 　]?呼んでない",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + " あ、そう。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 11;
		}
		p = Pattern.compile("^@"+BOT_NAME+"[ 　]?気のせい",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + "気のせいか・・・";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 12;
		}
		p = Pattern.compile("[#＃]ぴのくんはにわか",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + "おいやめろ";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 8;
		}

		p = Pattern.compile("にわか.+",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			if (Math.random()<0.5)	reply_str = name + " 俺にわかじゃないよ。";
			else					reply_str = name + " だからにわかじゃないって。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 1;
		}

		p = Pattern.compile("にわか$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + " おい";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 2;
		}


		p = Pattern.compile("@"+BOT_NAME+"[ 　]?(" + DECIMAL_NUMBER + ")番",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			String s = m.group(1);	//最初にマッチした正規表現を置き換える
			s = util.zenkakuNumToHankaku(s);
			int index = Integer.parseInt(s);
			tweet_call(index);
			return 3;
		}

		p = Pattern.compile("^(.*)?@"+BOT_NAME+".?$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			if (Math.random()<0.5)	reply_str = name + " 呼んだ？";
			else					reply_str = name + " ねえ、呼んだ？";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 10;
		}

		// 上記全ての条件に当てはまらなかった場合
		{
			reply_str = name + " ?__?";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e) {
				util.print_time();
				e.printStackTrace();
			}
		}


		return -1;
	}

	public static int doReplyForCreator(Status mention) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		String str = mention.getText();
		long id = mention.getId();
		String reply_str = null;
		Pattern p;
		Matcher m;

		p = Pattern.compile("("+DECIMAL_NUMBER+").?fav.rt",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			try {
				ta.fav(Long.parseLong(m.group(1)));
				ta.rt(Long.parseLong(m.group(1)));
			} catch (NumberFormatException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をfav&rtしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 1;
		}

		p = Pattern.compile("("+DECIMAL_NUMBER+").?rt.fav",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			try {
				ta.rt(Long.parseLong(m.group(1)));
				ta.fav(Long.parseLong(m.group(1)));
			} catch (NumberFormatException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をrt&favしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 2;
		}

		p = Pattern.compile("("+DECIMAL_NUMBER+").?(rt|リツイート)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			try {
				ta.rt(Long.parseLong(m.group(1)));
			} catch (NumberFormatException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をRTしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 3;
		}

		p = Pattern.compile("("+DECIMAL_NUMBER+").?(ふぁぼ|fav)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			try {
				ta.fav(Long.parseLong(m.group(1)));
			} catch (NumberFormatException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をふぁぼりました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 4;
		}

		p = Pattern.compile("("+DECIMAL_NUMBER+").?(削除|消して|けして|消去)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			try {
				delete(Long.parseLong(m.group(1)));
			} catch (NumberFormatException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " 了解しました。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 5;
		}

		p = Pattern.compile("[ 　](削除|消して|けして|消去)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			try {
				delete(mention.getInReplyToStatusId());
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			reply_str = "@" + CREATOR + " わかりました。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 6;
		}

		p = Pattern.compile("つぶやいて$|ツイートして$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			ta.tw();
			return 20;
		}

		p = Pattern.compile("と、?言って$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			str = m.replaceAll("");
			p = Pattern.compile("^@"+BOT_NAME+" ",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			str = m.replaceAll("");
			try {
				tweet(str);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			return 30;
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?(登録(して)?)?([ 　]|「.+」)",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			p = Pattern.compile("@"+BOT_NAME+"[ 　]?登録(して)?[ 　]?",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			int count = -1;
			try {
				count = Resister.resister_tweet(m.replaceAll(""));
				reply_str = "@" + CREATOR + " 登録しました。 (" + count + ")";
			} catch (ResisterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				reply_str = "@" + CREATOR + " 登録に失敗しました。(error 4)";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			return 11;
		}

		p = Pattern.compile("強制終了",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = "@" + CREATOR +  " 強制終了します。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			System.exit(100);
		}

		p = Pattern.compile("爆発しろ",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = "どっかーん！";
			StatusUpdate su = new StatusUpdate(reply_str);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e){
				util.print_time();
				e.printStackTrace();
			}
			System.exit(110);
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?(" + DECIMAL_NUMBER + ")番",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			String s = m.group(1);	//最初にマッチした正規表現を置き換える
			s = util.zenkakuNumToHankaku(s);
			int index = Integer.parseInt(s);
			tweet_call(index);
			return 15;
		}

		// 上記の条件全てに当てはまらなかった場合
		// 通常のリプライのチェックを通す
		if(doReply(mention) < 0) {
			// それでもリプライが生成されなかった場合
			if (reply_str == null) reply_str = "@" + CREATOR + " ?__?";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (TwitterException e) {
				util.print_time();
				e.printStackTrace();
			}
			//*/
		}


		return -1;

		}
	/**
	 * 登録されているツイートのうち、index番目のツイートを呼び出し、履歴ファイルを更新する
	 * @param index : 呼び出すツイートの番号
	 * @return ツイート成功時は正数、失敗時は負数
	 */
	public static int tweet_call(int index) {
		ArrayList<String> al = util.file_to_list(TWEET_FILE);
		if (index < al.size()) {
			try {
				tweet(util.replace_new_line(al.get(index)));
			} catch (TwitterException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				return -2;
			}
			try {
				update_history(index);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return -3;
			}
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * 履歴ファイルの更新
	 * @param index : つぶやいたツイートの番号
	 * @throws IOException 入出力のエラー
	 */
	public static void update_history(int index) throws IOException {
		File file = new File(PREVIEW_FILE);
		ArrayList<String> al = util.file_to_list(file);
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		pw.println(index);
		for(int i=0;i<N_PREVIEW-1;i++){
			pw.println(al.get(i));
		}
		pw.close();
	}

}
