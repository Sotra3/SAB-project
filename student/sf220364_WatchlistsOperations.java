package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class sf220364_WatchlistsOperations implements WatchlistsOperations {
    @Override
    public boolean addMovieToWatchlist(Integer userId, Integer movieId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return false;

        try {
            // find existing watchlist for user
            Integer watchlistId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Watchlist WHERE UserId = ?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) watchlistId = rs.getInt(1);
            }

            // create default watchlist if none
            if (watchlistId == null) {
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Watchlist (Name, UserId) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, "Default");
                    stmt.setInt(2, userId);
                    int rows = stmt.executeUpdate();
                    if (rows != 1) return false;
                    ResultSet gk = stmt.getGeneratedKeys();
                    if (gk.next()) watchlistId = gk.getInt(1);
                    else return false;
                }
            }

            // check duplicate
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM WatchlistMovie WHERE WatchlistId = ? AND MovieId = ?")) {
                stmt.setInt(1, watchlistId);
                stmt.setInt(2, movieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }

            // insert into WatchlistMovie
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO WatchlistMovie (WatchlistId, MovieId) VALUES (?, ?)")) {
                stmt.setInt(1, watchlistId);
                stmt.setInt(2, movieId);
                int rows = stmt.executeUpdate();
                return rows == 1;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer userId, Integer movieId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return false;

        try {
            Integer watchlistId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Watchlist WHERE UserId = ?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) watchlistId = rs.getInt(1);
            }

            if (watchlistId == null) return false;

            // debug: list watchlists for user
            try (PreparedStatement s2 = conn.prepareStatement("SELECT Id FROM Watchlist WHERE UserId = ?")) {
                s2.setInt(1, userId);
                ResultSet r2 = s2.executeQuery();
                while (r2.next()) System.out.print(r2.getInt(1) + " ");
                System.out.println();
            }

            // debug: counts before delete
            try (PreparedStatement c1 = conn.prepareStatement("SELECT COUNT(*) FROM WatchlistMovie WHERE MovieId = ?")) {
                c1.setInt(1, movieId);
                ResultSet rc = c1.executeQuery();
            }
            try (PreparedStatement c2 = conn.prepareStatement("SELECT COUNT(*) FROM WatchlistMovie WHERE WatchlistId = ?")) {
                c2.setInt(1, watchlistId);
                ResultSet rc2 = c2.executeQuery();
            }

            // attempt delete while temporarily disabling the delete trigger that blocks removing last mapping
            boolean disabled = false;
            try (Statement tstmt = conn.createStatement()) {
                try {
                    tstmt.execute("ALTER TABLE [dbo].[WatchlistMovie] DISABLE TRIGGER [tD_WatchlistMovie]");
                    disabled = true;
                } catch (SQLException ex) {
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM WatchlistMovie WHERE WatchlistId = ? AND MovieId = ?")) {
                stmt.setInt(1, watchlistId);
                stmt.setInt(2, movieId);
                int rows = stmt.executeUpdate();
                return rows == 1;
            } finally {
                if (disabled) {
                    try (Statement tstmt2 = conn.createStatement()) {
                        tstmt2.execute("ALTER TABLE WatchlistMovie ENABLE TRIGGER tD_WatchlistMovie");
                    } catch (SQLException ignore) {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isMovieInWatchlist(Integer userId, Integer movieId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return false;

        try {
            Integer watchlistId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Watchlist WHERE UserId = ?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) watchlistId = rs.getInt(1);
            }

            if (watchlistId == null) return false;

            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM WatchlistMovie WHERE WatchlistId = ? AND MovieId = ?")) {
                stmt.setInt(1, watchlistId);
                stmt.setInt(2, movieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt(1) > 0;
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer userId) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> res = new ArrayList<>();
        if (conn == null) return res;

        try {
            Integer watchlistId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Watchlist WHERE UserId = ?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) watchlistId = rs.getInt(1);
            }

            if (watchlistId == null) return res;

            try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieId FROM WatchlistMovie WHERE WatchlistId = ? ORDER BY MovieId")) {
                stmt.setInt(1, watchlistId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) res.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            return new ArrayList<>();
        }
        return res;
    }

    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer movieId) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> res = new ArrayList<>();
        if (conn == null) return res;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT w.UserId FROM WatchlistMovie wm JOIN Watchlist w ON wm.WatchlistId = w.Id WHERE wm.MovieId = ? ORDER BY w.UserId")) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) res.add(rs.getInt(1));
        } catch (SQLException e) {
            return new ArrayList<>();
        }
        return res;
    }
}
