package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
    Config cof = new Config();

    String username ="root";
    String password ="";
    Connection connStaging ;
    Connection connControl;
    String link = "https://www.pnj.com.vn/blog/gia-vang/";
    public Main(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connStaging = cof.connectToDatabase("staging");
            connControl = cof.connectToDatabase("control");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    private List<GoldPrice> crawl() throws IOException {

        Document doc = Jsoup.connect(link).get();
        Element element = doc.select("span[id=time-now]").first();
        String date  = element.text().split(" ")[0];
        String time = element.text().split(" ")[1];
        List<GoldPrice> goldPrices = new ArrayList<GoldPrice>();
        GoldPrice goldPrice = new GoldPrice(date, time);
        Element table = doc.select("tbody#content-price").first();

        Elements rows = table.select("tr");
        for (Element row : rows) {
            // Get the data from each cell in the row
            Elements cells = row.select("td");
            if (cells.size() >= 3) {
                String type = cells.get(0).text();
                double buyingPrice = Double.parseDouble(cells.get(1).select("span").text().replaceAll(",", "")) ;
                double sellingPrice = Double.parseDouble(cells.get(2).select("span").text().replaceAll(",", ""));
                Gold gold= new Gold(type,buyingPrice, sellingPrice);
                goldPrice.getListPrice().add(gold);
            }
        }
        goldPrices.add(goldPrice);
        return goldPrices;
    }
    public void writeDataToCSV() throws IOException {
        if (!checkProcess()) {
            String fileName = getFileName();
            String csvFileName = "D:\\Documents\\DataWareHouse\\" + fileName;

            insertLog("Crawling", "Lấy dữ liệu từ nguồn về file", 2);

            List<GoldPrice> goldPrices = crawl();
            String newDate = goldPrices.get(0).time + " " + goldPrices.get(0).date;

            if (isNew(newDate)) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFileName), StandardCharsets.UTF_8))) {
                    // Write the header row
                    insertConfig("Crawl Data", link, "Crawling", fileName, 2);
                    writer.write("Date,Time,Type,Buying Price,Selling Price\n");

                    // Write data rows
                    for (GoldPrice goldPrice : goldPrices) {
                        String date = goldPrice.getDate();
                        String time = goldPrice.getTime();
                        for (Gold gold : goldPrice.getListPrice()) {
                            String type = gold.getType();
                            String buyingPrice = String.valueOf(gold.getBuying());
                            String sellingPrice = String.valueOf(gold.getSelling());
                            String data = date + "," + time + "," + type + "," + buyingPrice + "," + sellingPrice + "\n";
                            writer.write(data);
                        }
                    }
                }catch (Exception e) {
                    updateStatusForConfig(10);
                    insertLog("Error", "Lấy dữ liệu từ nguồn về file thất bại", 10, findIdByFileName(fileName));
                }
                updateConfigForLog(findIdByFileName(fileName));
                insertLog("Crawled", "Lấy dữ liệu từ nguồn về file thành công", 3, findIdByFileName(fileName));

                updateStatusForConfig(3);

            }
            else {
                insertLog("Error", "Dữ liệu mới hiện chưa được cập nhật", 10);

            }
        }
    }
    private String getFileName() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int year = currentDateTime.getYear();
        int month = currentDateTime.getMonthValue();
        int day = currentDateTime.getDayOfMonth();
        int hour = currentDateTime.getHour();
        int minute = currentDateTime.getMinute();
        String fileName = String.format("dw_%02d_%02d_%02d_%02d_%04d.csv", hour, minute, day, month, year);
        return fileName;
    }
    private void insertLog(String title, String description, int status){
        String sql = "Insert Into Log(Time, Title, Description,  Status) values (NOW(),?,?,?)";
        try {
            PreparedStatement statement = connControl.prepareStatement(sql);

            statement.setString(1,title);
            statement.setString(2,description);
            statement.setInt(3,status);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void insertLog(String title, String description, int status, int configId){
        String sql = "Insert Into Log(Time, Title, Description, Config_Id, Status) values (NOW(),?,?,?, ?)";
        try {
            PreparedStatement statement = connControl.prepareStatement(sql);

            statement.setString(1,title);
            statement.setString(2,description);
            statement.setInt(3,configId);
            statement.setInt(4,status);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void insertConfig(String process, String source, String title, String fileName, int status){
        String sql = "Insert Into Config(Process, Source, Username, Password, Port, Title, FileName, Status, Flag ) values (?,?, ?,?,3306,?,?,?, 'False')";
        try {
            PreparedStatement statement = connControl.prepareStatement(sql);
            statement.setString(1, process);
            statement.setString(2,source);
            statement.setString(3,username);
            statement.setString(4,password);
            statement.setString(5,title);
            statement.setString(6, fileName);
            statement.setInt(7,status);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateConfigForLog(int id){
        String sql = "Update Log set Config_Id = "+ id +" where title = ? and log.time = (SELECT MAX(log.time) FROM log) ";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            stm.setString(1, "Crawling");
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void updateStatusForConfig(int status){
        String sql = "Update Config set Status = " + status+ " where Status =2";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private int findIdByFileName(String fileName){
        String sql = "select Id from Config where FileName = ?";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            stm.setString(1, fileName);
            ResultSet rs = stm.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String getNewStatus(){
        String st= "";
        String sql ="SELECT status.status FROM status, log  WHERE log.time = (SELECT MAX(log.time) FROM log) and log.status = status.id";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while (rs.next()){
                st = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return st;
    }
    private boolean checkProcess(){
         if(getNewStatus().equals("Crawling")) return true;
        else if(getNewStatus().equals("Extracting")) return true;
         else if(getNewStatus().equals("Loading")) return true;
       else return false;
    }
    private boolean isNew(String dateUp){
        String sql = "select  DISTINCT Time, Date from Temp";
        String date = "";
        try {
            PreparedStatement stm = connStaging.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while(rs.next()) {
                date = rs.getString(1) + " " + rs.getString(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(date.equals(dateUp)) return false;
        else return true;
    }
    private List<String> getlist(){
        List<String> res = new ArrayList<>();
        String sql ="SELECT FileName FROM config";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while (rs.next()){
                res.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
    public static void main(String[] args) throws IOException {
        Main main  = new Main();
        List<String>  list = main.getlist();
//       System.out.println(main.isNew("09:43 28/11/2023"));
        main.writeDataToCSV();

    }
}
