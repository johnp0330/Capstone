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
    private static final String css = "/style.css";
    private static final String js = "/js/script.js";

    /**
     * Reads and returns every line of a html file, inserting JS and CSS in the head tag (Used for full web pages).
     * @param uri String of the file URI
     * @return String of Every line of the full web page
     */
    private static String loadPage(String uri)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(uri)))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line;

            while ((line = reader.readLine()) != null && !line.contains("<head>")) // Reading page until we find the <head> tag
            {
                page.append(line);
            }

            // Adding the css and js to the page
            page.append("<head> ");
            page.append("<style>");
            page.append(loadResource(css));
            page.append("</style>");
            page.append("<script>");
            page.append(loadResource(js));
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
     * @param uri String of the file URI
     * @return String of all lines of file
     */
    private static String loadResource(String uri) {
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
        String page = INDEX_HTML;

        if ("application/x-www-form-urlencoded".equals(type)) // Requesting an item be added to inventory
        {
            String sku = request.getParameter("sku");
            String itemName = request.getParameter("itemname");
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            double price = Double.parseDouble(request.getParameter("price"));
            String description = request.getParameter("description");

            int rows = Database.addInventory(sku, itemName, quantity, price, description);

            String htmlResponse = (rows == 1 ? "Item successfully added to inventory." : "Unsuccessful.");
            PrintWriter writer = response.getWriter();

            writer.println(reloadInventory(htmlResponse));
        }
        else if (uri.contains(".html")) // Requesting web page
        {
            page = loadPage(uri);
            response.getWriter().println(page);
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
        else
        {
            response.getWriter().println(page);
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

   /*
    * The entire purpose of this method is to add a response above the
    * inventory form telling the user whether the form was successful or not.
    * It feels super redundant, but it works.
    */
    public static String reloadInventory(String htmlResponse)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream("/inventory.html")))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line;

            while ((line = reader.readLine()) != null && !line.contains("<head>")) // Reading page until we find the <head> tag
            {
                page.append(line);
            }

            // Adding the css and js to the page
            page.append("<head> ");
            page.append("<style>");
            page.append(loadResource(css));
            page.append("</style>");
            page.append("<script>");
            page.append(loadResource(js));
            page.append("</script>");

            // Continuing to read until we find the 'addInventory' form
            while ((line = reader.readLine()) != null && !line.contains("addInventory")) {
                page.append(line);
            }

            // Adding response
            page.append("<p>");
            page.append(htmlResponse);
            page.append("</p>");

            // Continuing to read the rest of the page
            while ((line = reader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } catch (final Exception exception) {
            System.out.println("Unable to load inventory");
            return getStackTrace(exception);
        }
    }
}
