package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.GenresOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class sf220364_GenresOperations implements GenresOperations {
    @Override
    public Integer addGenre(String s) {
        if (doesGenreExist(s)) {
            return null;
        }
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Genre (Name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);) {
            stmt.setString(1, s);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Integer updateGenre(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Genre SET Name=? WHERE Id=?");) {
            stmt.setString(1, s);
            stmt.setInt(2, integer);
            int i = stmt.executeUpdate();
            if (i == 0) return null;
            else return integer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer removeGenre(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Genre WHERE Id=?");) {
            stmt.setInt(1, integer);
            int i = stmt.executeUpdate();
            if (i == 0) return null;
            else return integer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesGenreExist(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM GENRE WHERE Name=?");) {
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return true;
            else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer getGenreId(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM GENRE WHERE Name=?");) {
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            else return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> getAllGenreIds() {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM GENRE");) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }
}
