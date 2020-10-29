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
     * Establishes a connection to the database.
     * @return SQL connection object
     * @throws SQLException Database connection exception
     */
    public static Connection connect() throws SQLException
    {
        return DriverManager.getConnection(connectionURL);
    }

    /**
     * Reads all data from Inventory and formats it into a JSON object.
     * @return All inventory data in a JSON object
     */
    public static JSONObject readInventory()
    {
        JSONObject jsonObject = new JSONObject();

        try (Connection conn = connect())
        {
            String sql = "SELECT * FROM dbo.Inventory ORDER BY ItemName DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            //Creating a json array
            JSONArray array = new JSONArray();
            //Inserting ResultSet data into the json object
            while(rs.next()) {
                JSONObject record = new JSONObject();
                //Inserting key-value pairs into the json object
                record.put("SKU", rs.getString("SKU"));
                record.put("ItemName", rs.getString("ItemName"));
                record.put("Quantity", rs.getInt("Quantity"));
                record.put("Price", rs.getDouble("Price"));
                record.put("Description", rs.getString("Description"));
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
     * Takes in details of an item and adds it to the inventory table in the database.
     * @param sku SKU code for the item
     * @param itemName Name of the item
     * @param price Price of the item
     * @param description Description of the item
     * @return Number of rows updated (It should be exactly one)
     */
    public static int addInventory(String sku, String itemName, int quantity, double price, String description)
    {
        int rows = 0;

        String sql = "INSERT INTO dbo.Inventory(SKU, ItemName, Quantity, Price, Description) VALUES(?,?,?,?,?)";

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1, sku);
            stmt.setString(2, itemName);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.setString(5, description);

            rows = stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Error adding to inventory.");
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Reads and returns the last record from the database and writes the current date to the database.
     * @param date Date at which the button was pressed
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
