package servlets.sheetsManagment;

import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permission.PermissionType;

import java.io.IOException;

@WebServlet("/permissionRequest")
public class PermissionRequestServlet extends HttpServlet {
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
        String requestedPermission = request.getParameter("requestedPermission");

        if (sheetName == null || requesterUsername == null || requestedPermission == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        try {
            PermissionType permissionType = PermissionType.valueOf(requestedPermission);
            engine.submitPermissionRequest(sheetName, requesterUsername, permissionType);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid permission type.");
        }
    }
}
