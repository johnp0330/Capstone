package org.example;

import java.sql.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Database {
    private static final String port = "1433";
    private static final String database = "nutsandboltsdb";
    private static final String user = "yellowB02";
    private static final String pass = "kpc7w9d2JMH5XEa";

    private static final String connectionURL = "jdbc:sqlserver://" + "aa14htpmtmpc6qx.cnhuivvv6zpo.us-east-1.rds.amazonaws.com" + ":" + port +
            ";databaseName=" + database + ";user=" + user + ";password=" + pass + ";";

    /**
     * Establishes a connection to the database
     * @return Connection - A SQL connection object
     * @throws SQLException - Database connection exception
     */
    public static Connection connect() throws SQLException
    {
        return DriverManager.getConnection(connectionURL);
    }

    /**
     * Reads all data from Inventory and formats it into a JSON object
     * @return jsonObject - All inventory data in JSON object format
     */
    public static JSONObject readInventory()
    {
        JSONObject jsonObject = new JSONObject();

        try (Connection conn = connect())
        {
            String sql = "SELECT * FROM dbo.Inventory ORDER BY Name DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            //Creating a json array
            JSONArray array = new JSONArray();
            //Inserting ResultSet data into the json object
            while(rs.next()) {
                JSONObject record = new JSONObject();
                //Inserting key-value pairs into the json object
                String thing = rs.getDate(1).toString();
                record.put("Model", rs.getInt("Model"));
                record.put("Name", rs.getString("Name"));
                record.put("Price", rs.getString("Price"));
                record.put("Description", rs.getDate("Description"));
                array.add(record);
            }
            jsonObject.put("Inventory", array);
        } catch (SQLException e)
        {
            System.out.println("Error retrieving inventory.");
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * Reads and returns the last record from the database and writes the current date to the database
     * @param date - Date at which the button was pressed
     * @return String containing the previous date at which the button was pressed
     */
    public static String readWriteDB(java.sql.Timestamp date)
    {
        String htmlResponse;

        try (Connection conn = connect())
        {
            // Reading from DB
            String sql = "SELECT TOP 1 Date FROM dbo.DateTime ORDER BY id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            java.sql.Timestamp lastDate = rs.getTimestamp("Date");

            // Writing to DB
            sql = "INSERT INTO dbo.DateTime (Date) VALUES (?)";
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
}
