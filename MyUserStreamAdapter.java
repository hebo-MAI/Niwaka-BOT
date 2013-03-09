import twitter4j.Status;
import twitter4j.UserStreamAdapter;


public class MyUserStreamAdapter extends UserStreamAdapter {

	@Override	// ツイートが届いた時の処理
	public void onStatus(Status status) {
		TwitterResponse.doReply(status);
		// for testing
		/*
			System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
		//*/
	}
}
