import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterAction extends Bot {

	protected static String CONSUMER_KEY;
	protected static String CONSUMER_SECRET;
	protected static String ACCESS_TOKEN;
	protected static String ACCESS_SECRET;

	static final File TWEET_FILE = new File("tweet.txt");

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	static final String CREATOR = "hebo_MAI";
	static final String BOT_NAME = "niwaka_bot";

	static final int N_PREVIEW = 11;

	TwitterAction() {
		load_key();
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

	public int fav(long id) {
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

	public int rt(long id) {
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
	public void tw() {
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

	public void say(String[] args){
		int length = args.length;
		String str = "";
		for(int i=1 ; i<length ; i++){
			str += " " + args[i];	//シェルによって分けられた文を、プログラムにより結合
		}
		tweet(str);
	}

	/**
	 * ツイートするために必要な各種情報を読み込む
	 */
	public void load_key() {
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


}
