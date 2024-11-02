package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import user.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

// Login Servlet
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) { // User is not logged in yet
            String usernameFromParameter = request.getParameter("username");

            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                // No username in session and no username in parameter - conflict situation
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print("Username cannot be empty.");
            } else {
                // Normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username already exists.";

                        // Conflict as there is already a user with this name
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getOutputStream().print(errorMessage);
                    } else {
                        // Add the new user to the users list
                        userManager.addUser(usernameFromParameter);

                        // Set the username in the session so it will be available on each request
                        // The true parameter means that if a session object does not exist yet, create a new one
                        request.getSession(true).setAttribute("username", usernameFromParameter);

                        // Log the request URI
                        System.out.println("On login, request URI is: " + request.getRequestURI());
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            }
        } else {
            // User is already logged in
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().print("User already logged in.");
        }
    }
}
