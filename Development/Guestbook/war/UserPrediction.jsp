<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>

  <head></head>

  <body>

<%
    String guestbookName = request.getParameter("guestbookName");
    if (guestbookName == null) {
        guestbookName = "default";
    }
    pageContext.setAttribute("guestbookName", guestbookName);
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
      pageContext.setAttribute("user", user);
%>
<p>Hello, ${fn:escapeXml(user.nickname)}! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<%
    } else {
%>
<p>Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to include your name with greetings you post.</p>
<%
    }
%>

<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key guestbookKey = KeyFactory.createKey("Guestbook", guestbookName);
    // Run an ancestor query to ensure we see the most up-to-date
    // view of the Greetings belonging to the selected Guestbook.
    Query query = new Query("Greeting", guestbookKey).addSort("date", Query.SortDirection.DESCENDING);
    List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(3));
    if (greetings.isEmpty()) {
        %>
        <p>Guestbook '${fn:escapeXml(guestbookName)}' has no messages.</p>
        <%
    } else {
        %>
        <p>Messages in Guestbook '${fn:escapeXml(guestbookName)}'.</p>
        <%
        for (Entity greeting : greetings) {
        	pageContext.setAttribute("greeting_content",
                    greeting.getProperty("content"));
        	
            
            pageContext.setAttribute("greeting_content",
                    greeting.getProperty("Outcome"));
                    
            if (greeting.getProperty("user") == null) {
                %>
                <p>An anonymous person wrote:</p>
                <%
            } else {
                pageContext.setAttribute("greeting_user",
                                         greeting.getProperty("user"));
                %>
                <p><b>${fn:escapeXml(greeting_user.nickname)}</b> wrote:</p>
                <%
            }
            %>
            <blockquote>${fn:escapeXml(greeting_content)}</blockquote>
            <%
        }
    }
%>

	Please select how many training varialbes you will be using
    <form action="/UserPrediction" method="post">
      <select name="TrainingVarCount" required = "true">
      	<option value=1>1</option>
      	<option value=2>2</option>
      </select>
      <br><br>
      <textarea name="TrainingVar1" rows="10" cols="15" placeholder="Training: 1st Explanatory Variables" required="true"></textarea>
      <textarea name="TrainingVar2" rows="10" cols="15" placeholder="Training: 2nd Explanatory Variables"></textarea>
      <textarea name="TrainingResponse" rows="10" cols="15" placeholder ="Training: Response Variables" required="true"></textarea>
      <br>
      <textarea name="TestingVar1" rows="10" cols="15" placeholder = "Testing: 1st Explanatory Variables" required="true"></textarea>
      <textarea name="TestingVar2" rows="10" cols="15" placeholder = "Testing: 2st Explanatory Variables"></textarea>
      <div><input type="submit" value="Run Prediction" /></div>
      <input type="hidden" name="guestbookName" value="${fn:escapeXml(guestbookName)}"/>
    </form>
    
    <!-- <button type="button">Click Me!</button> -->
    
    <br>
    
    <!--<button onclick="myFunction()",type="button", name = "asdf">2+2=?</button>-->
    
    <p>
    <form action="/sign" method="post" onsubmit="return myFunction(this);">
      <div><textarea name="test1" rows="3" cols="60" placeholder ="Used to test on submit function"></textarea></div>
      <div><textarea name="test2" rows="3" cols="60" placeholder ="Used to test on submit function"></textarea></div>
      <div><input type="submit" value="Post Greeting" /></div>
      <input type="hidden" name="guestbookName" value="${fn:escapeXml(guestbookName)}"/>
    </form>
    </p>
    
    <button type="button" onclick="myFunction()">Test Button?Nothing</button>
    
    <p id="demo"></p>
    <script>
	function myFunction(theForm)
	{
		if(theForm.test1.value.length == theForm.test2.value.length && theForm.test1.value.length != 0)
		{
			document.getElementById("demo").innerHTML = Date();
			alert("this is right");
			return false;
		}
		else
		{
			alert("this is wrong");
			return false;
		}
	}
	</script>

  </body>
</html>