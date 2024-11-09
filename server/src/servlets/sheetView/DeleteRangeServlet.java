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

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            engine.deleteRangeFromSheet(sheetName, rangeName);
            SheetDTO updatedSheetDTO = engine.getCurrentSheetDTO(sheetName);
            String jsonResponse = new Gson().toJson(updatedSheetDTO);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();  // To log the error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("error:" + e.getMessage());
            out.flush();
        }
    }
}
