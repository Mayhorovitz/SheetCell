package servlets;

import com.google.gson.Gson;
import dto.api.RangeDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@WebServlet("/getAllRanges")
public class GetAllRangesServlet extends HttpServlet {

    private Engine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = (Engine) getServletContext().getAttribute("engine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Collection<RangeDTO> ranges = engine.getAllRangesFromSheet(sheetName);
            String jsonResponse = new Gson().toJson(ranges);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new Gson().toJson("Error retrieving ranges: " + e.getMessage()));
            out.flush();
        }
    }
}