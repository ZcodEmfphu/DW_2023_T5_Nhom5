package view;

import model.Config;

public class Main {
   // private static LoadFromTempToDataWareHouse l1 = new LoadFromTempToDataWareHouse();
   // private static LoadFromDataWareHousetoDataMart l2 = new LoadFromDataWareHousetoDataMart();
    private static Config cof = new Config();

    public static void main(String[] args) {

        cof.loadDataFromPropertiesToConfig();
    }

}
