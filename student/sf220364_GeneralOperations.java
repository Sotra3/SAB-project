package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sf220364_GeneralOperations implements GeneralOperations {
    @Override
    public void eraseAll() {
        Connection conn=DBConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();) {
            stmt.executeUpdate("EXEC sp_MSforeachtable \"ALTER TABLE ? DISABLE TRIGGER ALL\"");

            stmt.executeUpdate("DELETE FROM WatchlistMovie");
            stmt.executeUpdate("DELETE FROM Watchlist");
            stmt.executeUpdate("DELETE FROM Rating");
            stmt.executeUpdate("DELETE FROM MovieTag");
            stmt.executeUpdate("DELETE FROM MovieGenre");
            stmt.executeUpdate("DELETE FROM Movie");
            stmt.executeUpdate("DELETE FROM Tag");
            stmt.executeUpdate("DELETE FROM Genre");
            stmt.executeUpdate("DELETE FROM Users");

            stmt.executeUpdate("DBCC CHECKIDENT ('Genre', RESEED, 0)");
            stmt.executeUpdate("DBCC CHECKIDENT ('Tag', RESEED, 0)");
            stmt.executeUpdate("DBCC CHECKIDENT ('Movie', RESEED, 0)");
            stmt.executeUpdate("DBCC CHECKIDENT ('Users', RESEED, 0)");
            stmt.executeUpdate("DBCC CHECKIDENT ('Watchlist', RESEED, 0)");

            stmt.executeUpdate("EXEC sp_MSforeachtable \"ALTER TABLE ? ENABLE TRIGGER ALL\"");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
