package app;

import java.awt.Desktop;
import java.io.File;
import java.util.Scanner;

import tests.IntermediateCodeTests;
import tests.LexerTests;
import tests.ParserTests;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        int option = 0;
        String input = "";
        while (!"4".equals(input)) {
            System.out.flush();
            System.out.println("1. Запуск тестов Лексера");
            System.out.println("2. Запуск тестов Парсера");
            System.out.println("3. Запуск тестов промежуточного кода");
            System.out.println("4. Открыть документацию");
            System.out.println("5. Открыть ChangeLog");
            System.out.println("6. Выход");
            System.out.print("Выберите пункт меню: ");
            input = scan.next();
            try {
                option = Integer.parseInt(input);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Нажмите ENTER чтобы продолжить");
                System.in.read();
            }
            switch (option) {
                case 1:
                try {
                    LexerTests.initializeTests();
                    LexerTests lt = new LexerTests();
                    lt.printTokensTest();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Нажмите ENTER чтобы продолжить");
                    System.in.read();
                    return;
                }
                case 2:
                try {
                    ParserTests.initializeTests();
                    ParserTests pt = new ParserTests();
                    pt.printStatementsTest();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Нажмите ENTER чтобы продолжить");
                    System.in.read();
                    return;
                }
                case 3:
                try {
                    IntermediateCodeTests.initializeTests();
                    IntermediateCodeTests tests = new IntermediateCodeTests();
                    tests.getICodeTest();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Нажмите ENTER чтобы продолжить");
                    System.in.read();
                    return;
                }
                case 4:
                    try {
                        String url = "docs/html/index.html";
                        File file = new File(url);
                        Desktop.getDesktop().browse(file.toURI());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Нажмите ENTER чтобы продолжить");
                        System.in.read();
                        return;
                    }
                    break;
                case 5:
                    try {
                        String url = "CHANGELOG.md";
                        File file = new File(url);
                        Desktop.getDesktop().browse(file.toURI());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Нажмите ENTER чтобы продолжить");
                        System.in.read();
                        return;
                    }
                default:
                    return;
                    
            }
        }
        System.out.println("Нажмите ENTER чтобы продолжить");
        System.in.read();
        scan.close();
    }
}