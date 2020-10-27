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
    private static final String INDEX_HTML = loadPage("/index.html");

    /**
     * Reads and returns every line of a html file, inserting JS and CSS in the head tag (Used for full web pages).
     * @param file String of the file URI
     * @return String of Every line of the full web page
     */
    private static String loadPage(String file)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(file)))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line;

            while ((line = reader.readLine()) != null && !line.contains("<head>")) // Reading page until we find the <head> tag
            {
                page.append(line);
            }

            // Adding the css and js to the page
            page.append("<head> ");
            page.append("<style>");
            page.append(loadResource("/style.css"));
            page.append("</style>");
            page.append("<script>");
            page.append(loadResource("/script.js"));
            page.append("</script>");

            // Continuing to read the rest of the page
            while ((line = reader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } catch (final Exception exception) {
            return getStackTrace(exception);
        }
    }

    /**
     * Reads and returns every line of a file (Used for JS and CSS files).
     * @param file String of the file URI
     * @return String of all lines of file
     */
    private static String loadResource(String file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(file)))) {
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
        String page = INDEX_HTML;

        if (type == "application/x-www-form-urlencoded") // POST request
        {
            if (request.getParameter("date") != null) {
                java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());

                String htmlResponse = Database.readWriteDB(date);

                PrintWriter writer = response.getWriter();

                writer.println("<html><p>" + htmlResponse + "</p></html>");
            }
        }
        else if (uri.contains(".html")) // Requesting web page
        {
            page = loadPage(uri);
        }
        else if (uri.contains(".png") || uri.contains(".jpg")) // Requesting image
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

        response.getWriter().println(page);
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
