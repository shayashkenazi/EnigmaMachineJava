package servlets;

import EnginePackage.EngineCapabilities;
import enigmaException.EnigmaException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.ServletUtils;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Scanner;

@WebServlet(name = "LoadXmlServlet", urlPatterns = {"/LoadXmlServlet"}) // TODO: Correct
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class LoadXmlServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException, FileNotFoundException{
        response.setContentType("text/plain");
        Collection<Part> parts = request.getParts();
        StringBuilder fileContent = new StringBuilder();
        try {
            for (Part curPart : parts) {
                EngineCapabilities engine = ServletUtils.getEngine(getServletContext());
                engine.createEnigmaMachineFromXMLInputStream(curPart.getInputStream(), true);
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
        }
        catch (EnigmaException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print(e.getMessage());
        }
        catch (JAXBException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private String readFromInputStream(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\Z").next();
    }
}