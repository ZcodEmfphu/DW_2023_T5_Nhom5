package controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadFromTempToDataWareHouse {
  private static final Logger logger = LoggerFactory.getLogger(LoadFromTempToDataWareHouse.class);
  private static final String LOG_INSERT_QUERY = "INSERT INTO Log (Time, Title, Description, Config_Id, Status) VALUES (?, ?, ?, ?, ?)";
  private static long startTime = System.currentTimeMillis();
  private static long duration = 1 * 30 * 1000;

  // 1. Kết nối đến cơ sở dữ liệu
  public static Connection connectToDatabase(String connectionName) {
    try {
      // 2. Tải các thuộc tính từ tệp cấu hình sử dụng tên kết nối
      Properties properties = loadProperties(connectionName);

      // 3. Sử dụng các thuộc tính để thiết lập và trả về kết nối đến cơ sở dữ liệu
      return getConnectionFromProperties(properties);
    } catch (SQLException e) {
      // Xử lý ngoại lệ nếu có lỗi khi kết nối đến cơ sở dữ liệu
      System.err.println("Error connecting to the database: " + e.getMessage());
      return null;
    }
  }

  // 2. Load File cấu hình
  public static Properties loadProperties(String connectionName) {
    // 2.1. Tạo một đối tượng Properties để lưu trữ các thuộc tính
    Properties properties = new Properties();

    try (InputStream input = LoadFromTempToDataWareHouse.class.getClassLoader()
        .getResourceAsStream("./config.properties")) {
      // 2.2. Kiểm tra xem tệp cấu hình có tồn tại hay không
      if (input == null) {
        System.err.println("Error: Unable to find config.properties");
        return properties;
      }

      // 2.3. Load các thuộc tính từ tệp cấu hình vào đối tượng Properties
      properties.load(input);

      // 2.4. Thêm các thuộc tính cụ thể cho kết nối từ tên kết nối đã cho
      properties.putAll(loadSpecificProperties(connectionName, properties));
    } catch (IOException e) {
      // Xử lý ngoại lệ nếu có lỗi khi đọc tệp cấu hình
      System.err.println("Error loading properties file: " + e.getMessage());
    }

    // Trả về đối tượng Properties đã được tải
    return properties;
  }

  // 2.4. Trích xuất các thuộc tính cụ thể dựa trên tên kết nối
  public static Properties loadSpecificProperties(String connectionName, Properties properties) {
    // Tạo một đối tượng Properties mới để lưu trữ các thuộc tính cụ thể
    Properties specificProperties = new Properties();

    // Đặt các thuộc tính cụ thể cho đối tượng specificProperties
    specificProperties.setProperty("database", properties.getProperty(connectionName + ".database"));
    specificProperties.setProperty("host", properties.getProperty(connectionName + ".host"));
    specificProperties.setProperty("port", properties.getProperty(connectionName + ".port"));
    specificProperties.setProperty("username", properties.getProperty(connectionName + ".username"));
    specificProperties.setProperty("password", properties.getProperty(connectionName + ".password"));

    // Trả về đối tượng specificProperties đã được điền thông tin
    return specificProperties;
  }

  // 3. Thiết lập kết nối đến cơ sở dữ liệu.
  public static Connection getConnectionFromProperties(Properties properties) throws SQLException {
    // 3.1. Xây dựng URL kết nối bằng cách sử dụng thông tin host, port và tên cơ sở
    // dữ liệu từ thuộc tính
    String url = "jdbc:mysql://" + properties.getProperty("host") + ":" + properties.getProperty("port") + "/"
        + properties.getProperty("database");

    // 3.2. Lấy thông tin tên đăng nhập và mật khẩu từ thuộc tính
    String username = properties.getProperty("username");
    String password = properties.getProperty("password");

    // Sử dụng DriverManager để tạo và trả về kết nối đến cơ sở dữ liệu
    return DriverManager.getConnection(url, username, password);
  }

  // Load dữ liệu từ staging sang Datawarehouse
  public static void loadDataFromTempToDataWarehouse() throws SQLException {

    // 1. Kiểm tra xem log mới nhất có Status ID là 7 không
    if (latestLogHasStatus()) {
      logInfo("Dừng quá trình tải dữ liệu");
      // 3.1 Ghi log thông báo bắt đầu quá trình tải dữ liệu
      insertLog("Error", "Tiến trình đang được khởi tạo vui lòng chờ", 1, 10);
      return;
    }

    // 3.2 Ghi log thông báo bắt đầu quá trình tải dữ liệu
    insertLog("Loading", "Data Loading TEMP to Product_fact ", 1, 7);
    logInfo("Đang tải dữ liệu....");

    // 4. Chờ trong khoảng thời gian đã định
    while (System.currentTimeMillis() - startTime < duration) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    // 5. Tải dữ liệu từ TEMP vào Product_dim
    loadDataFromTempToProductDim();
    // 6. Update dữ liệu từ staging sang product_dim
    updateDateExToCurrentDateId();

    // 7. Kết nối đến cơ sở dữ liệu staging và data warehouse
    try (Connection stagingConnection = connectToDatabase("staging");
        Connection dataWarehouseConnection = connectToDatabase("datawarehouse")) {

      // 9. Câu truy vấn SQL để chọn dữ liệu từ bảng staging kết hợp với date_dim
      String selectQuery = "SELECT date_dim.d_id, temp.Id, temp.BuyingPrice, temp.SellingPrice, CURRENT_TIME() AS Time "
          + "FROM giavang_staging.temp "
          + "JOIN giavang_datawarehouse.date_dim ON temp.Date = date_dim.full_date";

      // 10. Câu truy vấn SQL để chèn dữ liệu vào bảng data warehouse
      String insertQuery = "INSERT INTO giavang_datawarehouse.product_fact (Date_ef, Time, Date_ex, "
          + "Product_Id, BuyingPrice, SellingPrice, Status) " + "VALUES (?, ?, 7585, ?, ?, ?, 8)";

      // Thực hiện các câu truy vấn SQL
      try (PreparedStatement selectStatement = stagingConnection.prepareStatement(selectQuery);
          ResultSet resultSet = selectStatement.executeQuery();
          PreparedStatement insertStatement = dataWarehouseConnection.prepareStatement(insertQuery)) {

        // Xử lý kết quả và chèn dữ liệu vào bảng data warehouse
        while (resultSet.next()) {

          int date_ef = resultSet.getInt("d_id");
          int tempId = resultSet.getInt("Id");
          String time = resultSet.getString("Time");
          String buyingPrice = resultSet.getString("BuyingPrice");
          String sellingPrice = resultSet.getString("SellingPrice");

          insertStatement.setInt(1, date_ef);
          insertStatement.setString(2, time);
          insertStatement.setInt(3, tempId);
          insertStatement.setString(4, buyingPrice);
          insertStatement.setString(5, sellingPrice);

          insertStatement.executeUpdate();
        }

        // 10. Ghi log thông báo thành công
        logInfo("Dữ liệu được tải từ TEMP vào Product_fact thành công.");
        insertLog("Loaded", "Dữ liệu được tải từ TEMP vào Product_fact thành công.", 1, 8);
      } catch (SQLException e) {
        // Ghi log lỗi nếu có ngoại lệ trong quá trình tải dữ liệu
        logError("Lỗi khi tải dữ liệu từ TEMP vào Product_fact: " + e.getMessage(), e);
        insertLog("Load Data", "Lỗi khi tải dữ liệu từ TEMP vào Product_fact: " + e.getMessage(), 1, 9);
      }
    } catch (SQLException e) {
      // 11. Ghi log lỗi không mong muốn nếu có ngoại lệ trong quá trình kết nối cơ sở
      // dữ liệu
      logError("Lỗi không mong muốn: " + e.getMessage(), e);
      insertLog("Load Data", "Lỗi không mong muốn: " + e.getMessage(), 1, 9);
    }
  }

  // 5. Load Data từ Temp sang Table Product_dim
  public static void loadDataFromTempToProductDim() {
    try {
      // 5.1. Kiểm tra xem Product_dim có dữ liệu không
      if (hasDataInProductDim()) {
        // 5.3. Nếu có dữ liệu, cập nhật dữ liệu từ TEMP vào Product_dim
        updateDataFromTempToProductDim();
        logInfo("Dữ liệu của Product_dim đã được cập nhật");
        insertLog("Tranform", "Dữ liệu của Product_dim đã được cập nhật", 1, 6);
      } else {
        // 5.4. Nếu không có dữ liệu, tải dữ liệu từ TEMP vào Product_dim
        insertDataFromTempToProductDim();
        logInfo("Tải dữ liệu từ temp sang Product_dim thành công");
        insertLog("Loaded", "Tải dữ liệu từ temp sang Product_dim thành công", 1, 8);
      }
      logInfo("Load and update process completed successfully.");
    } catch (Exception e) {
      // Log lỗi nếu có lỗi trong quá trình tải và cập nhật dữ liệu
      logError("Error during load and update process: " + e.getMessage(), e);
    }
  }

  // 5.3. Update dữ liệu từ staging sang product_dim
  public static void updateDataFromTempToProductDim() {
    // 5.3.1. Kết nối với staging databases và datawarehouse database
    try (Connection stagingConnection = connectToDatabase("staging");
        Connection dataWarehouseConnection = connectToDatabase("datawarehouse")) {

      // Query SQL để insert hoặc update dữ liệu vào bảng Product_dim
      String insertQuery = "INSERT INTO Product_dim (id, p_id, Name, time) VALUES (?, ?, ?, ?) "
          + "ON DUPLICATE KEY UPDATE Name = VALUES(Name), time = VALUES(time)";
      // Query SQL để lấy dữ liệu từ bảng TEMP
      String selectQuery = "SELECT Id, Product FROM TEMP";

      // Thực thi câu lệnh SQL
      try (PreparedStatement selectStatement = stagingConnection.prepareStatement(selectQuery);
          PreparedStatement insertStatement = dataWarehouseConnection.prepareStatement(insertQuery)) {
        ResultSet resultSet = selectStatement.executeQuery();
        int updatedRows = 0;
        while (resultSet.next()) {
          int id = resultSet.getInt("Id");
          String productFromStaging = resultSet.getString("Product");

          // 5.3.3 Lấy tên sản phẩm từ bảng Product_dim trước khi cập nhật
          String existingName = getProductNameFromProductDim(dataWarehouseConnection, id);

          // Thiết lập các tham số cho câu lệnh insert
          insertStatement.setInt(1, id);
          insertStatement.setInt(2, id);
          insertStatement.setString(3, productFromStaging);
          // Thiết lập thời gian
          insertStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

          // Thực hiện câu lệnh insert và lấy số hàng bị ảnh hưởng
          int rowsAffected = insertStatement.executeUpdate();

          // Lấy tên đã cập nhật từ Product_dim sau khi cập nhật
          String updatedName = getProductNameFromProductDim(dataWarehouseConnection, id);

          // 5.3.4. Nếu có hàng bị cập nhật và tên thay đổi
          if (rowsAffected > 0 && !Objects.equals(existingName, updatedName)) {
            // Log thông tin về việc cập nhật hàng
            logInfo("Row with id " + id + " was updated. Name changed from '" + existingName + "' to '"
                + updatedName + "'.");
            // 5.3.7 Ghi log
            insertLog("Tranfer", existingName + " to " + updatedName, 1, 8);
            updatedRows++;
          }
        }
        logInfo(updatedRows + " rows updated from TEMP to Product_dim successfully.");
      } catch (SQLException e) {
        // Log lỗi khi cập nhật dữ liệu từ TEMP vào Product_dim
        logError("Error while updating data from TEMP to Product_dim: " + e.getMessage(), e);
      }
    } catch (SQLException e) {
      // Log lỗi khi kết nối đến cơ sở dữ liệu
      logError("Error connecting to databases: " + e.getMessage(), e);
    }
  }

  // 5.4. Load dữ liệu vào bảng product_dim
  public static void insertDataFromTempToProductDim() {
    // 5.4.1. Kết nối với staging databases và datawarehouse database
    try (Connection stagingConnection = connectToDatabase("staging");
        Connection dataWarehouseConnection = connectToDatabase("datawarehouse")) {
      // Câu lệnh query insert dữ liệu vào product_dim của datawarehouse database
      String insertQuery = "INSERT INTO Product_dim (id, p_id, Name, time) VALUES (?, ?, ?, NOW())";
      // Câu lệnh query chọn id và Product từ table temp của staging database
      String selectQuery = "SELECT Id, Product FROM TEMP";
      // Thực thi câu lệnh
      try (PreparedStatement selectStatement = stagingConnection.prepareStatement(selectQuery);
          PreparedStatement insertStatement = dataWarehouseConnection.prepareStatement(insertQuery)) {
        ResultSet resultSet = selectStatement.executeQuery();
        // Chạy vòng lặp để duyệt từng phần tử trong database
        while (resultSet.next()) {
          int id = resultSet.getInt("Id");
          String product = resultSet.getString("Product");
          insertStatement.setInt(1, id);
          insertStatement.setInt(2, id);
          insertStatement.setString(3, product);
          // 5.4.3 Thêm sản phẩm
          insertStatement.executeUpdate();
        }
        // 5.4.4 Ghi log hệ thống về việc tranform dữ liệu mới
        insertLog("Transform", "Transform dữ liệu từ Temp đến Product_dim thành công", 1, 6);
      } catch (SQLException e) {
        // Ghi log hệ thống về việc tranform dữ liệu lỗi
        insertLog("Transform", "Transform dữ liệu từ Temp đến Product_dim thất bại", 1, 10);
      }
    } catch (SQLException e) {
      // Ghi log hệ thống về việc kết nối với database thất bại
      logError("Error while connect to Datawarehouse or Staging" + e.getMessage(), e);
    }
  }

  // 5.3.4. Kiểm tra sản phẩm tồn tại trong table
  public static boolean hasDataInProductDim() {
    // Kiểm tra kết nối với datawarehouse database
    try (Connection dataWarehouseConnection = connectToDatabase("datawarehouse")) {
      // Câu lệnh đếm số sản phẩm trong table product_dim của datawareehouse database
      String countQuery = "SELECT COUNT(*) FROM Product_dim";
      // Thực thi câu lệnh
      try (PreparedStatement countStatement = dataWarehouseConnection.prepareStatement(countQuery)) {
        ResultSet resultSet = countStatement.executeQuery();
        // Nếu kết quả phẩn tử đầu tiên
        if (resultSet.next()) {
          int count = resultSet.getInt(1);// Có tồn tại
          return count > 0;// Trả về lớn hơn 0
        }
      }
    } catch (SQLException e) {
      // Thông báo lỗi log hệ thống
      logError("Error checking data in Product_dim: " + e.getMessage(), e);
    }
    return false;
  }

  // 5.3.3. Kiểm tra tương thích dữ liệu
  private static String getProductNameFromProductDim(Connection connection, int id) throws SQLException {
    // Câu truy vấn SQL để lấy tên từ bảng Product_dim dựa trên id
    String selectQuery = "SELECT Name FROM Product_dim WHERE id = ?";

    // Sử dụng try-with-resources để quản lý PreparedStatement và tự động đóng tài
    // nguyên
    try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
      // Thiết lập tham số trong PreparedStatement
      selectStatement.setInt(1, id);

      // Thực hiện truy vấn và nhận ResultSet chứa kết quả
      ResultSet resultSet = selectStatement.executeQuery();

      // Kiểm tra xem có kết quả nào trong ResultSet không
      if (resultSet.next()) {
        // Trả về tên sản phẩm nếu có kết quả
        return resultSet.getString("Name");
      } else {
        // Trả về null nếu không có kết quả
        return null;
      }
    }
  }

  // 6. Cập nhật thời gian
  public static void updateDateExToCurrentDateId() {
    // 6.1. Kết nối đến Data Mart
    try (Connection dmConnection = connectToDatabase("datawarehouse")) {
      dmConnection.setAutoCommit(false);

      // 6.2. Lấy id cho ngày hiện tại từ Date_dim
      String currentDateIdQuery = "SELECT d_id FROM Date_dim WHERE full_date = CURRENT_DATE";

      // Thực hiện truy vấn
      try (PreparedStatement currentDateIdStatement = dmConnection.prepareStatement(currentDateIdQuery);
          ResultSet currentDateIdResult = currentDateIdStatement.executeQuery()) {

        if (currentDateIdResult.next()) {
          // Lấy id cho ngày hiện tại
          int currentDateId = currentDateIdResult.getInt("d_id");

          // 6.3 Cập nhật Date_ex thành date_id của ngày hiện tại
          String updateQuery = "UPDATE Product_fact SET Date_ex = ? WHERE Time = (SELECT MAX(Time) FROM Product_fact)";

          // Thực hiện cập nhật
          try (PreparedStatement updateStatement = dmConnection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, currentDateId);
            updateStatement.executeUpdate();
            dmConnection.commit();
          }
        }
      }

    } catch (SQLException e) {
      // Xử lý ngoại lệ và có thể quay lại giao dịch
      e.printStackTrace();
    } finally {
      // Bật chế độ autocommit cho kết nối Data Mart và đóng kết nối
      try (Connection dmConnection = connectToDatabase("datawarehouse")) {
        dmConnection.setAutoCommit(true);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void insertLog(String title, String description, int configId, int status) {
    try (Connection controlConnection = getControlConnection();
        PreparedStatement insertLogStatement = controlConnection.prepareStatement(LOG_INSERT_QUERY)) {

      insertLogStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
      insertLogStatement.setString(2, title);
      insertLogStatement.setString(3, description);
      insertLogStatement.setInt(4, configId);
      insertLogStatement.setInt(5, status);

      insertLogStatement.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting log: " + e.getMessage());
    }
  }

  public static Connection getControlConnection() throws SQLException {
    return connectToDatabase("control");
  }

  public static void logInfo(String message) {
    logger.info(message);
  }

  public static void logError(String message, Throwable throwable) {
    logger.error(message, throwable);
  }

  public static boolean latestLogHasStatus() {
    try (Connection dataWarehouseConnection = connectToDatabase("control")) {
      String query = "SELECT l.Status FROM Log l ORDER BY l.Time DESC LIMIT 1";
      try (PreparedStatement preparedStatement = dataWarehouseConnection.prepareStatement(query);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          int latestStatus = resultSet.getInt("Status");
          logInfo("Latest Log Status: " + latestStatus);

          return latestStatus == 7 || latestStatus == 10;
        } else {
          logInfo("No log entries found.");
          return false;
        }
      }
    } catch (SQLException e) {
      logError("Error while checking latest log status: " + e.getMessage(), e);
      return false;
    }
  }
  public static void main(String[] args) throws SQLException {
    loadDataFromTempToDataWarehouse();
  }

}