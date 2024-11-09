package servlets.sheetsManagment;

import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permission.PermissionStatus;

import java.io.IOException;

@WebServlet("/permissionResponse")
public class PermissionResponseServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String requesterUsername = request.getParameter("requesterUsername");
        String approverUsername = (String) request.getSession().getAttribute("username");
        String status = request.getParameter("status");

        if (sheetName == null || requesterUsername == null || status == null || approverUsername == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        try {
            PermissionStatus permissionStatus = PermissionStatus.valueOf(status);
            engine.handleResponseRequest(sheetName, requesterUsername, approverUsername, permissionStatus);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid permission status.");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while handling the permission request.");
        }
    }
}
