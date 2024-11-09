package servlets.sheetsManagment;

import com.google.gson.Gson;
import dto.api.PermissionRequestDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permission.PermissionStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/permissionsTable")
public class PermissionsTableServlet extends HttpServlet {
    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        if (sheetName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid sheet name.");
            return;
        }

        try {
            List<PermissionRequestDTO> permissionRequestsDTO = engine.getPermissionRequestsDTO(sheetName);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Gson gson = new Gson();
            out.println(gson.toJson(permissionRequestsDTO));
            out.flush();
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String approverUsername = request.getParameter("approverUsername");
        int requestIndex = Integer.parseInt(request.getParameter("requestIndex"));
        String status = request.getParameter("status");

        if (sheetName == null || approverUsername == null || status == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        try {
            engine.handlePermissionRequest(sheetName, requestIndex, approverUsername, PermissionStatus.valueOf(status));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
