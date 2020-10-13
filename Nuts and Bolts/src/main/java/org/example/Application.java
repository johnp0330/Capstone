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
    private static final String INDEX_HTML = loadIndex();

    private static final String portDB = "1433";
    private static final String database = "nutsandboltsdb";
    private static final String userDB = "yellowB02";
    private static final String passDB = "kpc7w9d2JMH5XEa";

    private static final String connectionURL = "jdbc:sqlserver://" + "aa14htpmtmpc6qx.cnhuivvv6zpo.us-east-1.rds.amazonaws.com" + ":" + portDB +
            ";databaseName=" + database + ";user=" + userDB + ";password=" + passDB + ";";

    private static String loadIndex() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream("/index.html")))) {
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

        if (type == "application/x-www-form-urlencoded") {
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

            String htmlResponse = writeToDB(date);
            PrintWriter writer = response.getWriter();
            writer.println(htmlResponse);
        }
        response.getWriter().println(INDEX_HTML);
    }

    private String writeToDB(java.sql.Date date)
    {
        String htmlResponse = "<html><p>Saved to database</p></html>";

        try {
            Connection conn = DriverManager.getConnection(connectionURL);
            String sql = "INSERT INTO dbo.DateTime (Date) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, date);
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQL error");
            htmlResponse = "<html><p>Error</p></html>";
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
