package servlets.sheetView;


import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/updateBackgroundColor")
public class UpdateBackgroundColorServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String sheetName = request.getParameter("sheetName");
        String cellId = request.getParameter("cellId");
        String colorHex = request.getParameter("colorHex");

        Engine engine = (Engine) getServletContext().getAttribute("engine");
        if (engine != null && cellId != null && colorHex != null) {
            engine.updateCellBackgroundColor(sheetName,cellId, colorHex);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
