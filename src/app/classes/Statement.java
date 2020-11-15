package app.classes;

import java.util.ArrayList;
import java.util.Map;

import app.classes.exceptions.SemanticException;

public class Statement {
    // Свойства
    private String name;
    private Object result;
    private String text;
    // Поля
    private ArrayList<Expression> expressions = new ArrayList<>();
    private ArrayList<Statement> statements = new ArrayList<>(); // Внутренние операторы
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

    public void setExpressions(ArrayList<Expression> expressions) {
        this.expressions = expressions;
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    public void setStatements(ArrayList<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Конструктор
     * 
     * @param var       переменная
     * @param operation операция
     * @param expr      выражение
     * @throws SemanticException семантическая ошибка
     */
    public Statement(String var, String operation, Expression expr) throws SemanticException {
        name = "S" + counter++;
        expressions = new ArrayList<>(expr.getExpressions());
        expressions.add(expr);
        result = expr.getResult();
        if (!(result instanceof Integer || result instanceof Boolean || result instanceof String || result == null))
            throw new SemanticException(String.format("unknown class of expression result \"%s\"", expr.getText()));
        statements = new ArrayList<>();
        text = name + " -> " + var + operation + expr.getName() + " = " + result;
    }

    /**
     * Конструктор
     * 
     * @param operation операция
     * @param expr      выражение
     * @throws SemanticException
     */
    public Statement(String operation, Expression expr) throws SemanticException {
        name = "S" + counter++;
        expressions = new ArrayList<>(expr.getExpressions());
        expressions.add(expr);
        result = expr.getResult();
        if (!(result instanceof Integer || result instanceof Boolean || result instanceof String || result == null))
            throw new SemanticException(String.format("unknown class of expression result \"%s\"", expr.getText()));
        statements = new ArrayList<>();
        switch (operation) {
            case "print":
                result = expr.getResult();
                text = name + " -> " + operation + "(" + expr.getName() + ")" + " = " + result;
            case "return":
                result = expr;
                text = name + " -> " + operation + " " + expr.getName();
        }
    }

    /**
     * Конструктор
     * 
     * @param expr       выражение if
     * @param ifStates   список операторов оператора if
     * @param elseStates список операторов оператора else
     */
    public Statement(Expression expr, ArrayList<Statement> ifStates, ArrayList<Statement> elseStates) {
        name = "S" + counter++;
        expressions = new ArrayList<>(expr.getExpressions());
        expressions.add(expr);
        statements = new ArrayList<>();
        String ifStatesText = "{ ";
        for (Statement s : ifStates) {
            statements.addAll(s.statements);
            statements.add(s);
            ifStatesText = ifStatesText + s.getName() + " ";
        }
        ifStatesText = ifStatesText + "}";
        String elseStatesText = "";
        if (elseStates.size() != 0) {
            elseStatesText = " else { ";
            for (Statement s : elseStates) {
                statements.addAll(s.statements);
                statements.add(s);
                elseStatesText = elseStatesText + s.getName() + " ";
            }
            elseStatesText = elseStatesText + "}";
        }
        result = (Boolean) expr.getResult();
        text = name + " -> " + "if ( " + expr.getName() + " ) " + ifStatesText + elseStatesText;
    }

    /**
     * Конструктор
     * 
     * @param whileExpr   выражение while
     * @param whileStates список операторов оператора while
     */
    public Statement(Expression whileExpr, ArrayList<Statement> whileStates) {
        name = "S" + counter++;
        expressions = new ArrayList<>(whileExpr.getExpressions());
        expressions.add(whileExpr);
        statements = new ArrayList<>();
        String statesText = "{ ";
        for (Statement s : whileStates) {
            statements.addAll(s.statements);
            statements.add(s);
            statesText = statesText + s.getName() + " ";
        }
        statesText = statesText + "}";
        result = null;
        text = name + " -> " + "while ( " + whileExpr.getName() + " ) " + statesText;
    }

    /**
     * Конструктор
     * 
     * @param whileStates список операторов оператора while
     * @param whileExpr   выражение while
     */
    public Statement(ArrayList<Statement> whileStates, Expression whileExpr) {
        name = "S" + counter++;
        expressions = new ArrayList<>(whileExpr.getExpressions());
        expressions.add(whileExpr);
        statements = new ArrayList<>();
        String statesText = "{ ";
        for (Statement s : whileStates) {
            statements.addAll(s.statements);
            statements.add(s);
            statesText = statesText + s.getName() + " ";
        }
        statesText = statesText + "}";
        result = null;
        text = name + " -> " + "do " + statesText + " while ( " + whileExpr.getName() + " )";
    }

    /**
     * Конструктор
     * 
     * @param funcName   имя функции
     * @param funcVar    переменная-имя функции
     * @param funcArgs   аргументы функции
     * @param funcStates операторы функции
     * @param rtrnExpr   возвращаемое выражение функции
     * @throws SemanticException
     */
    public Statement(String funcName, String funcVar, Map<String, Expression> funcArgs,
            ArrayList<Statement> funcStates) {
        name = "S" + counter++;
        String argsText = " ( ";
        for (Expression e : funcArgs.values()) {
            expressions.add(e);
            argsText = argsText + e.getName() + " ";
        }
        argsText = argsText + ") ";
        statements = funcStates;
        String statesText = "{ ";
        for (Statement s : statements) {
            statesText = statesText + s.getName() + " ";
        }
        statesText = statesText + "} ";
        result = (Expression) funcStates.get(funcStates.size() - 1).getResult();
        text = name + " -> " + funcName + " = " + funcVar + argsText + statesText;
    }

    /**
     * Печать выражений
     * 
     * @return выражения в формате строки
     */
    private String printExpressions() {
        String result = "";
        for (Expression e : expressions)
            if (e != null)
                result += "    " + e.getName() + " -> " + e.getText() + '\n';
        return result;
    }

    /**
     * Печать операции
     * 
     * @return внутренние операции и выражения в формате строки
     */
    public String printStatement() {
        String result = "";
        result += printExpressions();
        for (Statement s : statements)
            if (s != null)
                result += "    " + s.getText() + '\n' + s.printExpressions();
        return result;
    }

    /**
     * Сброс счетчика
     */
    public static void resetCounter() {
        counter = 0;
    }
}