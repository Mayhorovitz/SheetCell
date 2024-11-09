package servlets.sheetsManagment;

import com.google.gson.Gson;
import dto.impl.SheetSummaryDTO;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@WebServlet("/availableSheets")
public class AvailableSheetsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userName = SessionUtils.getUsername(request);


        Engine engine = ServletUtils.getEngine(getServletContext());
        Collection<SheetSummaryDTO> availableSheets = engine.getAllSheetsSummary(userName);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(availableSheets));
        out.flush();
    }
}
