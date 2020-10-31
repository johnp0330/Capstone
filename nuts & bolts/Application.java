package org.example;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class Application extends AbstractHandler
{
    private static final int PAGE_SIZE = 3000;
    private static final String INDEX_HTML = loadFile("/index.html");

    /**
     * Reads and returns every line of a file (html, js, css).
     * @param uri String of the file URI
     * @return String Every line of the file
     */
    private static String loadFile(String uri)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(uri)))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line;

            while ((line = reader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } catch (final Exception exception) {
            return getStackTrace(exception);
        }
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);

        return stringWriter.getBuffer().toString();
    }

    private static int getPort() {
        return Integer.parseInt(System.getenv().get("PORT"));
    }

    private void handleHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getContentType();
        String uri = request.getRequestURI();

        if ("application/x-www-form-urlencoded".equals(type)) // POST request
        {
            String sku = request.getParameter("sku");
            String itemname = request.getParameter("itemname");
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            double price = Double.parseDouble(request.getParameter("price"));
            String description = request.getParameter("description");

            int rows = Database.addInventory(sku, itemname, quantity, price, description);

            if (rows == 1)
            {
                System.out.println("Item added to inventory!");
            }
            else
            {
                System.out.println("Failed to add item to inventory.");
            }
        }
        else if (uri.endsWith(".html")) // Requesting web page
        {
            response.getWriter().println(loadFile(uri));
        }
        else if (uri.endsWith(".png") || uri.endsWith(".jpg")) // Requesting image
        {
            String baseUrl = "src/main/resources";
            String mime = uri.contains(".png") ? "image/png" : "image/jpeg";

            response.setContentType(mime);
            File file = new File(baseUrl + uri);
            response.setContentLength((int)file.length());

            FileInputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();

            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
            in.close();
            out.close();
        }
        else if (uri.endsWith(".css")) // Requesting CSS stylesheet
        {
            response.setContentType("text/css;charset=utf-8");
            response.getWriter().println(loadFile(uri));
        }
        else if (uri.endsWith(".js")) // Requesting JavaScript
        {
            response.setContentType("application/javascript;charset=utf-8");
            response.getWriter().println(loadFile(uri));
        }
        else
        {
            response.getWriter().println(INDEX_HTML);
        }
    }

    private void handleCronTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handle WorkerTier tasks here.
        response.getWriter().println("Process Task Here.");
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String pathInfo = request.getPathInfo();
        if (pathInfo.equalsIgnoreCase("/crontask")) {
            handleCronTask(request, response);
        } else {
            handleHttpRequest(request, response);
        }
    }

    public static void main(String[] args) throws Exception
    {
        Server server = new Server(getPort());
        server.setHandler(new Application());
        server.start();
        server.join();
    }
}
