import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;




public class DatabaseManager {

    private static final String URL= "jdbc:postgresql://localhost:5432/private_messaging";
    private static final String USER = "yashpatil";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, "");
    }  

    public static Long findUserIdByUsername(String username) {

    String sql = "SELECT user_id FROM users WHERE LOWER(username) = LOWER(?)";

    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                return result.getLong("user_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Long createUser(String username) {

        String sql = "INSERT INTO users (username) VALUES (?) RETURNING user_id";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                return result.getLong("user_id");
                }
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
