package servlets;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import prediction.DatabaseConnection;
import chat.ChatMessage;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

public class ChatController extends HttpServlet {
	
	String ReturnedjsonStr = "";
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		System.out.println(req);
		System.out.println("^^^^^");
		
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
    	
    	StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        
        try 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
            	sb.append(line);
            }
        } 
        finally 
        {
            reader.close();
        }
        
        System.out.println(sb);
        
        String RequestType = req.getHeader("Type");
        
        DatabaseConnection dbConnection = new DatabaseConnection();
        
        if("createChat".equals(RequestType) && user != null)
        {
        	ChannelService channelService = ChannelServiceFactory.getChannelService();
        	
        	dbConnection.query = "Select PredictionsLeaguePrimKey from F1Prediction.PredictionsLeague where UserEamil = '" + user.getEmail() + "';";
			String[] userPrimKey = dbConnection.runStringQuery();
        	
        	
        	//String channelKey = "ChatKey" + String.valueOf(userPrimKey[0]);
			String channelKey = String.valueOf(System.currentTimeMillis());
        	String channelToken = channelService.createChannel(channelKey);
        	
        	ChatMessage.addSub(channelKey);
        	
        	Gson ReturnedJson = new Gson();
	        
	        //The string is initialised to )]}',\n This is a specific string 
	        //that is used to protect against a Json security vulnerability 
	        
	        ReturnedjsonStr = ")]}',\n";
	        
	        ReturnedjsonStr += ReturnedJson.toJson(channelToken);
        }
        
        try 
    	{
    		resp.setContentType("application/json");  
        	resp.setCharacterEncoding("UTF-8");
    		resp.getWriter().write(ReturnedjsonStr);
		} 
    	catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		System.out.println(req);
		System.out.println("^^^^^");
		
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
				
    	Gson JsonParser  = new Gson();
    	
    	StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        
        try 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
            	sb.append(line);
            }
        } 
        finally 
        {
            reader.close();
        }
        
        System.out.println(sb);
        
        String RequestType = req.getHeader("Type");
        
        DatabaseConnection dbConnection = new DatabaseConnection();
        
        if("sendMsg".equals(RequestType) && user != null)
        {
        	//need to first store this GSon
        	
        	ChatMessage SendMessage = JsonParser.fromJson(sb.toString(),ChatMessage.class);
        	
        	SendMessage.sendMessage();
        	
        	Gson ReturnedJson = new Gson();
        	
        	ReturnedjsonStr = ")]}',\n";
	        
	        ReturnedjsonStr += ReturnedJson.toJson("yep");
        }
        
        try 
    	{
    		resp.setContentType("application/json");  
        	resp.setCharacterEncoding("UTF-8");
    		resp.getWriter().write(ReturnedjsonStr);
		} 
    	catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
