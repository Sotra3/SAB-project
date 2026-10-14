package rs.ac.bg.etf.sab.student;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String username="sa";
    private static final String password="123";
    private static final String database="SABProjekat";
    private static final int port=1433;
    private static final String server="localhost";

    private static final String connectionUrl =
            "jdbc:sqlserver://localhost:1433;databaseName=SABProjekat;encrypt=true;trustServerCertificate=true;";


    private Connection connection;
    private DBConnection() {
        try {
            connection=DriverManager.getConnection(connectionUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static DBConnection db=null;
    public static DBConnection getInstance() {
        if(db==null)
            db=new DBConnection();
        return db;
    }
    public Connection getConnection() {
        return connection;
    }
}
