package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.RatingsOperations;
import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class sf220364_RatingsOperations implements RatingsOperations {
    @Override
    public boolean addRating(Integer integer, Integer integer1, Integer integer2) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Rating (UserId, MovieId, Value, Date) VALUES (?, ?, ?, GETDATE())");
            CallableStatement csmt=conn.prepareCall("{call SP_REWARD_USER_Reward(?,?)}")
        )
        {
            csmt.setInt(1,integer);
            csmt.setInt(2,integer1);
            stmt.setInt(1, integer);
            stmt.setInt(2, integer1);
            stmt.setInt(3, integer2);
            int i = stmt.executeUpdate();
            csmt.execute();
            return i != 0;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateRating(Integer integer, Integer integer1, Integer integer2) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Rating SET Value=? WHERE UserId=? and MovieId=?");) {
            stmt.setInt(1, integer2);
            stmt.setInt(2, integer);
            stmt.setInt(3,integer1);
            int i = stmt.executeUpdate();
            return i == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeRating(Integer integer, Integer integer1) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Rating WHERE UserId=? and MovieId=?");) {
            stmt.setInt(1, integer);
            stmt.setInt(2, integer1);
            int i = stmt.executeUpdate();
            return i==1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer getRating(Integer integer, Integer integer1) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Value FROM Rating WHERE UserId=? and MovieId=?");) {
            stmt.setInt(1, integer);
            stmt.setInt(2, integer1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            else return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieId FROM Rating where UserId=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT UserId FROM Rating where MovieId=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }
}
