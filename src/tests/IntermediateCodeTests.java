package tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import app.classes.IntermediateCode;
import app.classes.Lexer;
import app.classes.Parser;

public class IntermediateCodeTests {

    private static List<String> programs = new ArrayList<>();

    @BeforeClass
    public static void initializeTests() throws FileNotFoundException {
        File file = new File("./bin/tests/intermediateCode_tests.txt");
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
    public void getICodeTest() throws Exception {
        for (int i = 0; i < programs.size(); i=i+2) {
            String expr = programs.get(i);
            Parser parser = new Parser(new Lexer(expr).getTokens());
            String actual = "";
            String expected = ""; 
            try {
                parser.parse();
                actual = IntermediateCode.getICode().replace("\r", "");
                //System.out.println(actual);
                expected = programs.get(i + 1).replace("\r", "") + "\n";
            } catch (Exception ex) {
                actual = ex.toString() + "\n";
                expected = programs.get(i + 1).replace("\r", "") + "\n";
            }
            assertEquals("Ошибка в тесте " + (i / 2), actual, expected);
            System.out.println("Тест " + (i / 2) + "\n" + expr + "\n" + actual);
        }
    }
}