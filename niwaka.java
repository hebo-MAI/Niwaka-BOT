// UTF-8N , LF
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.Status;
import twitter4j.ResponseList;


public class niwaka {

	private static String CONSUMER_KEY;
	private static String CONSUMER_SECRET;
	private static String ACCESS_TOKEN;
	private static String ACCESS_SECRET;

	private static final File RESPONSE_FILE = new File("resister.txt");
	private static final File TWEET_FILE = new File("tweet.txt");

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	static final String CREATOR = "hebo_MAI";
	static final String BOT_NAME = "niwaka_bot";

	private static final int N_PREVIEW = 11;

	public static void main(String[] args) {
		load_key();
		try {
			//引数によるオプション
			if (args.length>0){
				if (args[0].equals("fav") && args.length==2){
					fav(Long.parseLong(args[1]));
					System.exit(0);
				}
				if ((args[0].equals("rt") || args[0].equals("retweet")) && args.length==2) {
					rt(Long.parseLong(args[1]));
					System.exit(0);
				}
				if ((args[0].equals("tw") || args[0].equals("tweet")) && args.length==1) {
					tw();
					System.exit(0);
				}
				if (args[0].equals("say")) {
					say(args);
					System.exit(0);
				}
				if (( (args[0].equals("fav") && args[1].equals("rt"))
					||(args[0].equals("rt") && args[1].equals("fav")) )
					&& args.length==3) {
					fav(Long.parseLong(args[2]));
					rt(Long.parseLong(args[2]));
					System.exit(0);
				}
				if (args[0].equals("start") && args.length==1) {
					bot();
					System.exit(0);
				}
				if (args[0].equals("reply") && args.length<3) {
					if (args.length==2) reply(Long.parseLong(args[1]));		//引数のIDに対してリプライを行う
					else reply();											//リプライを行なっていないツイートに対してリプライを行う
					System.exit(0);
				}
				if (args[0].equals("response") && args.length==1) {
					System.out.println(responseTimeline());
					System.exit(0);
				}
				if (args[0].equals("refollow") && args.length==1) {
					//autoRefollow();
					System.out.println("This function doesn't work!");
					System.exit(0);
				}
				if (args[0].equals("debug") && args.length==1) {
					debug();
					System.exit(0);
				}
				System.out.println("Error Wrong Argument.");
				System.exit(5);
			} else {
				System.out.println("Usage: ");
				System.out.println("\t fav number : favorite");
				System.out.println("\t rt number : retweet");
				System.out.println("\t tw : bot tweet");
				System.exit(4);
			}

		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		}
	}
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

	public static void tw() {
		try {
			//ツイート履歴の読込
			File file = new File("preview.txt");
			if (file.exists()==false){
				System.err.println("\"preview.txt\" does not exist!");
				System.exit(1);
			}
			FileReader fr= new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			int[] preview = new int [N_PREVIEW];	//N回前までのツイートの番号を格納
			String line;
			for(int i=0;i<N_PREVIEW;i++){
				line = br.readLine();
				preview[i] = Integer.parseInt(line);
			}

			//ツイートのネタの読込
			ArrayList<String> list = util.file_to_list(TWEET_FILE);
			int n_tweet = list.size();

			int rnd = (int)(Math.random()*n_tweet);
			int fl = 0;
			while(fl<N_PREVIEW){
				fl=0;
				for(int i=0;i<N_PREVIEW;i++){	//N回前までのツイートと同じものが選ばれたら再抽選
					if (rnd==preview[i]) {
						rnd = (int)(Math.random()*n_tweet);
					} else {
						fl++;
					}
				}
			}

			//ツイート履歴の更新
			for(int i=N_PREVIEW-2;i>=0;i--){
				preview[i+1] = preview[i];
			}
			preview[0] = rnd;

			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(int i=0;i<N_PREVIEW;i++){
				pw.println(preview[i]);
			}
			pw.close();

			//System.out.println(rnd);
			String s = list.get(rnd);
			//改行を表す"\n"を文字列に含むツイートの仮の改行を、実際の改行文字と置換する。
			s = util.replace_new_line(s);
			tweet(s);

		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		}

	}

	public static void say(String[] args){
		int length = args.length;
		String str = "";
		for(int i=1 ; i<length ; i++){
			str += " " + args[i];	//シェルによって分けられた文を、プログラムにより結合
		}
		tweet(str);
	}

	// バックグラウンド実行用
	public static void bot(){
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
		Random rnd = new Random();
		Calendar cal;
		int hour,minute;
		int random_time;
		while (true){
			random_time = 15;
			for(int i=0;i<3;i++){
				random_time += rnd.nextInt(10);
			}
			for(int i=0;i<random_time;i++){
				try {
				Thread.sleep(50000+rnd.nextInt(5000));
				} catch(Exception e) {
				}
				//if (i%5 == 0) {
					reply();
					responseTimeline();
				//}
			}
			cal = Calendar.getInstance();
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
			if (hour>=19 || hour<=2 || (hour==12 && minute<=30)) {
				tw();
				try {
					Thread.sleep(100000+rnd.nextInt(10000));
				} catch(Exception e){
				}
			}
			//autoRefollow();
		}
	}

	public static void tweet(String message) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);

		// Twitterのアプリケーション設定画面の値を入れる
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);

		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		try {
			twitter.updateStatus(message);
		} catch (TwitterException e) {
			util.print_time();
			e.printStackTrace();
		}
	}

	public static int fav(long id) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);

		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		try {
			twitter.createFavorite(id);
			return 1;
		} catch (TwitterException e) {
			return -1;
		}
	}

	public static int rt(long id) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);

		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		try {
			twitter.retweetStatus(id);
			return 1;
		} catch (TwitterException e) {
			return -1;
		}
	}

	public static void delete(long id) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		try {
			twitter.destroyStatus(id);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	public static void autoRefollow(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		try {
			IDs friends = twitter.getFriendsIDs(twitter.getId());
			IDs follower = twitter.getFollowersIDs(twitter.getId());

			// 片思いユーザ群を取得し、フォローする
			for(long userid : follower.getIDs()) {
				if(! contains(friends, userid)) {
					follow(userid);
				}
			}
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static boolean contains(IDs friends, long userid) {
		for(long friendsId : friends.getIDs()) {
			if(friendsId == userid) {
				return true;
			}
		}
		return false;
	}

	public static int follow(long id) throws TwitterException {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		User user = twitter.createFriendship(id);
		if(user == null) return -1;

		return 0;
	}

	public static long responseTimeline() {
		ResponseList<Status> userTimeline = null;
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
			in = new FileReader("read_log.txt");
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

		try {
			//userTimeline = twitter.getFriendsTimeline(paging);	//使用すべきではない
			userTimeline = twitter.getHomeTimeline(paging);
			//userTimeline = twitter.getHomeTimeline();
		} catch (Exception e) {
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
				File file = new File("read_log.txt");
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
			in = new FileReader("read_log.txt");
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

		// @niwaka_bot を含むツイートを除外する
		p = Pattern.compile("@"+BOT_NAME, Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find())	return 1;

		//twittbot.netを含むソースからのツイートに対しては反応しない
		p = Pattern.compile("twittbot\\.net", Pattern.CASE_INSENSITIVE);
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
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CONSUMER_KEY);
				builder.setOAuthConsumerSecret(CONSUMER_SECRET);
				builder.setOAuthAccessToken(ACCESS_TOKEN);
				builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
				Configuration conf = builder.build();

				Twitter twitter = new TwitterFactory(conf).getInstance();
				twitter.updateStatus(su);
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
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CONSUMER_KEY);
				builder.setOAuthConsumerSecret(CONSUMER_SECRET);
				builder.setOAuthAccessToken(ACCESS_TOKEN);
				builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
				Configuration conf = builder.build();

				Twitter twitter = new TwitterFactory(conf).getInstance();
				twitter.updateStatus(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 15;
		}

		p = Pattern.compile("[(ぴの)|(pino)|(ｐｉｎｏ)|(ピノ)][(くん)君].*にわか",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 3;
			String reply = "@" + name + " 俺にわかじゃないよ";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CONSUMER_KEY);
				builder.setOAuthConsumerSecret(CONSUMER_SECRET);
				builder.setOAuthAccessToken(ACCESS_TOKEN);
				builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
				Configuration conf = builder.build();

				Twitter twitter = new TwitterFactory(conf).getInstance();
				twitter.updateStatus(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 13;
		}
		p = Pattern.compile("にわか.*[(ぴの)|(pino)|(ｐｉｎｏ)|(ピノ)][(くん)君]",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			long id = status.getId();
			String name = status.getUser().getScreenName();
			if (name.equals(BOT_NAME)) return 4;
			String reply = "@" + name + " にわかじゃないよ!";
			StatusUpdate su = new StatusUpdate(reply);
			su.setInReplyToStatusId(id);
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CONSUMER_KEY);
				builder.setOAuthConsumerSecret(CONSUMER_SECRET);
				builder.setOAuthAccessToken(ACCESS_TOKEN);
				builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
				Configuration conf = builder.build();

				Twitter twitter = new TwitterFactory(conf).getInstance();
				twitter.updateStatus(su);
				Thread.sleep(5000);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 14;
		}
		return -1;
	}

	public static void reply() {
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
			in = new FileReader("reply_log.txt");
			BufferedReader br = new BufferedReader(in);
			try {
				lastPostId = Long.parseLong(br.readLine()) + 1;
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
			}
		} catch (Exception e){
			util.print_time();
			e.printStackTrace();
		}
		paging.setSinceId(lastPostId);

		ResponseList<Status> mentions = null;
		try {
			mentions = twitter.getMentions();
		} catch (Exception e) {
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
					File file = new File("reply_log.txt");
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
	public static void reply(long id) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();

		ResponseList<Status> mentions = null;

		try {
			mentions = twitter.getMentions();
		} catch (Exception e) {
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
		//String name = "@" + mention.getInReplyToScreenName();
		String name = "@" + mention.getUser().getScreenName();
		//自分自身にはリプライしない
		if (name.equals("@"+BOT_NAME)) return 100;
		String reply_str = null;
		Pattern p;
		Matcher m;

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?「.+」$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			p = Pattern.compile("@"+BOT_NAME+" ",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			int count = resister_tweet(m.replaceAll(""));
			if (count > 0) {
				reply_str = name + " 登録しました。 (" + count + ")";
			} else {
				reply_str = name + " 登録に失敗しました。";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 100;
		}
		p = Pattern.compile("@"+BOT_NAME+"[ 　]?登録(して)?[ 　]?「",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			int count = resister_tweet(m.replaceAll("「"));
			if (count > 0) {
				reply_str = name + " 登録しました。 (" + count + ")";
			} else {
				reply_str = name + " 登録に失敗しました。";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 101;
		}
		p = Pattern.compile("@"+BOT_NAME+"[ 　]?登録(して)?[ 　]|",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			int count = resister_tweet(m.replaceAll(""));
			if (count > 0) {
				reply_str = name + " 登録しました。 (" + count + ")";
			} else {
				reply_str = name + " 登録に失敗しました。";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 102;
		}

		p = Pattern.compile("^.+@"+BOT_NAME+"|^@"+BOT_NAME+".?$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			if (Math.random()<0.5)	reply_str = name + " 呼んだ？";
			else					reply_str = name + " ねえ、呼んだ？";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 10;
		}
		p = Pattern.compile("^@"+BOT_NAME+"[ 　]?呼んでない",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + " あ、そう。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
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
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 12;
		}
		p = Pattern.compile("#ぴのくんはにわか",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			reply_str = name + "おいやめろ";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 8;
		}

		p = Pattern.compile("にわか",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			if (Math.random()<0.5)	reply_str = name + " 俺にわかじゃないよ。";
			else					reply_str = name + " だからにわかじゃないって。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 1;
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?([0-9０１２３４５６７８９]+)番",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			String s = m.group(1);	//最初にマッチした正規表現を置き換える
			s = util.zenkakuNumToHankaku(s);
			int index = Integer.parseInt(s);
			tweet_call(index);
			return 3;
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

		p = Pattern.compile("(\\d+).?fav.rt",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			fav(Long.parseLong(m.group(1)));
			rt(Long.parseLong(m.group(1)));
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をfav&rtしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 1;
		}

		p = Pattern.compile("(\\d+).?rt.fav",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			rt(Long.parseLong(m.group(1)));
			fav(Long.parseLong(m.group(1)));
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をrt&favしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 2;
		}

		p = Pattern.compile("(\\d+).?rt|(\\d+).?リツイート",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			rt(Long.parseLong(m.group(1)));
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をRTしました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 3;
		}

		p = Pattern.compile("(\\d+).?ふぁぼ|(\\d+).?fav");
		m = p.matcher(str);
		if (m.find()) {
			fav(Long.parseLong(m.group(1)));
			reply_str = "@" + CREATOR + " " + m.group(1).concat("をふぁぼりました。");
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 4;
		}

		p = Pattern.compile("(\\d+).?削除|(\\d+).?消して|(\\d+).?けして|(\\d+).?消去",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			delete(Long.parseLong(m.group(1)));
			reply_str = "@" + CREATOR + " 了解しました。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 5;
		}

		p = Pattern.compile(" 削除| 消して| けして| 消去",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			delete(mention.getInReplyToStatusId());
			reply_str = "@" + CREATOR + " わかりました。";
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			return 6;
		}

		p = Pattern.compile("つぶやいて$|ツイートして$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			tw();
			return 20;
		}

		p = Pattern.compile("と、?言って$",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			str = m.replaceAll("");
			p = Pattern.compile("^@"+BOT_NAME+" ",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			str = m.replaceAll("");
			tweet(str);
			return 30;
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?「",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()){
			p = Pattern.compile("」$",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			if (m.find()){
				p = Pattern.compile("@"+BOT_NAME+" ",Pattern.CASE_INSENSITIVE);
				m = p.matcher(str);
				str = m.replaceAll("");
				p = Pattern.compile("@" + CREATOR + " ",Pattern.CASE_INSENSITIVE);
				m = p.matcher(str);
				if (m.find()) str = m.replaceAll("");
				p = Pattern.compile("@",Pattern.CASE_INSENSITIVE);
				m = p.matcher(str);
				if (m.find()) return 15;


				int count = resister_tweet(str);
				if (count > 0) {
					reply_str = "@" + CREATOR + " 登録しました。 (" + count + ")";
				} else {
					reply_str = "@" + CREATOR + " 登録に失敗しました。";
				}
				StatusUpdate su = new StatusUpdate(reply_str);
				su.setInReplyToStatusId(id);
				try {
					twitter.updateStatus(su);
				} catch (Exception e){
					util.print_time();
					e.printStackTrace();
				}
				return 10;
			}
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?登録(して)?[ 　「].+」",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			p = Pattern.compile("@"+BOT_NAME+"[ 　]?登録(して)?[ 　]",Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			int count = resister_tweet(m.replaceAll(""));
			if (count > 0) {
				reply_str = "@" + CREATOR + " 登録しました。 (" + count + ")";
			} else {
				reply_str = "@" + CREATOR + " 登録に失敗しました。";
			}
			StatusUpdate su = new StatusUpdate(reply_str);
			su.setInReplyToStatusId(id);
			try {
				twitter.updateStatus(su);
			} catch (Exception e){
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
			} catch (Exception e){
				util.print_time();
				e.printStackTrace();
			}
			System.exit(100);
		}

		p = Pattern.compile("@"+BOT_NAME+"[ 　]?([0-9０１２３４５６７８９]+)番",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) {
			String s = m.group(1);	//最初にマッチした正規表現を置き換える
			s = util.zenkakuNumToHankaku(s);
			int index = Integer.parseInt(s);
			tweet_call(index);
			return 15;
		}

		if (reply_str == null) reply_str = "@" + CREATOR + " ?__?";
		StatusUpdate su = new StatusUpdate(reply_str);
		su.setInReplyToStatusId(id);
		//System.out.println(reply_str);
		try {
			twitter.updateStatus(su);
		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		}

		return -1;

		}

	public static int resister_tweet(String str) {
		Pattern p;
		Matcher m;
		p = Pattern.compile("pino|ぴの",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		if (m.find()) return -1;

		//改行文字をすべて"\n"に置き換え、意図しない改行を防ぐ
		p = Pattern.compile(LINE_SEPARATOR);
		m = p.matcher(str);
		if (m.find()) m.replaceAll("\n");

		//ツイートをリストに登録する
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TWEET_FILE,true),"UTF-8"));
			bw.write(LINE_SEPARATOR + str);
			//bw.write(str);
			bw.close();
		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
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

		return util.count_line(TWEET_FILE);
	}

	/**
	 * 登録されているツイートのうち、index番目のツイートを呼び出し、履歴ファイルを更新する
	 * @param index : 呼び出すツイートの番号
	 * @return ツイート成功時は正数、失敗時は負数
	 */
	public static int tweet_call(int index) {
		ArrayList<String> al = util.file_to_list(TWEET_FILE);
		if (index < al.size()) {
			tweet(util.replace_new_line(al.get(index)));
			try {
				update_history(index);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
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
		File file = new File("preview.txt");
		ArrayList<String> al = util.file_to_list(file);
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		pw.println(index);
		for(int i=0;i<N_PREVIEW-1;i++){
			pw.println(al.get(i));
		}
		pw.close();
	}

	/**
	 * 特定のキーワードに反応するためのワード等を登録する
	 * @param str1 : 反応するキーワード
	 * @param str2 : 反応した時につぶやく内容
	 * @return : 登録成功時は正数、失敗時は負数
	 */
	public static int resister_response(String str1,String str2){
		Pattern p;
		Matcher m;
		p = Pattern.compile("pino|ぴの",Pattern.CASE_INSENSITIVE);
		m = p.matcher(str1);
		if (m.find()) return -1;

		m = p.matcher(str2);
		if (m.find()) return -2;

		//改行文字をすべて"\n"に置き換え、意図しない改行を防ぐ
		p = Pattern.compile(LINE_SEPARATOR);
		m = p.matcher(str1);
		if (m.find()) m.replaceAll("\n");
		m = p.matcher(str2);
		if (m.find()) m.replaceAll("\n");

		//反応をリストに登録する
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TWEET_FILE,true),"UTF-8"));
			bw.write(LINE_SEPARATOR + str1 + LINE_SEPARATOR + str2);
			//bw.write(str);
			bw.close();
		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e) {
				util.print_time();
				e.printStackTrace();
			}
		}

		return util.count_line(RESPONSE_FILE)/2;
	}

	/**
	 * ツイートするために必要な各種情報を読み込む
	 */
	public static void load_key() {
		FileInputStream fis;
		BufferedReader br = null;
		String line = null;
		try {
			fis = new FileInputStream("key.txt");
			br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
		} catch (Exception e) {
			util.print_time();
			e.printStackTrace();
		}
		try {
			if ( (line = br.readLine()) != null ) {
				CONSUMER_KEY = line;
			}
			if ( (line = br.readLine()) != null ) {
				CONSUMER_SECRET = line;
			}
			if ( (line = br.readLine()) != null ) {
				ACCESS_TOKEN = line;
			}
			if ( (line = br.readLine()) != null ) {
				ACCESS_SECRET = line;
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			System.exit(-1);
		}

	}
	static class util {

		/**
		 * 呼び出したときの時間をエラー出力に表示する
		 */
		public static void print_time() {
			Calendar c = Calendar.getInstance();

			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DATE);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			int second = c.get(Calendar.SECOND);

	    	System.err.print("[" + month + "/" + day + " " + hour + ":" + minute + ":" + second + "] ");

		}


		/** http://www7a.biglobe.ne.jp/~java-master/samples/string/HankakuNumberToZenkakuNumber.html
		 * 全角数字を半角に変換します。
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
}