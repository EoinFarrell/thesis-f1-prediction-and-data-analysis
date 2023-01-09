package chat;

import java.util.HashSet;
import java.util.Iterator;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class ChatMessage 
{
	public String Message;
	public String Channel;
	private static HashSet<String> subs = new HashSet<String>();
	
	//Check subscription
		public static boolean isSubscribed(String sub) {
			return subs.contains(sub);
		}

		//Remove subscription
		public static void removeSub(String sub) {
			subs.remove(sub);
		}

		//Add a new subscription
		public static void addSub(String sub) {
			subs.add(sub);
		}
	
	public void sendMessage()
	{
		System.out.println("Message: " + Message + " : Channel : " + Channel);
		
		Iterator<String> it = subs.iterator();
		
		while(it.hasNext())
		{
			String sub = it.next();
			
			ChannelService channelService = ChannelServiceFactory.getChannelService();
		
			channelService.sendMessage(new ChannelMessage(sub, Message));
		}
	}
}
