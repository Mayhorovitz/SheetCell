package servlets.sheetView;

import engine.api.Engine;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;


import java.io.IOException;

@WebServlet("/getLatestVersion")
public class GetLatestVersionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());

        String sheetName = request.getParameter("sheetName");
        int latestVersion = engine.getLatestVersion(sheetName);

        response.setContentType("text/plain");
        response.getWriter().write(String.valueOf(latestVersion));
    }
}