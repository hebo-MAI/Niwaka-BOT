import java.util.Random;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TimeLineStream extends TwitterAction implements StatusListener {
	static Random rnd = new Random();

	public void onException(Exception arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onDeletionNotice(StatusDeletionNotice arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onScrubGeo(long arg0, long arg1) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onStallWarning(StallWarning arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	// ツイートが届いた時の処理
	public void onStatus(Status status) {
		try {
			Thread.sleep(30000 + rnd.nextInt(3000));
		} catch (InterruptedException e) {
		}
		TwitterResponse.doReply(status);

	}

	public void onTrackLimitationNotice(int arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	TimeLineStream(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();
		TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
		twitterStream.addListener(this);
	}

}
