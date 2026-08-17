import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL =
        "jdbc:postgresql://localhost:5432/private_messaging";

    private static final String USER = "yashpatil";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, "");
    }

    public static Long findUserIdByUsername(String username) {

        String sql =
            "SELECT user_id FROM users WHERE LOWER(username) = LOWER(?)";

        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

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

        String sql =
            "INSERT INTO users (username) VALUES (?) RETURNING user_id";

        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

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

    public static Long findRoomIdByName(String roomName) {

        String sql =
            "SELECT room_id FROM rooms WHERE LOWER(room_name) = LOWER(?)";

        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, roomName);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getLong("room_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Long createRoom(String roomName, long ownerId) {

        String sql =
            "INSERT INTO rooms (room_name, owner_id) VALUES (?, ?) RETURNING room_id";

        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, roomName);
            statement.setLong(2, ownerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getLong("room_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean deleteRoom(long roomId) {

        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, roomId);

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}