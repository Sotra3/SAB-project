package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class sf220364_UsersOperations implements UsersOperations {
@Override
    public Integer addUser(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
    try (PreparedStatement stmt1=conn.prepareStatement("Select Username from Users");)
    {
        List<String> MyList=new ArrayList<>();
        ResultSet rs=stmt1.executeQuery();
        while(rs.next())MyList.add(rs.getString(1));
        if(MyList.contains(s)) return null;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Users (Username) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setString(1, s);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    @Override
    public Integer updateUser(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE Users SET Username=? where Id=?")) {
            stmt.setString(1, s);
            stmt.setInt(2,integer);
            int i=stmt.executeUpdate();
            if (i==1) return integer;
        } catch (SQLException e) {
            return null;
        }
        return null;
}

    @Override
    public Integer removeUser(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement p1 = conn.prepareStatement("DELETE FROM WatchlistMovie WHERE WatchlistId IN (SELECT Id FROM Watchlist WHERE UserId=?)")) {
                p1.setInt(1, integer);
                p1.executeUpdate();
            }

            try (PreparedStatement p2 = conn.prepareStatement("DELETE FROM Watchlist WHERE UserId=?")) {
                p2.setInt(1, integer);
                p2.executeUpdate();
            }

            try (PreparedStatement p3 = conn.prepareStatement("DELETE FROM Rating WHERE UserId=?")) {
                p3.setInt(1, integer);
                p3.executeUpdate();
            }

            try (PreparedStatement p4 = conn.prepareStatement("DELETE FROM Users WHERE Id=?")) {
                p4.setInt(1, integer);
                int deleted = p4.executeUpdate();
                if (deleted != 1) {
                    conn.rollback();
                    return null;
                }
            }

            conn.commit();
            return integer;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return null;
        } finally {
            try { if (conn != null) conn.setAutoCommit(previousAutoCommit); } catch (SQLException ex) { /* ignore */ }
        }
    }

    @Override
    public boolean doesUserExist(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT Id from Users where Username=?")) {
            stmt.setString(1, s);
            ResultSet rs =stmt.executeQuery();
            if (rs.next()) return true;
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public Integer getUserId(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT Id from Users where Username=?")) {
            stmt.setString(1, s);
            ResultSet rs =stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            return null;
        }
        return null;
}

    @Override
    public List<Integer> getAllUserIds() {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Users");) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer integer) {
        List<Integer> result = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();

        String sql =
                "SELECT m.Id, AVG(CAST(r2.Value AS FLOAT)) AS AvgRating, COUNT(r2.Value) AS RatingCount " +
                        "FROM Movie m " +
                        "JOIN MovieGenre mg ON mg.MovieId = m.Id " +
                        "JOIN Rating r2 ON r2.MovieId = m.Id " +
                        "WHERE mg.GenreId IN ( " +
                        "    SELECT mg2.GenreId " +
                        "    FROM Rating r " +
                        "    JOIN MovieGenre mg2 ON mg2.MovieId = r.MovieId " +
                        "    WHERE r.UserId = ? " +
                        "    GROUP BY mg2.GenreId " +
                        "    HAVING AVG(CAST(r.Value AS FLOAT)) >= 8 " +
                        ") " +
                        "AND m.Id NOT IN (SELECT MovieId FROM Rating WHERE UserId = ?) " +
                        "AND m.Id NOT IN ( " +
                        "    SELECT wm.MovieId FROM WatchlistMovie wm " +
                        "    JOIN Watchlist w ON w.Id = wm.WatchlistId " +
                        "    WHERE w.UserId = ? " +
                        ") " +
                        "GROUP BY m.Id " +
                        "HAVING (COUNT(r2.Value) >= 4 AND AVG(CAST(r2.Value AS FLOAT)) >= 7.5) " +
                        "    OR (COUNT(r2.Value) < 4 AND AVG(CAST(r2.Value AS FLOAT)) >= 9) " +
                        "ORDER BY AvgRating DESC, m.Id ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            stmt.setInt(2, integer);
            stmt.setInt(3, integer);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
}

    @Override
    public Integer getRewards(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT RewardCount from Users where Id=?")) {
            stmt.setInt(1, integer);
            ResultSet rs =stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            return null;
        }
        return null;
}

    @Override
    public List<String> getThematicSpecializations(Integer integer) {
        List<String> result = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        String sql = "SELECT t.Name " +
                "FROM Rating r " +
                "JOIN MovieTag mt ON r.MovieId = mt.MovieId " +
                "JOIN Tag t ON mt.TagId = t.Id " +
                "WHERE r.UserId = ? AND r.Value >= 8 " +
                "GROUP BY t.Id, t.Name " +
                "HAVING COUNT(DISTINCT mt.MovieId) >= 2";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;    }

    @Override
    public String getUserDescription(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();

        int ratingCount = 0;
        String countSql = "SELECT COUNT(*) FROM Rating WHERE UserId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
            stmt.setInt(1, integer);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ratingCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (ratingCount < 10) {
            return "undefined";
        }

        int distinctTagCount = 0;
        String tagSql =
                "SELECT COUNT(DISTINCT mt.TagId) " +
                        "FROM Rating r " +
                        "JOIN MovieTag mt ON mt.MovieId = r.MovieId " +
                        "WHERE r.UserId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(tagSql)) {
            stmt.setInt(1, integer);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    distinctTagCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (distinctTagCount >= 10) {
            return "curious";
        } else {
            return "focuser";
        }
    }
}
