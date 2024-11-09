package servlets.sheetView;

import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/resetCellDesign")
public class ResetCellDesignServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
        if (engine == null) {
            throw new ServletException("Engine not initialized");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String cellId = request.getParameter("cellId");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            if (sheetName == null || sheetName.isEmpty() || cellId == null || cellId.isEmpty()) {
                throw new IllegalArgumentException("Sheet name and cell ID must be provided.");
            }

            // Call engine to reset the cell design
            engine.resetCellDesign(sheetName, cellId);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"message\": \"Cell design reset successfully\"}");
            out.flush();
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}");
            out.flush();
        }
    }
}
