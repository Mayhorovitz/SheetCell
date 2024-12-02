package servlets.sheetView;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/singleDynamicAnalysis")
public class SingleDynamicAnalysisServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String cellId = request.getParameter("cellId");
        String newValue = request.getParameter("newValue");

        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            SheetDTO temporarySheetDTO = engine.performSingleDynamicAnalysis(sheetName, cellId, newValue);
            String sheetJson = new Gson().toJson(temporarySheetDTO);
            response.setContentType("application/json");
            response.getWriter().write(sheetJson);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}
