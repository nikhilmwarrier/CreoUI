import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHandle {
    private static final String url = "jdbc:sqlite:oop.db";

    public static Connection connect() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
        return con;
    }

    public static void Initialize() {
        try (Connection con = connect(); Statement s = con.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON;");
            s.execute("CREATE TABLE IF NOT EXISTS Requests (ID INTEGER PRIMARY KEY, Method TEXT, URL TEXT, Headers TEXT, Body TEXT, Timestamp DATETIME DEFAULT current_timestamp);");
            s.execute("CREATE TABLE IF NOT EXISTS Responses (ID INTEGER PRIMARY KEY, Request_ID INTEGER, Status_Code INTEGER, Headers TEXT, Body TEXT, Content_Type TEXT, Timestamp DATETIME DEFAULT current_timestamp, FOREIGN KEY(Request_ID) REFERENCES Requests(ID));");
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }
}