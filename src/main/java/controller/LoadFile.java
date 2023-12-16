package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;

public class LoadFile {
    String jdbcURLStaging = "jdbc:mysql://localhost:3306/giavang_staging";
    String jdbcURLControl = "jdbc:mysql://localhost:3306/giavang_control";
    String username = "root";
    String password = "";
    int batchSize = 20;
    Connection connStaging;
    Connection connControl;
    public void loadData(String csvFilePath) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 1. kết nối database Control và database staging
        connControl = DriverManager.getConnection(jdbcURLControl, username, password);
        connStaging = DriverManager.getConnection(jdbcURLStaging, username, password);
        connStaging.setAutoCommit(false);
        // 2.checkProcess is running(Crawling, Extracting, Loading)?
        if (!checkProcess()) {
            File file = new File(csvFilePath);
            BufferedReader lineReader = null;
            try {
                lineReader = new BufferedReader(new FileReader(csvFilePath));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // 3. insert table Log
            insertLog("Load Data To Staging", "Lấy dữ liệu từ file csv xuống staging", 4, findIdByFileName(file.getName()));
            // 4.Truncate bảng temp
            String truncateSQL = "TRUNCATE TABLE temp";
            PreparedStatement truncateStatement = connStaging.prepareStatement(truncateSQL);
            truncateStatement.executeUpdate();
            // 5. Write command sql to insert data to temp
            String sql = "INSERT INTO temp (Date, Time, Product, BuyingPrice, SellingPrice) VALUES (?, ?, ?, ?, ?)";

            try {
                // 6. Run sql
                PreparedStatement statement = connStaging.prepareStatement(sql);
                String lineText = null;
                int count = 0;
                try {
                    // 8. Read line of data
                    lineReader.readLine();
                    while ((lineText = lineReader.readLine()) != null) {
                        String[] data = lineText.split(",");
                        String Date = data[0];
                        String Time = data[1];
                        String Type = data[2];
                        String BuyingPrice = data[3];
                        String SellingPrice = data[4];

                        statement.setString(1, Date);
                        statement.setString(2, Time);
                        statement.setString(3, Type);
                        statement.setString(4, BuyingPrice);
                        statement.setString(5, SellingPrice);

                        // 10. Insert sql with data to batch
                        statement.addBatch();
                        if (count % batchSize == 0) {
                            statement.executeBatch();
                        }
                    }
                    lineReader.close();
                    statement.executeBatch();
                    // 11. Insert table Log
                    insertLog("Load data to staging", "Dữ liệu đã được truyền", 5, findIdByFileName(file.getName()));

                } catch (IOException e) {
                    // 9. insert table Log
                    insertLog("Error", "Đã xảy ra lỗi: " + e.getMessage(), 10, findIdByFileName(file.getName()));
                    throw new RuntimeException(e);
                }
                connStaging.commit();
                connStaging.close();
            } catch (SQLException e) {
                // 7. insert table Log
                insertLog("Error", "Lỗi load dữ liệu", 10, findIdByFileName(file.getName()));
                throw new RuntimeException(e);
            }
            String targetDirectory = "D:\\DataWarehouse\\Archive";
            moveFileToAnotherDirectory(csvFilePath, targetDirectory);
        }
    }

    private void insertLog(String title, String description, int status, int configId) {
        String sql = "Insert Into Log(Time, Title, Description, Config_Id, Status) values (NOW(),?,?,?, ?)";
        try {
            PreparedStatement statement = connControl.prepareStatement(sql);
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setInt(3, configId);
            statement.setInt(4, status);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkProcess() {
        if (getNewStatus().equals("Crawling")) return true;
        else if (getNewStatus().equals("Extracting")) return true;
        else if (getNewStatus().equals("Loading")) return true;
        else return false;
    }

    private String getNewStatus() {
        String st = "";
        String sql = "SELECT status.status FROM status, log  WHERE log.time = (SELECT MAX(log.time) FROM log) and log.status = status.id";
        try {
            PreparedStatement stm = connControl.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                st = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return st;
    }

    private int findIdByFileName(String fileName) {
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

    // 12. hàm di chuyển file csv sang thư mục khác
    private void moveFileToAnotherDirectory(String csvFilePath, String targetDirectory) {
        File sourceFile = new File(csvFilePath);
        File targetDirectoryFile = new File(targetDirectory);
        // Tạo thư mục đích nếu nó chưa tồn tại
        if (!targetDirectoryFile.exists()) {
            if (!targetDirectoryFile.mkdirs()) {
                throw new RuntimeException("Không thể tạo thư mục đích: " + targetDirectory);
            }
        }

        Path sourcePath = sourceFile.toPath();
        Path targetPath = Paths.get(targetDirectory, sourcePath.getFileName().toString());

        try {
            // di chuyển file
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Không thể di chuyển file: " + e.getMessage(), e);
        }
    }
    private void insertConfig(String process, String source, String title, String fileName, int status) throws SQLException {
        connControl = DriverManager.getConnection(jdbcURLControl, username, password);
        String sql = "Insert Into Config(Process, Source, Username, Password, Port, Title, FileName, Status, Flag ) values (?,?, ?,?,3306,?,?,?, 'False')";
        try {
            PreparedStatement statement = connControl.prepareStatement(sql);
            statement.setString(1, process);
            statement.setString(2,source);
            statement.setString(3,"root");
            statement.setString(4,"");
            statement.setString(5,title);
            statement.setString(6, fileName);
            statement.setInt(7,status);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws SQLException {
        LoadFile loadFile = new LoadFile();
        File directory = new File("D:\\Documents\\DataWarehouse");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        loadFile.loadData(file.getPath());
                        System.out.println(file.getPath());
                    }
                }
            }
        }

    }
}