package org.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class Application extends AbstractHandler
{
    private static final int PAGE_SIZE = 3000;
    private static final String INDEX_HTML = loadPage("/index.html");

    private static final String portDB = "1433";
    private static final String database = "nutsandboltsdb";
    private static final String userDB = "yellowB02";
    private static final String passDB = "kpc7w9d2JMH5XEa";

    private static final String connectionURL = "jdbc:sqlserver://" + "aa14htpmtmpc6qx.cnhuivvv6zpo.us-east-1.rds.amazonaws.com" + ":" + portDB +
            ";databaseName=" + database + ";user=" + userDB + ";password=" + passDB + ";";

    private static String loadPage(String name)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(name)))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line = null;
            // Reading page until we find the <head> tag
            while ((line = reader.readLine()) != null && !line.contains("<head>"))
            {
                page.append(line);
            }
            // Adding the css to the page
            page.append("<head> " +
                        "<style>");
            page.append(loadResource("/style.css"));
            page.append("</style>");
            // Continuing to read the rest of the page
            while ((line = reader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } catch (final Exception exception) {
            return getStackTrace(exception);
        }
    }

    private static String loadResource(String name) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(name)))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line = null;

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
        // Handle HTTP requests here.
        String type = request.getContentType();
        String page = INDEX_HTML;

        if (type == "application/x-www-form-urlencoded") {

            if (request.getParameter("date") != null) {
                java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());

                String htmlResponse = readWriteDB(date);
                PrintWriter writer = response.getWriter();
                writer.println("<html><p>" + htmlResponse + "</p></html>");
                response.getWriter().println(INDEX_HTML);
            }
        }

        if (request.getParameter("contactUsBtn") != null)
        {
            page = loadPage("/contact_us.html");
        }
        else if (request.getParameter("faq") != null)
        {
            page = loadPage("/faq.html");
        }
        else if (request.getParameter("homepage") != null)
        {
            page = loadPage("/index.html");
        }

        response.getWriter().println(page);
    }
    /**
     * Reads and returns the last record from the database and writes the current date to the database
     * @param date - Date at which the button was pressed
     * @return String containing the previous date at which the button was pressed
     */
    private static String readWriteDB(java.sql.Timestamp date)
    {
        String htmlResponse;

        try (Connection conn = DriverManager.getConnection(connectionURL);)
        {
            // Reading from DB
            String sql = "SELECT TOP 1 Date " +
                    "FROM dbo.DateTime " +
                    "ORDER BY id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            java.sql.Timestamp lastDate = rs.getTimestamp("Date");

            // Writing to DB
            sql = "INSERT INTO dbo.DateTime (Date) " +
                    "VALUES (?)";
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, date);
            stmt.executeUpdate();

            htmlResponse = "The last click was at " + lastDate.toString() + " UTC";
        } catch (SQLException e) {
            System.out.println("SQL error");
            htmlResponse = "Error sending or retrieving data";
            e.printStackTrace();
        }

        return htmlResponse;
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
