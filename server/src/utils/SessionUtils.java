package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    // Retrieves the username stored in the session
    public static String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    // Stores the username in the session
    public static void setUsername(HttpServletRequest request, String userName) {
        HttpSession session = request.getSession(true);
        session.setAttribute(Constants.USERNAME, userName);
    }

    // Clears the session
    public static void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
