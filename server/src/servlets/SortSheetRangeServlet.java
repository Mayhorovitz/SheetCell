package servlets;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/sortSheetRange")
public class SortSheetRangeServlet extends HttpServlet {

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
        String columns = request.getParameter("columns");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Validate input
            if (sheetName == null || range == null || columns == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new Gson().toJson("Missing required parameters"));
                out.flush();
                return;
            }

            // Parse columns into an array
            String[] columnArray = columns.split(",");

            // Sort the sheet range by columns
            SheetDTO sortedSheetDTO = engine.sortSheetRangeByColumns(sheetName, range, columnArray);

            // Send the sorted sheet as a response
            String jsonResponse = new Gson().toJson(sortedSheetDTO);
            out.print(jsonResponse);
            out.flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error sorting sheet range: " + e.getMessage()));
            out.flush();
        }
    }
}
