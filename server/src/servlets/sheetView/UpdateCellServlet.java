package servlets.sheetView;

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

@WebServlet("/updateCell")
public class UpdateCellServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String cellId = request.getParameter("cellId");
        String newValue = request.getParameter("newValue");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Engine engine = (Engine) getServletContext().getAttribute("engine");
        try {
            engine.updateCell(sheetName, cellId, newValue);
            SheetDTO updatedSheetDTO = engine.getCurrentSheetDTO(sheetName);
            String jsonResponse = new Gson().toJson(updatedSheetDTO);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error updating cell: " + e.getMessage()));
            out.flush();
        }
    }
}
