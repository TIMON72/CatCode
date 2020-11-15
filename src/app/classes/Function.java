package app.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Function {
    // Свойства
    private String name;
    private String text;
    private String functionVariable;
    private Map<String, Expression> arguments = new HashMap<String, Expression>();
    private Expression returnedExpression;
    // Поля
    private Map<String, Expression> variables = new HashMap<String, Expression>();
    private ArrayList<Statement> statements = new ArrayList<>();
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
     * @return возвращаемое выражение
     */
    public Object getReturnedExpression() {
        return returnedExpression;
    }

    /**
     * Получить содержимое выражения
     * 
     * @return содержимое выражения
     */
    public String getText() {
        return text;
    }

    /**
     * Получить переменную функции
     * 
     * @return имя переменной функции
     */
    public String getFunctionVariable() {
        return functionVariable;
    }

    /**
     * Получить словарь аргументов функции
     * 
     * @return словарь строка-выражение аргументов
     */
    public Map<String, Expression> getArguments() {
        return arguments;
    }

    /**
     * Конструктор
     * 
     * @param funcVar    переменная-имя функции
     * @param funcArgs   аргументы функции
     * @param funcVars   переменные функции
     * @param funcStates операторы функции
     * @param rtrnExpr   возвращаемое выражение функции
     */
    public Function(String funcVar, Map<String, Expression> funcArgs, Map<String, Expression> funcVars,
            ArrayList<Statement> funcStates) {
        name = "F" + counter++;
        functionVariable = funcVar;
        arguments = funcArgs;
        variables.putAll(funcArgs);
        variables.putAll(funcVars);
        String argsText = " ( ";
        for (Expression e : arguments.values()) {
            argsText = argsText + e.getName() + " ";
        }
        argsText = argsText + ") ";
        statements = funcStates;
        String statesText = "{ ";
        for (Statement s : statements) {
            statesText = statesText + s.getName() + " ";
        }
        statesText = statesText + "} ";
        //returnedExpression = rtrnExpr;
        text = name + " -> def " + funcVar + argsText + statesText;
    }

    /**
     * Печать операторов
     * 
     * @return список операторов в формате строки
     */
    public String printStatements() {
        String result = "";
        for (Statement s : statements)
            if (s != null)
                result += "  " +  s.getText() + '\n' + s.printStatement();
        return result;
    }

    /**
     * Печать функции
     * 
     * @return функцию в формате строки
     */
    public String printFunction() {
        String result = "";
        String argsText = "";
        for (Expression e : arguments.values())
            argsText += "  " + e.getName() + " -> " + e.getText() + "\n";
        result += text + '\n' + argsText + printStatements();
        return result;
    }

    /**
     * Сброс счетчика
     */
    public static void resetCounter() {
        counter = 0;
    }
}