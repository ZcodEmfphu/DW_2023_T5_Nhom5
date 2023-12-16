package view;

import controller.LoadFromDataWareHousetoDataMart;
import controller.LoadFromTempToDataWareHouse;
import model.Config;

import java.sql.SQLException;

public class Main {

  private final static LoadFromTempToDataWareHouse l1 = new LoadFromTempToDataWareHouse();
  private final static LoadFromDataWareHousetoDataMart l2 = new LoadFromDataWareHousetoDataMart();
  private static final Config cof = new Config();

  public static void main(String[] args) throws SQLException {
//        cof.loadDataFromPropertiesToConfig();
        l1.loadDataFromTempToDataWarehouse();
//        l2.LoadDatawarehouseToDataMart();

  }

}
