package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.MoviesOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class sf220364_MoviesOperations implements MoviesOperations {
    @Override
    public Integer addMovie(String s, Integer integer, String s1) {
        Connection conn = DBConnection.getInstance().getConnection();
        int movieId=0;
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Movie (Title,Director) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);) {
            stmt.setString(1, s);
            stmt.setString(2,s1);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                movieId=rs.getInt(1);
            }
        } catch (SQLException e) {
            // insertion failed (possibly trigger/constraint) — report failure
            return null;
        }
        if(movieId==0) return null;
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO MovieGenre (MovieId,GenreId) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);) {
            stmt.setInt(1, movieId);
            stmt.setInt(2,integer);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // failed to insert movie-genre mapping — return null
            return null;
        }
        return movieId;
    }

    @Override
    public Integer updateMovieTitle(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Movie SET Title=? WHERE Id=?");) {
            stmt.setString(1, s);
            stmt.setInt(2, integer);
            int i = stmt.executeUpdate();
            if (i == 0) return null;
            else return integer;
        } catch (SQLException e) {
            // update failed (movie may not exist or trigger blocked) — return null
            return null;
        }
    }

    @Override
    public Integer addGenreToMovie(Integer integer, Integer integer1) {
        Connection conn = DBConnection.getInstance().getConnection();
        try {
            try (PreparedStatement chk = conn.prepareStatement("SELECT GenreId FROM MovieGenre WHERE MovieId=? AND GenreId=?")) {
                chk.setInt(1, integer);
                chk.setInt(2, integer1);
                ResultSet r = chk.executeQuery();
                if (r.next()) return null;
            }
            try (PreparedStatement chk = conn.prepareStatement("SELECT Id FROM Movie WHERE Id=?")) {
                chk.setInt(1, integer);
                ResultSet r = chk.executeQuery();
                if (!r.next()) return null;
            }
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO MovieGenre (MovieId,GenreId) VALUES (?,?)");) {
                stmt.setInt(1, integer);
                stmt.setInt(2, integer1);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("[dbg] addGenreToMovie failed: " + e.getMessage());
            e.printStackTrace();
            // insert failed (duplicate/trigger) — return null
            return null;
        }
        return integer;
    }

    @Override
    public Integer removeGenreFromMovie(Integer integer, Integer integer1) {
        List<Integer> MyList = getGenreIdsForMovie(integer);
        if (!MyList.contains(integer1)) return null;
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieGenre WHERE MovieId=? and GenreId=?");) {
            stmt.setInt(1, integer);
            stmt.setInt(2, integer1);
            // check whether deleting this mapping would be the last for movie or genre
            try (PreparedStatement c1 = conn.prepareStatement("SELECT COUNT(*) FROM MovieGenre WHERE MovieId=?")) {
                c1.setInt(1, integer);
                ResultSet rc = c1.executeQuery();
                if (rc.next() && rc.getInt(1) <= 1) {
                    // trigger will block deleting the last mapping — return null to indicate failure
                    return null;
                }
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[dbg] removeGenreFromMovie failed: " + e.getMessage());
            e.printStackTrace();
            // delete blocked by trigger/constraint
            return null;
        }
        return integer;
    }

    @Override
    public Integer updateMovieDirector(Integer integer, String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE MOVIE SET Director=? WHERE Id=?");) {
            stmt.setString(1, s);
            stmt.setInt(2, integer);
            int i = stmt.executeUpdate();
            if (i == 0) return null;
            else return integer;
        } catch (SQLException e) {
            // update failed (movie may not exist or trigger blocked) — return null
            return null;
        }
    }

    @Override
    public Integer removeMovie(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Movie WHERE Id=?");
            PreparedStatement stmt2=conn.prepareStatement("DELETE FROM MovieGenre WHERE MovieId=?");
            PreparedStatement stmt3= conn.prepareStatement("DELETE FROM MovieTag where MovieId=?");
            PreparedStatement stmt4=conn.prepareStatement("DELETE FROM WatchlistMovie where MovieId=?");
            PreparedStatement stmt5=conn.prepareStatement("DELETE FROM Rating where MovieId=?");
        ) {
            stmt.setInt(1, integer);
            stmt2.setInt(1,integer);
            stmt3.setInt(1,integer);
            stmt4.setInt(1,integer);
            stmt5.setInt(1,integer);
            stmt2.executeUpdate();
            stmt3.executeUpdate();
            stmt4.executeUpdate();
            stmt5.executeUpdate();
            int i=stmt.executeUpdate();
            if(i==0) return null;
        } catch (SQLException e) {
            System.out.println("[dbg] removeMovie failed: " + e.getMessage());
            e.printStackTrace();
            // operation blocked by trigger/constraint — return null instead of throwing
            return null;
        }
        return integer;
    }

    @Override
    public List<Integer> getMovieIds(String s, String s1) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Movie where Title=? and Director=?");) {
            stmt.setString(1,s);
            stmt.setString(2,s1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getAllMovieIds() {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Movie");) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getMovieIdsByGenre(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieId FROM MovieGenre where GenreId=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getGenreIdsForMovie(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT GenreId FROM MovieGenre where MovieId=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public List<Integer> getMovieIdsByDirector(String s) {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Integer> MyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Movie where Director=?");) {
            stmt.setString(1,s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) MyList.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return MyList;
    }

    @Override
    public String getMovieTrend(Integer integer) {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Status FROM Movie where Id=?");) {
            stmt.setInt(1,integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
