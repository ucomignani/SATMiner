package fr.liris.bd.datagen;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class MainTest {

    @Test
    public void simpleCommandLines() throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        Main.parseArgs(new String[] { "-t", "toto", "--nb", "3", "-cp", "a" });
        Main.parseArgs(new String[] { "-t", "toto", "--nb", "3", "-cp", "a",
                "--sql", "<std>" });
    }

    @Test
    public void simpleRun() {
        Main.main(new String[] { "-t", "a", "--nb", "2", "-c", "--ncols", "3",
                "-p", "b", "-s", "c", "-i", "--column", "--ncols", "4", "-p",
                "d", "-s", "e", "--autos", "-cp", "e" });
        Main.main(new String[] { "-t", "a", "--nb", "2", "-c", "--ncols", "3",
                "-p", "b", "-s", "c", "-i", "--column", "--ncols", "4", "-p",
                "d", "-s", "e", "--autos", "-cp", "e",
                "--sql", "<std>" });
    }

}
