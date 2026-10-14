package rs.ac.bg.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.student.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;

import java.util.List;

public class StudentMain {
    public static void main(String[] args) throws Exception {
// Uncomment and change fallowing lines

        GeneralOperations generalOperations = new sf220364_GeneralOperations();
        GenresOperations genresOperations = new sf220364_GenresOperations();
        MoviesOperations moviesOperations = new sf220364_MoviesOperations();
        RatingsOperations ratingsOperation = new sf220364_RatingsOperations();
        TagsOperations tagsOperations = new sf220364_TagsOperations();
        UsersOperations usersOperations = new sf220364_UsersOperations();
        WatchlistsOperations watchlistsOperations = new sf220364_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}