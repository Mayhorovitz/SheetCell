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
@WebServlet("/getSheetVersion")
public class GetSheetVersionServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String version = request.getParameter("version");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int versionNumber = Integer.parseInt(version);
            SheetDTO sheetDTO = engine.getSheetDTOByVersion(sheetName, versionNumber);
            String jsonResponse = new Gson().toJson(sheetDTO);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error retrieving sheet version: " + e.getMessage()));
            out.flush();
        }
    }
}
