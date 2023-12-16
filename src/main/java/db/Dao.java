package db;

import model.Gold;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dao {
   static Connection conn;
    public static List<Gold> getNewData() {
        String username = "root";
        String password = "";
        String url ="jdbc:mysql://localhost:3306/giavang_datamart";
        List<Gold> res = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            conn = DriverManager.getConnection(url, username, password);


        String sql = "SELECT time,Date_ef, Product_Name, BuyingPrice, SellingPrice " +
                "from aggregate where time = (select max(time) " +
                "from aggregate where Date_ef = (SELECT MAX(Date_ef) from aggregate)) and Date_ef = (SELECT MAX(Date_ef) from aggregate)";
        PreparedStatement stm = conn.prepareStatement(sql);
        ResultSet rs = stm.executeQuery();
        while (rs.next()){
            res.add(new Gold(rs.getString(1), rs.getString(2),rs.getString(3), rs.getDouble(4), rs.getDouble(5)));

        }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        Dao d = new Dao();
//        System.out.println(getNewData().get(0));
//    }
}
