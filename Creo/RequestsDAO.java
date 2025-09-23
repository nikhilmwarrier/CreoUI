import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestsDAO {
    public int insert(Request request) {
        String query = "INSERT INTO Requests(Method, URL, Headers, Body) VALUES (?, ?, ?, ?);";
        int generatedId = -1;
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, request.getMethod());
            p.setString(2, request.getUrl());
            p.setString(3, request.getHeaders());
            p.setString(4, request.getBody());
            p.executeUpdate();
            try (ResultSet generatedKeys = p.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public List<Request> GetAll() {
        List<Request> requests = new ArrayList<>();
        String query = "SELECT * FROM Requests ORDER BY Timestamp DESC;";
        try (Connection con = DBHandle.connect();
             Statement s = con.createStatement();
             ResultSet r = s.executeQuery(query)) {
            while (r.next()) {
                requests.add(new Request(
                        r.getInt("ID"),
                        r.getString("Method"),
                        r.getString("URL"),
                        r.getString("Headers"),
                        r.getString("Body"),
                        r.getString("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public void Delete(int requestId) throws SQLException {
        String query = "DELETE FROM Requests WHERE ID = ?;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, requestId);
            int rowsAffected = p.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No request found with ID: " + requestId);
            }
        }
    }

    public void DeleteAll() throws SQLException {
        String query = "DELETE FROM Requests;";
        try (Connection con = DBHandle.connect();
             Statement s = con.createStatement()) {
            s.executeUpdate(query);
        }
    }

    public Request FindById(int requestId) {
        String query = "SELECT * FROM Requests WHERE ID = ?;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, requestId);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return new Request(
                        r.getInt("ID"),
                        r.getString("Method"),
                        r.getString("URL"),
                        r.getString("Headers"),
                        r.getString("Body"),
                        r.getString("Timestamp")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCount() {
        String query = "SELECT COUNT(*) as count FROM Requests;";
        try (Connection con = DBHandle.connect();
             Statement s = con.createStatement();
             ResultSet r = s.executeQuery(query)) {
            if (r.next()) {
                return r.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}