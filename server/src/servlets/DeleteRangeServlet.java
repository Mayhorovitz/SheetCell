package servlets;
import com.google.gson.Gson;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
@WebServlet("/deleteRange")
public class DeleteRangeServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String rangeName = request.getParameter("rangeName");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            engine.deleteRangeFromSheet(sheetName, rangeName);
            out.print(new Gson().toJson("Range deleted successfully"));
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error deleting range: " + e.getMessage()));
            out.flush();
        }
    }
}
