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
import java.util.List;

@WebServlet("/getUniqueValues")
public class GetUniqueValuesServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String range = request.getParameter("range");
        String column = request.getParameter("column");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            List<String> uniqueValues = engine.getUniqueValuesInRangeColumn(sheetName, range, column);
            String jsonResponse = new Gson().toJson(uniqueValues);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error retrieving unique values: " + e.getMessage()));
            out.flush();
        }
    }
}
