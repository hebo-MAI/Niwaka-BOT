import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class UserTimelineStream extends TwitterAction {

	UserTimelineStream(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(CONSUMER_KEY);
		builder.setOAuthConsumerSecret(CONSUMER_SECRET);
		builder.setOAuthAccessToken(ACCESS_TOKEN);
		builder.setOAuthAccessTokenSecret(ACCESS_SECRET);
		Configuration conf = builder.build();
		TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();

		twitterStream.addListener(new MyUserStreamAdapter());
		twitterStream.user();
	}

}
