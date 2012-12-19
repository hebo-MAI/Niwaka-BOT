// UTF-8N , LF

public class niwaka {


	public static void main(String[] args) {
		try {
			//引数によるオプション
			if (args.length>0){
				if (args[0].equals("fav") && args.length==2){
					TwitterAction ta = new TwitterAction();
					ta.fav(Long.parseLong(args[1]));
					System.exit(0);
				}
				if ((args[0].equals("rt") || args[0].equals("retweet")) && args.length==2) {
					TwitterAction ta = new TwitterAction();
					ta.rt(Long.parseLong(args[1]));
					System.exit(0);
				}
				if ((args[0].equals("tw") || args[0].equals("tweet")) && args.length==1) {
					TwitterResponse tr = new TwitterResponse();
					tr.tw();
					System.exit(0);
				}
				if (args[0].equals("say")) {
					TwitterAction ta = new TwitterAction();
					ta.say(args);
					System.exit(0);
				}
				if (( (args[0].equals("fav") && args[1].equals("rt"))
					||(args[0].equals("rt") && args[1].equals("fav")) )
					&& args.length==3) {
					TwitterAction ta = new TwitterAction();
					ta.fav(Long.parseLong(args[2]));
					ta.rt(Long.parseLong(args[2]));
					System.exit(0);
				}
				if (args[0].equals("start") && args.length==1) {
					Bot bot = new Bot();
					bot.bot();
					System.exit(0);
				}
				if (args[0].equals("reply") && args.length<3) {
					TwitterResponse tr = new TwitterResponse();
					if (args.length==2) tr.checkReply(Long.parseLong(args[1]));		//引数のIDに対してリプライを行う
					else tr.makeReply();											//リプライを行なっていないツイートに対してリプライを行う
					System.exit(0);
				}
				if (args[0].equals("response") && args.length==1) {
					TwitterResponse tr = new TwitterResponse();
					System.out.println(tr.responseTimeline());
					System.exit(0);
				}
				if (args[0].equals("refollow") && args.length==1) {
					//autoRefollow();
					System.out.println("This function doesn't work!");
					System.exit(0);
				}
				if (args[0].equals("debug") && args.length==1) {
					Debug.debug();
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
}