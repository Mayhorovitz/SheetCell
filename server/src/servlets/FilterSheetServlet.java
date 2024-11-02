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
import java.util.Arrays;
import java.util.List;

@WebServlet("/filterSheet")
public class FilterSheetServlet extends HttpServlet {

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
        String selectedValuesJson = request.getParameter("selectedValues");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String[] selectedValuesArray = new Gson().fromJson(selectedValuesJson, String[].class);
            List<String> selectedValues = Arrays.asList(selectedValuesArray);

            SheetDTO filteredSheetDTO = engine.filterSheetByValues(sheetName, range, column, selectedValues, null);

            String jsonResponse = new Gson().toJson(filteredSheetDTO);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error filtering sheet: " + e.getMessage()));
            out.flush();
        }
    }
}
