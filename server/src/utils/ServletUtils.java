package utils;


import chat.ChatManager;
import engine.api.Engine;
import engine.impl.EngineImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import user.UserManager;

import static utils.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {
    private static final Object chatManagerLock = new Object();
    private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    public static final String ENGINE_ATTRIBUTE_NAME = "engine";

    private static final Object userManagerLock = new Object();
    private static final Object engineLock = new Object();

    // Retrieves the UserManager instance from the servlet context, creating it if it doesn't exist
    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    // Retrieves the Engine instance from the servlet context, creating it if it doesn't exist
    public static Engine getEngine(ServletContext servletContext) {
        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new EngineImpl());
            }
        }
        return (Engine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }

    public static ChatManager getChatManager(ServletContext servletContext) {
        synchronized (chatManagerLock) {
            if (servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
            }
        }
        return (ChatManager) servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME);
    }
    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }
}
