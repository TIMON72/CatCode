package app.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.classes.exceptions.LexicalException;

/**
 * Класс лексического анализа
 */
public class Lexer {
    // Используемые символы
    private static final String operators = "+-*/(){}=<>!&|";
    private static final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String digits = "0123456789";
    // Используемые операторы
    private static final Map<String, Token.Type> operatorsMap;
    static {
        operatorsMap = new HashMap<>();
        // Арифметические
        operatorsMap.put("+", Token.Type.PLUS);
        operatorsMap.put("-", Token.Type.MINUS);
        operatorsMap.put("*", Token.Type.STAR);
        operatorsMap.put("/", Token.Type.SLASH);
        // Присвоения
        operatorsMap.put("=", Token.Type.EQ);
        // Выражения
        operatorsMap.put("{", Token.Type.LBRACE);
        operatorsMap.put("}", Token.Type.RBRACE);
        operatorsMap.put("(", Token.Type.LPAREN);
        operatorsMap.put(")", Token.Type.RPAREN);
        // Сравнения
        operatorsMap.put("<", Token.Type.LT);
        operatorsMap.put(">", Token.Type.GT);
        operatorsMap.put("==", Token.Type.EQEQ);
        operatorsMap.put("!=", Token.Type.EXCLEQ);
        operatorsMap.put("<=", Token.Type.LTEQ);
        operatorsMap.put(">=", Token.Type.GTEQ);
        // Логические
        operatorsMap.put("&&", Token.Type.AMPAMP);
        operatorsMap.put("||", Token.Type.BARBAR);
        operatorsMap.put("!", Token.Type.EXCL);
    }
    // Поля
    private String input;
    private int length;
    // Перменные
    private int line = 1;
    private int pos = 0;
    private int globalPos;
    private List<Token> tokens = new ArrayList<>();

    /**
     * Конструктор
     * 
     * @param input входной поток
     * @throws LexicalException лексическая ошибка
     */
    public Lexer(String input) throws LexicalException {
        this.input = input;
        this.length = input.length();
        tokenize();
    }

    /**
     * Получить символ на relativePosition
     * 
     * @param relativePosition относительная позиция (от глобального положения
     *                         каретки)
     * @return возвращает символ
     */
    private char peek(int relativePosition) {
        final int position = globalPos + relativePosition;
        if (position >= length)
            return '\0';
        return input.charAt(position);
    }

    /**
     * Смещение каретки вперед
     * 
     * @return возвращает символ после смещения
     */
    private char next() {
        globalPos++;
        pos++;
        if ("\n".indexOf(peek(0)) != -1) {
            line++;
            pos = 0;
        }
        return peek(0);
    }

    /**
     * Токенизация int
     */
    private void tokenizeInt() {
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while (true) {
            // Символ - не цифра?
            if (digits.indexOf(current) == -1)
                break;
            buffer.append(current);
            current = next();
        }
        tokens.add(new Token(Token.Type.INT, buffer.toString(), line, pos));
    }

    /**
     * Токенизация комментария
     */
    private void tokenizeComment() {
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while ("\r\n\t\0".indexOf(current) == -1) {
            buffer.append(current);
            current = next();
        }
        tokens.add(new Token(Token.Type.COMMENT, buffer.toString(), line, pos));
    }

    /**
     * Токенизация оператора
     * 
     * @throws LexicalException лексическая ошибка
     */
    private void tokenizeOperator() throws LexicalException {
        char current = peek(0);
        if (current == '/') {
            if (peek(1) == '/') {
                next();
                next();
                tokenizeComment();
                return;
            }
        }
        else if (current == '&') {
            if (peek(1) == '&') {
                next();
                next();
                tokens.add(new Token(operatorsMap.get("&&"), "&&", line, pos));
                return;
            }
        }
        else if (current == '|') {
            if (peek(1) == '|') {
                next();
                next();
                tokens.add(new Token(operatorsMap.get("||"), "||", line, pos));
                return;
            }
        }
        else if (current == '=') {
            if (peek(1) == '=') {
                next();
                next();
                tokens.add(new Token(operatorsMap.get("=="), "==", line, pos));
                return;
            }
        }
        else if (current == '!') {
            if (peek(1) == '=') {
                next();
                next();
                tokens.add(new Token(operatorsMap.get("!="), "!=", line, pos));
                return;
            }
        }
        final StringBuilder buffer = new StringBuilder();
        while (true) {
            final String text = buffer.toString();
            if (!operatorsMap.containsKey(text + current)) {
                if (!text.isEmpty()) {
                    tokens.add(new Token(operatorsMap.get(text), text, line, pos));
                    return;
                } else
                    throw new LexicalException(String.format("Unidentified symbol \"%s\" of operator: string %d, position %d\n%s",
                        current, line, pos, printTokens()));
            }
            buffer.append(current);
            current = next();
        }
    }

    /**
     * Токенизация ключевого слова
     */
    private void tokenizeKeyword() {
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while (true) {
            if ((letters.indexOf(current) == -1) && (digits.indexOf(current) == -1)) {
                break;
            }
            buffer.append(current);
            current = next();
        }
        final String keyword = buffer.toString();
        Token.Type keywordType = null;
        switch (keyword) {
            case "print":
                keywordType = Token.Type.PRINT;
                break;
            case "if":
                keywordType = Token.Type.IF;
                break;
            case "else":
                keywordType = Token.Type.ELSE;
                break;
            case "while":
                keywordType = Token.Type.WHILE;
                break;
            case "do":
                keywordType = Token.Type.DO;
                break;
            case "def":
                keywordType = Token.Type.DEF;
                break;
            case "return":
                keywordType = Token.Type.RETURN;
                break;
            case "struct":
                keywordType = Token.Type.STRUCT;
                break;
            default:
                keywordType = Token.Type.VAR;
                break;
        }
        tokens.add(new Token(keywordType, keyword, line, pos));
    }

    /**
     * Токенизация boolean
     */
    private void tokenizeBool() {
        final StringBuilder buffer = new StringBuilder();
        int startPos = globalPos;
        char current = peek(0);
        while (true) {
            if (letters.indexOf(current) == -1) {
                break;
            }
            buffer.append(current);
            current = next();
        }
        String boolStr = buffer.toString();
        if (!boolStr.equals("true") && !boolStr.equals("false")) {
            globalPos = startPos;
            tokenizeKeyword();
        } else
            tokens.add(new Token(Token.Type.BOOL, boolStr, line, pos));
    }

    /**
     * Токенизация string
     * 
     * @throws LexicalException лексическая ошибка
     */
    private void tokenizeString() throws LexicalException {
        next(); // Пропускаем открывающую "
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while (true) {
            if (current == '\\') {
                current = next();
                switch (current) {
                    case '"':
                        current = next();
                        buffer.append('"');
                        continue;
                    case 'n':
                        current = next();
                        buffer.append('\n');
                        continue;
                    case 't':
                        current = next();
                        buffer.append('\t');
                        continue;
                }
                buffer.append('\\');
                continue;
            }
            if (current == '"')
                break;
            else if ("\n\0".indexOf(peek(1)) != -1)
                throw new LexicalException(String.format("Waited closing quote: string %d, position %d\n%s",
                    line, pos, printTokens()));
            buffer.append(current);
            current = next();
        }
        next(); // Пропускаем закрывающую "

        tokens.add(new Token(Token.Type.STRING, buffer.toString(), line, pos));
    }

    /**
     * Токенизация
     * 
     * @return возвращает список токенов
     * @throws LexicalException лексическая ошибка
     */
    private List<Token> tokenize() throws LexicalException {
        while (globalPos < length) {
            final char current = peek(0);
            // Текущий символ - цифра?
            if (digits.indexOf(current) != -1)
                tokenizeInt();
            // Текущий символ - оператор?
            else if (operators.indexOf(current) != -1)
                tokenizeOperator();
            // Текущий символ - буква?
            else if (letters.indexOf(current) != -1) {
                if (current == 't' || current == 'f')
                    tokenizeBool();
                else
                    tokenizeKeyword();
            }
            // Текущий символ - " ?
            else if (current == '"')
                tokenizeString();
            // Текущий символ - пробел или переход на строку?
            else if ("\r\n\t\0 ".indexOf(current) != -1)
                next();
            else
                throw new LexicalException(String.format("Unidentified symbol \"%s\": string %d, position %d\n%s",
                    current, line, pos, printTokens()));
        }
        return tokens;
    }

    /**
     * Получить токены
     * 
     * @return список токенов
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Вывод токенов в консоль
     * 
     * @return возвращает список токенов
     */
    public String printTokens() {
        String result = "";
        if (tokens.size() != 0)
            for (Token t : tokens)
                result += t.toString() + '\n';
        return result;
    }
}