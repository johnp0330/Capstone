package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;

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

    public static JSONArray readInventory()
    {
        JSONArray array = new JSONArray();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Inventory ORDER BY ItemName DESC";
            ResultSet rs = stmt.executeQuery(sql);

            //Inserting ResultSet data into the json object
            while(rs.next()) {
                JSONObject record = new JSONObject();
                //Inserting key-value pairs into the json object
                record.put("ItemName", rs.getString("ItemName"));
                record.put("SKU", rs.getString("SKU"));
                record.put("Quantity", rs.getInt("Quantity"));
                record.put("Price", rs.getDouble("Price"));
                record.put("Description", rs.getString("Description"));

                array.add(record);
            }
        } catch (SQLException e)
        {
            System.out.println("Error retrieving inventory.");
            e.printStackTrace();
        }

        System.out.println(array.toJSONString()); // TESTING

        return array;
    }

    /**
     * Takes in details of an item and adds it to the inventory table in the database.
     * @param sku SKU code for the item
     * @param itemname Name of the item
     * @param quantity Number of items in stock
     * @param price Price of the item
     * @param description Description of the item
     * @return Number of rows updated (It should be exactly one)
     */
    public static int addInventory(String sku, String itemname, int quantity, double price, String description)
    {
        int rows = 0;

        String sql = "INSERT INTO dbo.Inventory(SKU, ItemName, Quantity, Price, Description) VALUES(?,?,?,?,?)";

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1, sku);
            stmt.setString(2, itemname);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.setString(5, description);

            rows = stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Error adding item to inventory.");
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Removes item from the inventory table of the database.
     * @param sku SKU code of the item
     * @return Number of rows altered (It should be exactly one)
     */
    public static int removeInventory(String sku)
    {
        int rows = 0;

        String sql = "DELETE FROM dbo.Inventory WHERE SKU='" + sku + "';";

        try (Connection con = connect();
             Statement stmt = con.createStatement())
        {
            rows = stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            System.out.println("Error removing item from inventory.");
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Takes in details of an item and adds it to the inventory table in the database.
     * @param sku SKU code for the item
     * @param itemname Name of the item
     * @param quantity Number of items in stock
     * @param price Price of the item
     * @param description Description of the item
     * @return Number of rows updated (It should be exactly one)
     */
    public static int editInventory(String sku, String itemname, int quantity, double price, String description)
    {
        int rows = 0;

        String sql = "UPDATE dbo.Inventory SET ItemName = ?, Quantity = ?, Price = ?, Description = ? WHERE SKU = ?;";

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1, itemname);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, price);
            stmt.setString(4, description);
            stmt.setString(5, sku);

            rows = stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Error editing item in inventory.");
            e.printStackTrace();
        }

        return rows;
    }

    public static List<String> reduceInventory(String[] skus, int[] quantities)
    {
        List<String> itemsOutOfStock = new ArrayList<>();
        List<String> queries = new ArrayList<>();

        try (Connection con = connect();
             Statement stmt = con.createStatement())
        {
            for (int i = 0; i < skus.length; i++)
            {
                String sku = skus[i];
                int quantityPurchased = quantities[i];
                ResultSet rs = stmt.executeQuery("SELECT Quantity FROM Inventory WHERE SKU = " + sku + ";");
                int quantityInStock = rs.getInt("Quantity");

                if (quantityPurchased >= quantityInStock)
                {
                    queries.add("UPDATE Inventory SET Quantity = " + (quantityInStock - quantityPurchased) + " WHERE SKU = " + sku + ";");
                }
                else
                {
                    itemsOutOfStock.add(sku);
                }
            }

            if (itemsOutOfStock.isEmpty())
            {
                for (String query : queries)
                    stmt.executeQuery(query);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error editing item in inventory.");
            e.printStackTrace();
        }

        return itemsOutOfStock;
    }
}












