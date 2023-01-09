package servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

public class UserSignIn extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        
        String test = "";
        String RequestType = req.getHeader("Type");
        
        if("checkSignIn".equals(RequestType))
        {
        	if (user == null) 
	        {
	        	test = "false";
	        } else 
	        {
	            test = "true";
	            System.out.println(user.getEmail());
	        }
        	resp.setContentType("application/json");
        	resp.setCharacterEncoding("UTF-8");
    		resp.getWriter().write(test);
        }
        else if("SignIn".equals(RequestType))
        {
	        if(user != null)
	        {
	        	resp.sendRedirect("/");
	        	System.out.println("in here");
	        }
	        else 
	        {
	            test = userService.createLoginURL(req.getRequestURI());
	            System.out.println(test + "   : out here");
	        }
	        
	        Gson ReturnedJson = new Gson();
	        
			//The string is initialised to )]}',\n This is a specific string 	
	        //that is used to protect against a Json security vulnerability 
			String ReturnedjsonStr = ")]}',\n";
			ReturnedjsonStr += ReturnedJson.toJson(test);
			
			System.out.println(ReturnedjsonStr);
			
	    	try 
	    	{
	    		resp.setContentType("application/json");  
	        	resp.setCharacterEncoding("UTF-8");
	    		resp.getWriter().write(test);
			} 
	    	catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else
        {
        	resp.sendRedirect("/");
        }
	}
}
