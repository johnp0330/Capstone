package org.example;

import javax.servlet.annotation.WebServlet;
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

    private static final String USER = "yellowB02";
    private static final String PASS = "kpc7w9d2JMH5XEa";
    private static final String PORT = "1433";
    private static final String HOST = "aarfzcittk4zbf.cnhuivvv6zpo.us-east-1.rds.amazonaws.com";
    private static final String DATABASE = "logs";

    private static final String connectionURL = "jdbc:sqlserver://" + HOST + ":" + PORT +
            ";databaseName=" + DATABASE + ";user=" + USER + ";password=" + PASS + ";";


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

        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        catch(ClassNotFoundException ex)
        {
            System.out.println("Error loading driver class");
            System.exit(1);
        }

        if (type.equals("application/x-www-form-urlencoded"))
        {
            String lastDateTime = readFromDB();
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
            java.sql.Time time = new java.sql.Time(System.currentTimeMillis());
            writeToDB(date, time);

            // Write date to webpage
            PrintWriter writer = response.getWriter();
            String htmlResponse = "<html>Last save was at " + lastDateTime + "</html>";
            writer.println(htmlResponse);
        }
    }

    private String readFromDB()
    {
        String lastDateTime = "";
        try
        {
            Connection conn = DriverManager.getConnection(connectionURL);
            String sql = "SELECT TOP 1 * FROM dbo.Activities";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            lastDateTime = rs.getString(1);
            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println("SQL error");
            System.exit(1);
        }

        return lastDateTime;
    }

    private void writeToDB(java.sql.Date date, java.sql.Time time)
    {
        try {
            Connection conn = DriverManager.getConnection(connectionURL);
            String sql = "INSERT INTO dbo.Activities VALUES (?)";
            Statement stmt = conn.createStatement();
            stmt.executeQuery(sql);
            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println("SQL error");
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
