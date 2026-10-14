package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class debugg {
    public static void main(String[] args) {
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

        // Promeni klasu ovde ako zelis da testiras drugu (npr. RatingsOperationsTest.class)
        Result result = JUnitCore.runClasses(rs.ac.bg.etf.sab.tests.PublicModuleTest.class);

        System.out.println("=== NEUSPESNI TESTOVI ===");
        for (Failure failure : result.getFailures()) {
            System.out.println("TEST: " + failure.getTestHeader());
            System.out.println("GRESKA: " + failure.getMessage());
            System.out.println("DETALJI:");
            System.out.println(failure.getTrace());
            System.out.println("----------------------------------------");
        }

        System.out.println("Uspesno: " + (result.getRunCount() - result.getFailureCount()) + " / " + result.getRunCount());
    }
}