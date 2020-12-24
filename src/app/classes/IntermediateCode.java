package app.classes;

import java.util.ArrayList;
import java.util.Map;

public class IntermediateCode {
    // Свойства
    private static String iCode = "";
    private static Boolean stop = false;
    // Поля
    private static String tab1 = "  ";

    /**
     * Получить промежуточный код
     * 
     * @return код (string)
     */
    public static String getICode() {
        return iCode;
    }

    /**
     * Остановить запись промежуточного кода
     * @param stop логическая команда
     */
    public static void setStop(Boolean stop) {
        IntermediateCode.stop = stop;
    }

    /**
     * Сбросить промежуточный код
     */
    public static void resetICode() {
        iCode = "";
    }

    /**
     * Задать промежуточный код функции - начало
     * @param funcVar имя функции
     * @param funcArgs аргументы функции
     */
    public static void setFunction_Start(String funcVar, Map<String, Expression> funcArgs) {
        iCode += funcVar + ":\n";
        for (String a : funcArgs.keySet()) {
            iCode += tab1 + "pop " + a + "\n";
        }
    }
    /**
     * Задать промежуточный код функции - конец
     */
    public static void setFunction_End() {
        iCode += "return" + "\n";
    }

    /**
     * Задать промежуточный код функции - вызов
     * @param funcVar имя функции
     * @param funcArgs аргументы функции
     */
    public static void setFunction_Call(String funcVar, Map<String, Expression> funcArgs) {
        ArrayList<Expression> funcArgsExprList = new ArrayList<Expression>();
        for (Expression e : funcArgs.values()) {
            funcArgsExprList.add(0, e);
        }
        for (Expression e : funcArgsExprList) {
            setExpression(e);
            iCode += tab1 + "push $" + e.getName() + "\n";
        }
        iCode += tab1 + "call " + funcVar + " " + funcArgs.size() + "\n";
    }

    /**
     * Задать промежуточный код присвоения
     * @param var переменная
     * @param expr присваиваемое выражение
     */
    public static void setAssign(String var, Expression expr) {
        if (stop)
            return;
        for (Expression e : expr.getExpressions()) {
            setExpression(e);
        }
        iCode += tab1 + expr.getICode() + "\n";
        iCode += tab1 + var + "=$" + expr.getName() + "\n";
    }

    /**
     * Задать промежуточный код операции
     */
    public static void setOperation(String operation, Expression expr) {
        if (stop)
            return;
        for (Expression e : expr.getExpressions()) {
            setExpression(e);
        }
        iCode += tab1 + operation + " $" + expr.getName() + "\n";
    }

    /**
     * Задать ложное условие с переходом к метке
     * @param expr выражение условия
     * @param label метка
     */
    public static void setIfFalse(Expression expr, int label) {
        if (stop)
            return;
        for (Expression e : expr.getExpressions()) {
            setExpression(e);
        }
        iCode += tab1 + expr.getICode() + "\n";
        iCode += tab1 + "ifFalse $" + expr.getName() + " goto " + label + "\n";
    }

    /**
     * Задать истинное условие с переходом к метке
     * @param expr выражение условия
     * @param label метка
     */
    public static void setIfTrue(Expression expr, int label) {
        if (stop)
            return;
        for (Expression e : expr.getExpressions()) {
            setExpression(e);
        }
        iCode += tab1 + expr.getICode() + "\n";
        iCode += tab1 + "ifTrue $" + expr.getName() + " goto " + label + "\n";
    }

    /**
     * Задать переход к метке goto
     * @param label метка
     */
    public static void setGoto(int label) {
        iCode += tab1 + "goto " + label + "\n";
    }

    /**
     * Задать метку
     * @param label метка
     */
    public static void setLabel(int label) {
        iCode += label + ":\n";
    }

    /**
     * Задать метку с переходом на предыдущую метку
     * @param label метка
     */
    public static void setLabel_GotoPreviousLabel(int label) {
        iCode += label + ":\n";
        iCode += tab1 + "goto " + (label - 1) + "\n";
        iCode += (label - 1) + ":\n";
    }

    /**
     * Задать промежуточный код выражения
     * @param expr выражение
     */
    private static void setExpression(Expression expr) {
        iCode += tab1 + expr.getICode() + "\n";
    }
}