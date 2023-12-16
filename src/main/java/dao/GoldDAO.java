package dao;

import db.DBContext;
import model.Gold;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoldDAO {

  Connection connect = null;
  PreparedStatement ps = null;
  ResultSet result = null;

  public List<Gold> getAllGold() {
    DBContext db = DBContext.getInstance();
    List<Gold> list = new ArrayList<>();
    try {
      Connection connect = db.getConnection();
      String query = "SELECT Date_ef, Time, Product_Name, SellingPrice, BuyingPrice " +
          "FROM AGGREGATE " +
          "WHERE YEAR(Date_ex) = 2999";
      try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
        try (ResultSet result = preparedStatement.executeQuery()) {
          while (result.next()) {
            // Assuming Gold class has a constructor that accepts these parameters
            Gold gold = new Gold(
                result.getString("Date_ef"),
                result.getString("Time"),
                result.getString("Product_Name"),
                result.getDouble("SellingPrice"),
                result.getDouble("BuyingPrice")
            );
            list.add(gold);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  public static void main(String[] args) {
    GoldDAO pro = new GoldDAO();
    List<Gold> goldList = pro.getAllGold();
    for (Gold gold : goldList) {
      System.out.println(gold.toString()); // Assuming Gold class has a toString() method
    }
  }


}
