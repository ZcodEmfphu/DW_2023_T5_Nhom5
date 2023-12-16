package model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Config {


  // 1. Kết nối đến cơ sở dữ liệu
  public Connection connectToDatabase(String connectionName) {
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
  public Properties loadProperties(String connectionName) {
    // 2.1. Tạo một đối tượng Properties để lưu trữ các thuộc tính
    Properties properties = new Properties();

    try (InputStream input = Config.class.getClassLoader().getResourceAsStream("./config.properties")) {
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
  public Properties loadSpecificProperties(String connectionName, Properties properties) {
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
  public Connection getConnectionFromProperties(Properties properties) throws SQLException {
    //3.1. Xây dựng URL kết nối bằng cách sử dụng thông tin host, port và tên cơ sở dữ liệu từ thuộc tính
    String url = "jdbc:mysql://" + properties.getProperty("host") + ":" + properties.getProperty("port")
        + "/" + properties.getProperty("database");

    // 3.2. Lấy thông tin tên đăng nhập và mật khẩu từ thuộc tính
    String username = properties.getProperty("username");
    String password = properties.getProperty("password");

    // Sử dụng DriverManager để tạo và trả về kết nối đến cơ sở dữ liệu
    return DriverManager.getConnection(url, username, password);
  }



  // 4. Tải dữ liệu cấu hình từ các nguồn khác nhau và lưu vào bảng Config
  public void loadDataFromPropertiesToConfig() {
    // 1. Kết nối đến cơ sở dữ liệu "control"
    try (Connection conn = connectToDatabase("control")) {
      //5. Tải toàn bộ dữ liệu cấu hình vào cơ sở dữ liệu "control"
      loadAllConfigData(conn);
    } catch (SQLException e) {
      // Xử lý ngoại lệ nếu có lỗi khi kết nối đến cơ sở dữ liệu
      System.err.println("Error: " + e.getMessage());
    }
  }


  // 5. Tải toàn bộ dữ liệu cấu hình vào cơ sở dữ liệu
  public static void loadAllConfigData(Connection connection) throws SQLException {
    //5.1. Tải dữ liệu cấu hình cho môi trường "staging"
    loadConfigData(connection, "staging");

    //5.2. Tải dữ liệu cấu hình cho môi trường "datawarehouse"
    loadConfigData(connection, "datawarehouse");

    //5.3. Tải dữ liệu cấu hình cho môi trường "datamart"
    loadConfigData(connection, "datamart");

    //5.4. Tải dữ liệu cấu hình cho môi trường "control"
    loadConfigData(connection, "control");

    // In thông báo khi dữ liệu đã được tải thành công
    System.out.println("Load dữ liệu thành công !");
  }

  // 6. Tải dữ liệu cấu hình cho một kết nối
  public static void loadConfigData(Connection connection, String connectionName) throws SQLException {
    //6.1. Tạo một đối tượng Config và tải các thuộc tính từ tệp cấu hình
    Properties properties = new Config().loadProperties(connectionName);

    //6.2. Kiểm tra xem quy trình đã tồn tại trong bảng Config hay chưa
    if (processExists(connection, properties.getProperty("database"))) {
      return;  // Nếu tồn tại, thoát phương thức mà không làm gì cả
    }

    // Tạo câu lệnh SQL để chèn dữ liệu vào bảng Config
    String sql = "INSERT INTO Config (Source, Username, Password, Port, Status) VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      // Đặt các giá trị cho các tham số của câu lệnh SQL
      preparedStatement.setString(1, properties.getProperty("database"));
      preparedStatement.setString(2, properties.getProperty("username"));
      preparedStatement.setString(3, properties.getProperty("password"));
      preparedStatement.setInt(4, Integer.parseInt(properties.getProperty("port")));

      // Đặt giá trị cho tham số thứ 5 (Status)
      preparedStatement.setInt(5, 1);

      // Thực hiện câu lệnh SQL để chèn dữ liệu
      preparedStatement.executeUpdate();
    }
  }

  // 6. Cập nhật dữ liệu cấu hình
  public void updateConfigData(Connection connection, String newProcess, String connectionName, String newTitle, String newDest, int newStatus) throws SQLException {

    // Tải các thuộc tính từ tệp cấu hình sử dụng tên kết nối
    Properties properties = new Config().loadProperties(connectionName);

    // Kiểm tra xem quy trình đã tồn tại trong bảng Config hay chưa
    if (!processExists(connection, properties.getProperty("database"))) {
      return;  // Nếu không tồn tại, thoát phương thức mà không làm gì cả
    }

    // Tạo câu lệnh SQL để cập nhật dữ liệu trong bảng Config
    String sql = "UPDATE Config SET Process = ?, Title = ?, Dest = ?, Status = ? WHERE Source = ?";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      // Đặt các giá trị cho các tham số của câu lệnh SQL
      preparedStatement.setString(1, newProcess);
      preparedStatement.setString(2, newTitle);
      preparedStatement.setString(3, newDest);
      preparedStatement.setInt(4, newStatus);
      preparedStatement.setString(5, properties.getProperty("database"));

      // Thực hiện câu lệnh SQL để cập nhật dữ liệu
      int rowsAffected = preparedStatement.executeUpdate();

      // Kiểm tra số dòng bị ảnh hưởng để xác định liệu có dữ liệu nào được cập nhật hay không
      if (rowsAffected > 0) {
        System.out.println("Data updated for Process " + properties.getProperty("database"));
      } else {
        System.out.println(
            "No matching data found for Process " + properties.getProperty("database"));
      }
    }
  }

  // 7. Kiểm tra xem một quy trình đã tồn tại trong bảng Config hay chưa
  private static boolean processExists(Connection connection, String processName) throws SQLException {
    // Câu lệnh SQL để đếm số lượng dòng có Source (tên quy trình) trùng với giá trị được cung cấp
    String sql = "SELECT COUNT(*) FROM Config WHERE Source = ?";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      // Đặt giá trị cho tham số trong câu lệnh SQL
      preparedStatement.setString(1, processName);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        // Kiểm tra xem có dòng nào trong kết quả không và giá trị đầu tiên có lớn hơn 0 hay không
        return resultSet.next() && resultSet.getInt(1) > 0;
      }
    }
  }

}
