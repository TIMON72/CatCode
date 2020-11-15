package app.classes;

import java.util.ArrayList;

import app.classes.exceptions.SemanticException;

public class Expression {
    // Свойства
    private String name;
    private Object result;
    private String text;
    private String iCode;
    // Поля
    private ArrayList<Expression> expressions;
    // Статические поля
    private static int counter = 0;

    /**
     * Получить имя выражения
     * 
     * @return имя выражения
     */
    public String getName() {
        return name;
    }

    /**
     * Получить результат выражения
     * 
     * @return результат выражения
     */
    public Object getResult() {
        return result;
    }

    /**
     * Получить содержимое выражения
     * 
     * @return содержимое выражения
     */
    public String getText() {
        return text;
    }

    public ArrayList<Expression> getExpressions() {
        return expressions;
    }

    public String getICode() {
        return iCode;
    }

    /**
     * Конструктор
     */
    public Expression(String var, Boolean bool) {
        name = "E" + counter++;
        result = null;
        text = null;
        expressions = new ArrayList<Expression>();
        iCode = "$" + name + "=" + var;
    }

    /**
     * Конструктор
     * 
     * @param expr выражение из одного целого числа
     */
    public Expression(Integer expr) {
        name = "E" + counter++;
        text = expr.toString();
        result = expr;
        expressions = new ArrayList<Expression>();
        iCode = "$" + name + "=" + expr;
    }

    /**
     * Констуктор
     * 
     * @param expr выражение из одного логического значения
     */
    public Expression(Boolean expr) {
        name = "E" + counter++;
        result = expr;
        text = expr.toString();
        expressions = new ArrayList<Expression>();
        iCode = "$" + name + "=" + expr;
    }

    /**
     * Конструктор
     * 
     * @param expr выражение из одного строчного значения
     */
    public Expression(String expr) {
        name = "E" + counter++;
        result = expr;
        text = "\"" + expr + "\"";
        expressions = new ArrayList<Expression>();
        iCode = "$" + name + "=" + expr;
    }

    /**
     * Конструктор
     * 
     * @param expr выражение
     * @throws SemanticException ошибка типа результата выражения
     */
    public Expression(Expression expr) throws SemanticException {
        name = String.format("%s", expr.name);
        Object resultExpr = expr.result;
        expressions = new ArrayList<Expression>(expr.expressions);
        expressions.add(expr);
        if (resultExpr instanceof Integer || resultExpr instanceof Boolean || resultExpr instanceof String) {
            result = expr.result;
            text = expr.text;
            iCode = "$" + name + "=" + expr.name;
        } else
            throw new SemanticException(
                    String.format("Undefined class result \"%s\"", resultExpr.getClass()));
    }

    /**
     * Конструктор
     * 
     * @param leftBr  левая скобка
     * @param expr    выражение
     * @param rightBr правая скобка
     */
    public Expression(String leftBr, Expression expr, String rightBr) {
        name = "E" + counter++;
        text = String.format("(%s) = %s", expr.name, expr.result);
        result = expr.result;
        expressions = new ArrayList<Expression>(expr.expressions);
        expressions.add(expr);
        iCode = "$" + name + "=" + expr.name;
    }

    /**
     * Конструктор
     * 
     * @param operation унарная операция
     * @param expr      выражение
     * @throws SemanticException семантическая ошибка
     */
    public Expression(String operation, Expression expr) throws SemanticException {
        name = "E" + counter++;
        if (expr.result instanceof Integer) {
            int expr1 = (int) expr.result;
            switch (operation) {
                case "-":
                    result = -expr1;
                    break;
            }
        } else if (expr.result instanceof Boolean) {
            boolean expr1 = (boolean) expr.result;
            switch (operation) {
                case "!":
                    result = !expr1;
                    break;
            }
        } else
            throw new SemanticException(String.format("Undefined class result \"%s\"", expr.result));
        text = operation + expr.name + " = " + result;
        expressions = new ArrayList<Expression>(expr.expressions);
        expressions.add(expr);
        iCode = "$" + name + "=" + operation + expr.name;
    }

    /**
     * Конструктор
     * 
     * @param operation арифметическая (логическая) операция
     * @param exprLeft  выражение слева
     * @param exprRight выражение справа
     * @throws SemanticException семантическая ошибка
     */
    public Expression(String operation, Expression exprLeft, Expression exprRight) throws SemanticException {
        name = "E" + counter++;
        if (exprLeft.result instanceof Integer && exprRight.result instanceof Integer) {
            int expr1 = (int) exprLeft.result;
            int expr2 = (int) exprRight.result;
            switch (operation) {
                case "*":
                    result = expr1 * expr2;
                    break;
                case "/":
                    try {
                        result = expr1 / expr2;
                    } catch (ArithmeticException ex) {
                        throw new SemanticException(ex.getMessage());
                    }
                    break;
                case "+":
                    result = expr1 + expr2;
                    break;
                case "-":
                    result = expr1 - expr2;
                    break;
                case ">":
                    result = expr1 > expr2;
                    break;
                case ">=":
                    result = expr1 >= expr2;
                    break;
                case "<":
                    result = expr1 < expr2;
                    break;
                case "<=":
                    result = expr1 <= expr2;
                    break;
                case "==":
                    result = expr1 == expr2;
                    break;
                case "!=":
                    result = expr1 != expr2;
                    break;
            }
        } else if (exprLeft.result instanceof Boolean && exprRight.result instanceof Boolean) {
            boolean expr1 = (boolean) exprLeft.result;
            boolean expr2 = (boolean) exprRight.result;
            switch (operation) {
                case "==":
                    result = expr1 == expr2;
                    break;
                case "!=":
                    result = expr1 != expr2;
                    break;
                case "&&":
                    result = expr1 && expr2;
                    break;
                case "||":
                    result = expr1 || expr2;
                    break;
            }
        } else if (exprLeft.result instanceof String && exprRight.result instanceof String) {
            String expr1 = (String) exprLeft.result;
            String expr2 = (String) exprRight.result;
            switch (operation) {
                case "+":
                    result = expr1 + expr2;
                    break;
                case "==":
                    result = expr1 == expr2;
                    break;
                case "!=":
                    result = expr1 != expr2;
                    break;
            }
        } else if (exprLeft.result == null || exprRight.result == null) {
            result = null;
        } else
            throw new SemanticException(String.format("Undefined class result \"%s%s%s\"",
                    exprLeft.result, operation, exprRight.result));
        text = exprLeft.name + operation + exprRight.name + " = " + exprLeft.result + operation + exprRight.result
                + " = " + result;
        expressions = new ArrayList<Expression>();
        expressions.addAll(exprLeft.expressions);
        expressions.add(exprLeft);
        expressions.addAll(exprRight.expressions);
        expressions.add(exprRight);
        iCode = "$" + name + "=$" + exprLeft.name + operation +"$" + exprRight.name;
    }

    /**
     * Сброс счетчика
     */
    public static void resetCounter() {
        counter = 0;
    }
}