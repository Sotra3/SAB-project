package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.TagsOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class sf220364_TagsOperations implements TagsOperations {
    @Override
    public Integer addTag(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        
        int TagId = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Tag WHERE Name=?")) {
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                TagId = rs.getInt(1);
            }
        } catch (SQLException e) {
            return null;
        }

        if (TagId == 0) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Tag (Name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, s);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) TagId = rs.getInt(1);
                else return null;
            } catch (SQLException e) {
                return null;
            }
        }

        try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO MovieTag (MovieId,TagId) VALUES(?,?)")) {
            stmt2.setInt(1, integer);
            stmt2.setInt(2, TagId);
            int i = stmt2.executeUpdate();
            if (i != 1) return null;
        } catch (SQLException e) {
            return null;
        }
        return integer;
    }

    @Override
    public Integer removeTag(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;
        int TagId = 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id from Tag where Name=?")) {
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) TagId = rs.getInt(1);
            else return null;
        } catch (SQLException e) {
            return null;
        }
        try (PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM MovieTag WHERE MovieId=? and TagId=?")) {
            stmt2.setInt(1, integer);
            stmt2.setInt(2, TagId);
            int i = stmt2.executeUpdate();
            if (i != 1) return null;
        } catch (SQLException e) {
            return null;
        }
        return integer;
    }

    @Override
    public int removeAllTagsForMovie(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        int i=0;
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieTag WHERE MovieId=?");) {
            stmt.setInt(1,integer);
            i = stmt.executeUpdate();
            return i;
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public boolean hasTag(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        int TagId = 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id from Tag where Name=?");) {
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) TagId = rs.getInt(1);
            else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement stmt2 = conn.prepareStatement("SELECT MovieId FROM MovieTag where MovieId=? and TagId=?");) {
            stmt2.setInt(1, integer);
            stmt2.setInt(2, TagId);
            ResultSet rs = stmt2.executeQuery();
            if (rs.next()) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public List<String> getTagsForMovie(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<String> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT t.Name FROM MovieTag mt join Tag t on mt.TagId=t.id where mt.MovieId=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getMovieIdsByTag(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT mt.MovieId FROM MovieTag mt join Tag t on mt.TagId=t.id where t.Name=?");) {
            stmt.setString(1,s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<String> getAllTags() {
        Connection conn = DBConnection.getInstance().getConnection();
        List<String> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT Name FROM Tag");) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }
}
