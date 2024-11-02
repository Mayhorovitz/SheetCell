package servlets;

import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/uploadSheet")
@MultipartConfig
public class SheetLoaderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userName = SessionUtils.getUsername(request);
        if (userName == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User is not logged in.");
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("File is required.");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        try {
            engine.loadFile(filePart.getInputStream(), userName);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Sheet uploaded successfully.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to upload the sheet: " + e.getMessage());
        }
    }
}
