package servlets;

import com.google.gson.Gson;
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
import java.io.PrintWriter;
@WebServlet("/uploadSheet")
@MultipartConfig
public class SheetLoaderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userName = SessionUtils.getUsername(request);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (userName == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(new Gson().toJson("User is not logged in."));
            out.flush();
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(new Gson().toJson("File is required."));
            out.flush();
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        try {
            engine.loadFile(filePart.getInputStream(), userName);
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("Sheet uploaded successfully.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // Escape JSON properly to ensure it shows text as intended
            out.write(e.getMessage());
        } finally {
            out.flush(); // Flush the stream to ensure the message is sent
            out.close(); // Close the stream
        }
    }
}
