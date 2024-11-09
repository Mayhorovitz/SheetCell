package servlets.sheetsManagment;

import com.google.gson.Gson;
import dto.api.PermissionRequestDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/ownedSheetsPendingRequests")
public class OwnedSheetsPendingRequestsServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String ownerUsername = request.getParameter("ownerUsername");

        if (ownerUsername == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing owner username parameter.");
            return;
        }

        try {
            List<PermissionRequestDTO> pendingRequests = engine.getPendingRequestsForOwner(ownerUsername);
            String jsonResponse = new Gson().toJson(pendingRequests);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while fetching pending requests.");
        }
    }
}
