package tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import app.classes.Lexer;
import app.classes.exceptions.LexicalException;

public class LexerTests {
    
    private static List<String> programs = new ArrayList<>();

    @BeforeClass
    public static void initializeTests() throws FileNotFoundException {
        File file = new File("./bin/tests/lexer_tests.txt");
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\n\r\n");
            while (scanner.hasNext()) {
                String line = scanner.next();
                programs.add(line);
            }
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }
    
    @Test
    public void printTokensTest() throws LexicalException {
        // Тест 1
        var expr = programs.get(0);
        var actual = new Lexer(expr).printTokens();
        var expected = "INT 2\nPLUS +\nINT 2\n";
        assertEquals("Ошибка в тесте 1", actual, expected);
        System.out.println("Тест 1\n" + expr + "\n" + actual);
        // Тест 2
        expr = programs.get(1);
        actual = new Lexer(expr).printTokens();
        expected = "DEF def\nVAR main\nLPAREN (\nVAR a\nVAR b\nRPAREN )\n"
                + "LBRACE {\nRETURN return\nRBRACE }\n";
        assertEquals("Ошибка в тесте 2", actual, expected);
        System.out.println("Тест 2\n" + expr + "\n" + actual);
        // Тест 3
        expr = programs.get(2);
        actual = new Lexer(expr).printTokens();
        expected = "VAR a\nEQ =\nINT 2\nVAR b\nEQ =\nSTRING 2\n";
        assertEquals("Ошибка в тесте 3", actual, expected);
        System.out.println("Тест 3\n" + expr + "\n" + actual);
        // Тест 4
        expr = programs.get(3);
        actual = new Lexer(expr).printTokens();
        expected = "BOOL true\nVAR trues\nVAR ffalse\nBOOL false\n";
        assertEquals("Ошибка в тесте 4", actual, expected);
        System.out.println("Тест 4\n" + expr + "\n" + actual);
        // Тест 5
        expr = programs.get(4);
        try {
            actual = new Lexer(expr).printTokens();
            Assert.fail("Ошибка в тесте 5");
        } catch (Exception e) {
            Assert.assertTrue(e.toString(), true);
            System.out.println("Тест 5\n" + expr + "\n" + e.toString());
        }
        // Тест 6
        expr = programs.get(5);
        try {
            actual = new Lexer(expr).printTokens();
            Assert.fail("Ошибка в тесте 6");
        } catch (Exception e) {
            Assert.assertTrue(e.toString(), true);
            System.out.println("Тест 6\n" + expr + "\n" + e.toString());
        }
        // Тест 7
        expr = programs.get(6);
        actual = new Lexer(expr).printTokens();
        expected = "VAR a1\nEQ =\nINT 1\nVAR b\n";
        assertEquals("Ошибка в тесте 7", actual, expected);
        System.out.println("Тест 7\n" + expr + "\n" + actual);
        // Тест 8
        expr = programs.get(7);
        actual = new Lexer(expr).printTokens();
        expected = "COMMENT  Comment\n";
        assertEquals("Ошибка в тесте 8", actual, expected);
        System.out.println("Тест 8\n" + expr + "\n" + actual);
        // Тест 9
        expr = programs.get(8);
        actual = new Lexer(expr).printTokens();
        expected = "STRING text\nVAR DEF\n";
        assertEquals("Ошибка в тесте 9", actual, expected);
        System.out.println("Тест 9\n" + expr + "\n" + actual);
        // Тест 10
        expr = programs.get(9);
        try {
            actual = new Lexer(expr).printTokens();
            Assert.fail("Ошибка в тесте 10");
        } catch (Exception e) {
            Assert.assertTrue(e.toString(), true);
            System.out.println("Тест 10\n" + expr + "\n" + e.toString());
        }
    }
}