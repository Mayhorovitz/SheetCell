package servlets;

import com.google.gson.Gson;
import dto.api.CellDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/getCellInfo")
public class GetCellInfoServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String cellId = request.getParameter("cellId");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            CellDTO cellDTO = engine.getCellInfo(sheetName, cellId);
            String jsonResponse = new Gson().toJson(cellDTO);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error retrieving cell info: " + e.getMessage()));
            out.flush();
        }
    }
}
