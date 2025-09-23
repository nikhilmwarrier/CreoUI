import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResponsesDAO {
    public void insert(Response response) {
        String query = "INSERT INTO Responses(Request_ID, Status_Code, Headers, Body, Content_Type) VALUES(?, ?, ?, ?, ?);";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, response.getRequestID());
            p.setInt(2, response.getStatusCode());
            p.setString(3, response.getHeaders());
            p.setString(4, response.getBody());
            p.setString(5, response.getContentType());
            p.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Response FindByRequestID(int requestId) {
        String query = "SELECT * FROM Responses WHERE Request_ID = ?;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, requestId);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return new Response(
                        r.getInt("ID"),
                        r.getInt("Request_ID"),
                        r.getInt("Status_Code"),
                        r.getString("Headers"),
                        r.getString("Body"),
                        r.getString("Content_Type"),
                        r.getString("Timestamp")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void DeleteByRequestID(int requestId) throws SQLException {
        String query = "DELETE FROM Responses WHERE Request_ID = ?;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, requestId);
            p.executeUpdate(); // Don't throw error if no rows affected - response might not exist
        }
    }

    public void DeleteAll() throws SQLException {
        String query = "DELETE FROM Responses;";
        try (Connection con = DBHandle.connect();
             Statement s = con.createStatement()) {
            s.executeUpdate(query);
        }
    }

    public void Delete(int responseId) throws SQLException {
        String query = "DELETE FROM Responses WHERE ID = ?;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, responseId);
            int rowsAffected = p.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No response found with ID: " + responseId);
            }
        }
    }

    public List<Response> GetAll() {
        List<Response> responses = new ArrayList<>();
        String query = "SELECT * FROM Responses ORDER BY Timestamp DESC;";
        try (Connection con = DBHandle.connect();
             Statement s = con.createStatement();
             ResultSet r = s.executeQuery(query)) {
            while (r.next()) {
                responses.add(new Response(
                        r.getInt("ID"),
                        r.getInt("Request_ID"),
                        r.getInt("Status_Code"),
                        r.getString("Headers"),
                        r.getString("Body"),
                        r.getString("Content_Type"),
                        r.getString("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responses;
    }

    public int getCount() {
        String query = "SELECT COUNT(*) as count FROM Responses;";
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

    public List<Response> FindByStatusCode(int statusCode) {
        List<Response> responses = new ArrayList<>();
        String query = "SELECT * FROM Responses WHERE Status_Code = ? ORDER BY Timestamp DESC;";
        try (Connection con = DBHandle.connect();
             PreparedStatement p = con.prepareStatement(query)) {
            p.setInt(1, statusCode);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                responses.add(new Response(
                        r.getInt("ID"),
                        r.getInt("Request_ID"),
                        r.getInt("Status_Code"),
                        r.getString("Headers"),
                        r.getString("Body"),
                        r.getString("Content_Type"),
                        r.getString("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responses;
    }
}