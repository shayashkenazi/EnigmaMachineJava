package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.*;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                         throws ServletException, IOException {

        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //response.setContentType("text/html;charset=UTF-8"); TODO: what is this ?
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) { //user is not logged in yet

            String usernameFromParameter = request.getParameter(USERNAME);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session and no username in parameter -
                //redirect back to the index page
                //this return an HTTP code back to the browser telling it to load
                response.sendRedirect(SIGN_UP_URL);
            } else {

                usernameFromParameter = usernameFromParameter.trim(); //normalize the username value

                /*
                One can ask why not enclose all the synchronizations inside the userManager object ?
                Well, the atomic action we need to perform here includes both the question (isUserExists) and (potentially) the insertion
                of a new user (addUser). These two actions needs to be considered atomic, and synchronizing only each one of them, solely, is not enough.
                (of course there are other more sophisticated and performable means for that (atomic objects etc) but these are not in our scope)

                The synchronized is on this instance (the servlet).
                As the servlet is singleton - it is promised that all threads will be synchronized on the very same instance (crucial here)

                A better code would be to perform only as little and as necessary things we need here inside the synchronized block and avoid
                do here other not related actions (such as request dispatcher\redirection etc. this is shown here in that manner just to stress this issue
                 */
                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";
                        // username already exists, forward the request back to index.jsp
                        // with a parameter that indicates that an error should be displayed
                        // the request dispatcher obtained from the servlet context is one that MUST get an absolute path (starting with'/')
                        // and is relative to the web app root
                        // see this link for more details:
                        // http://timjansen.github.io/jarfiller/guide/servlet25/requestdispatcher.xhtml
                        request.setAttribute(Constants.USER_NAME_ERROR, errorMessage);
                        getServletContext().getRequestDispatcher(LOGIN_ERROR_URL).forward(request, response);
                    }
                    else {
                        //add the new user to the users list
                        userManager.addUser(usernameFromParameter);
                        //set the username in a session so it will be available on each request
                        //the true parameter means that if a session object does not exists yet
                        //create a new one
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);

                        //redirect the request to the chat room - in order to actually change the URL
                        System.out.println("On login, request URI is: " + request.getRequestURI());
                        response.sendRedirect(CHAT_ROOM_URL);
                    }
                }
            }
        } else {
            //user is already logged in
            response.sendRedirect(CHAT_ROOM_URL);
        }
    }
}
