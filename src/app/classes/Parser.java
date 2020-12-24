package app.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.classes.exceptions.SemanticException;
import app.classes.exceptions.SyntaxException;

public class Parser {
    // Поля
    private List<Token> tokens;
    private int size;
    private int globalPos;
    private int label;
    // Переменные
    private ArrayList<Function> functions = new ArrayList<>();
    private Map<String, Integer> functionsPos = new HashMap<String, Integer>();
    private Map<String, Expression> variables = new HashMap<String, Expression>();

    /**
     * Конструктор
     * 
     * @param tokens список токенов
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        size = tokens.size();
    }

    /**
     * Получить токен на relativePosition
     * 
     * @param relativePosition относительная позиция токена к глобальной
     * @return токен на текущей глобальной позиции или null, если достигнут конец
     *         списка токенов
     */
    private Token get(int relativePosition) {
        final int position = globalPos + relativePosition;
        if (position >= size)
            return null;
        return tokens.get(position);
    }

    /**
     * Совпадение типа токена с типом текущего токена
     * 
     * @param compared сравниваемый тип токена
     * @return true или false
     */
    private boolean isTypeMatch(Token.Type compared) {
        Token current = get(0);
        if (current == null || compared != current.getType())
            return false;
        globalPos++;
        return true;
    }

    /**
     * Задать функцию F
     * 
     * @return фуекцию
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Function setFunction() throws SyntaxException, SemanticException {
        Token current = get(0);
        String funcVar = "";
        HashMap<String, Expression> funcArgs = new HashMap<>();
        ArrayList<Statement> funcStates = new ArrayList<>();
        if (isTypeMatch(Token.Type.DEF)) {
            // Начало функции - def ... ( ...
            current = get(-1);
            if (!isTypeMatch(Token.Type.VAR)) {
                throw new SyntaxException(
                        String.format("waited \"VAR\" after \"def\": %s", current.getFullPosition()));
            }
            current = get(-1);
            funcVar = current.getText();
            if (!isTypeMatch(Token.Type.LPAREN)) {
                throw new SyntaxException(
                        String.format("waited \"(\" after \"%s\": %s", funcVar, current.getFullPosition()));
            }
            // Аргументы функции
            while (true) {
                current = get(-1);
                String var = get(0).getText();
                if (isTypeMatch(Token.Type.VAR)) {
                    current = get(-1);
                    Expression expr = new Expression(var, true);
                    funcArgs.put(var, expr);
                    variables.put(current.getText(), expr);
                } else if (isTypeMatch(Token.Type.RPAREN)) {
                    break;
                } else {
                    throw new SyntaxException(
                            String.format("waited \"VAR\" or \")\": %s", current.getFullPosition()));
                }
            }
            // Конец аргументов и начало тела функции = ... ) { ...
            current = get(-1);
            if (!isTypeMatch(Token.Type.LBRACE)) {
                throw new SyntaxException(
                        String.format("waited \"{\" after \"def ...(...)\": %s", current.getFullPosition()));
            }
            IntermediateCode.setFunction_Start(funcVar, funcArgs);
            // Тело функции
            functionsPos.put(funcVar, globalPos);
            while (true) {
                Statement state = setStatement();
                if (state == null)
                    break;
                funcStates.add(state);
            }
            // Конец тела функции - ... }
            current = get(-1);
            if (!isTypeMatch(Token.Type.RBRACE)) {
                throw new SyntaxException(String.format("waited \"}\" after \"%s\": %s", current.getText(),
                        current.getFullPosition()));
            }
            IntermediateCode.setFunction_End();
            return new Function(funcVar, funcArgs, variables, funcStates);
        }
        return null;
    }

    /**
     * Задать оператор S
     * 
     * @return оператор
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Statement setStatement() throws SyntaxException, SemanticException {
        if (isTypeMatch(Token.Type.COMMENT)) {
            globalPos++;
            globalPos--;
        }
        if (isTypeMatch(Token.Type.VAR)) {
            if (isTypeMatch(Token.Type.EQ)) {
                return setAssignmentStatement();
            }
            if (isTypeMatch(Token.Type.LPAREN)) {
                return setFunctionCallStatement();
            }
        }
        if (isTypeMatch(Token.Type.PRINT)) {
            return setPrintStatement();
        }
        if (isTypeMatch(Token.Type.IF)) {
            return setConditionalStatement();
        }
        if (isTypeMatch(Token.Type.WHILE)) {
            return setPrecyclicStatement();
        }
        if (isTypeMatch(Token.Type.DO)) {
            return setPostcyclicStatement();
        }
        if (isTypeMatch(Token.Type.RETURN))
            return setReturnableStatement();
        return null;
    }

    /**
     * Задать оператор присваивания
     * 
     * @return оператор
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Statement setAssignmentStatement() throws SyntaxException, SemanticException {
        Statement state = null;
        Token current = get(-2);
        String var = current.getText();
        Expression expr = setExpression();
        state = new Statement(var, "=", expr);
        IntermediateCode.setAssign(var, expr);
        variables.put(var, expr);
        return state;
    }

    /**
     * Задать оператор вызова функции
     * 
     * @return оператор
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Statement setFunctionCallStatement() throws SyntaxException, SemanticException {
        Token current = get(-2);
        String funcVar = current.getText();
        Map<String, Expression> funcArgs = new HashMap<String, Expression>();
        ArrayList<Statement> states = new ArrayList<>();
        Function calledFunc = null;
        try {
            calledFunc = functions.stream().filter(f -> f.getFunctionVariable().equals(funcVar)).findFirst().get();
        } catch (Exception ex) {
            throw new SemanticException(
                    String.format("function \"%s\" not declared: %s", funcVar, current.getFullPosition()));
        }
        String funcName = calledFunc.getName();
        // Аргументы функции
        for (Map.Entry<String, Expression> entry : calledFunc.getArguments().entrySet()) {
            current = get(0);
            String var = current.getText();
            if (isTypeMatch(Token.Type.RPAREN))
                break;
            Expression expr = null;
            if (isTypeMatch(Token.Type.VAR))
                expr = variables.get(var);
            else
                expr = setExpression();
            funcArgs.put(entry.getKey(), expr);
        }
        IntermediateCode.setFunction_Call(funcVar, funcArgs);
        // Тело функции
        int currentGlobalPos = globalPos; // запоминаем текущую позицию
        Map<String, Expression> oldVariables = variables; // запоминаем переменные до вызова
        globalPos = functionsPos.get(funcVar); // перемещаемся на позицию объявления функции после "("
        IntermediateCode.setStop(true);
        variables = funcArgs;
        while (true) {
            current = get(0);
            Statement state = setStatement();
            if (state == null)
                break;
            states.add(state);
        }
        globalPos = currentGlobalPos; // возврат на запомненную позицию
        IntermediateCode.setStop(false);
        variables = oldVariables; // возврат к запомненным переменным
        Statement state = new Statement(funcName, funcVar, funcArgs, states);
        current = get(-1);
        if (!isTypeMatch(Token.Type.RPAREN)) {
            throw new SyntaxException(
                    String.format("waited \")\" after \"%s\": %s", current.getText(), current.getFullPosition()));
        }

        return state;
    }

    /**
     * Задать оператор печати print(E)
     * 
     * @return оператор
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Statement setPrintStatement() throws SemanticException, SyntaxException {
        Token current = get(-1);
        if (!isTypeMatch(Token.Type.LPAREN)) {
            throw new SyntaxException(String.format("waited \"(\" after \"print\": %s", current.getFullPosition()));
        }
        Statement state;
        current = get(-1);
        Expression expr = setExpression();
        try {
            state = new Statement("print", expr);
        } catch (SemanticException se) {
            throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
        }
        current = get(-1);
        if (!isTypeMatch(Token.Type.RPAREN)) {
            throw new SyntaxException(
                    String.format("waited \")\" instead of \"%s\": %s", current.getText(), current.getFullPosition()));
        }
        IntermediateCode.setOperation("print", expr);
        return state;
    }

    /**
     * Задать условный оператор if (E) {S+} else {S+}
     * 
     * @return оператор
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Statement setConditionalStatement() throws SyntaxException, SemanticException {
        Token current = get(-1);
        Expression expr = null;
        Statement ifState = null;
        ArrayList<Statement> ifStates = new ArrayList<>();
        Statement elseState = null;
        ArrayList<Statement> elseStates = new ArrayList<>();
        // условие
        if (!isTypeMatch(Token.Type.LPAREN)) {
            throw new SyntaxException(String.format("waited \"(\" after \"if\": %s", current.getFullPosition()));
        }
        expr = setExpression();
        current = get(-1);
        if (!((Object) expr.getResult() instanceof Boolean)) {
            if (!((Object) expr.getResult() == null))
            throw new SemanticException(
                    String.format("waited class \"Boolean\" instead of \"%s\" with expression result \"%s\": %s",
                            expr.getResult().getClass(), current.getText(), current.getFullPosition()));
        }
        // if-операторы - ... ) {
        if (!isTypeMatch(Token.Type.RPAREN)) {
            throw new SyntaxException(
                    String.format("waited \")\" after \"%s\": %s", current.getText(), current.getFullPosition()));
        }
        current = get(-1);
        if (!isTypeMatch(Token.Type.LBRACE)) {
            throw new SyntaxException(String.format("waited \"{\" after \"if (...)\": %s", current.getFullPosition()));
        }
        IntermediateCode.setIfFalse(expr, label);
        while (true) {
            ifState = setStatement();
            if (ifState == null)
                break;

            ifStates.add(ifState);
        }
        IntermediateCode.setGoto(label + 1);
        current = get(-1);
        if (!isTypeMatch(Token.Type.RBRACE)) {
            throw new SyntaxException(
                    String.format("waited \"}\" after \"%s\": %s", current.getText(), current.getFullPosition()));
        }
        current = get(-1);
        // else-операторы
        if (isTypeMatch(Token.Type.ELSE)) {
            current = get(-1);
            if (!isTypeMatch(Token.Type.LBRACE)) {
                throw new SyntaxException(String.format("waited \"{\" after \"else\": %s", current.getFullPosition()));
            }
            IntermediateCode.setLabel(label);
            while (true) {
                elseState = setStatement();
                if (elseState == null)
                    break;
                elseStates.add(elseState);
            }
            IntermediateCode.setLabel(label + 1);
            current = get(-1);
            if (!isTypeMatch(Token.Type.RBRACE)) {
                throw new SyntaxException(
                        String.format("waited \"}\" after \"%s\": %s", current.getText(), current.getFullPosition()));
            }
            label += 2;
        } else {
            IntermediateCode.setLabel_GotoPreviousLabel(label + 1);
        }
        Statement state;
        state = new Statement(expr, ifStates, elseStates);
        return state;
    }

    /**
     * Задать предциклический оператор while (E) {S+}
     * 
     * @return оператор
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Statement setPrecyclicStatement() throws SemanticException, SyntaxException {
        Token current = get(-1);
        Expression expr = null;
        Statement state = null;
        int whileExprPos = -1;
        Expression whileExpr = null;
        ArrayList<Statement> states = new ArrayList<>();
        if (!isTypeMatch(Token.Type.LPAREN)) {
            throw new SyntaxException(String.format("waited \"(\" after \"while\": %s", current.getFullPosition()));
        }
        whileExprPos = globalPos;
        whileExpr = setExpression();
        expr = whileExpr;
        while (true) {
            current = get(-1);
            if (!((Object) whileExpr.getResult() instanceof Boolean)) {
                if (!((Object) expr.getResult() == null))
                    throw new SemanticException(
                        String.format("waited class \"Boolean\" instead of \"%s\" with expression result \"%s\": %s",
                                whileExpr.getResult().getClass(), current.getText(), current.getFullPosition()));
            }
            IntermediateCode.setLabel(label);
            IntermediateCode.setIfFalse(whileExpr, label + 1);
            if (!isTypeMatch(Token.Type.RPAREN)) {
                throw new SyntaxException(
                        String.format("waited \")\" after \"%s\": %s", current.getText(), current.getFullPosition()));
            }
            current = get(-1);
            if (!isTypeMatch(Token.Type.LBRACE)) {
                throw new SyntaxException(
                        String.format("waited \"{\" after \"while (...)\": %s", current.getFullPosition()));
            }
            while (true) {
                state = setStatement();
                if (state == null)
                    break;
                states.add(state);
            }
            current = get(-1);
            if (!isTypeMatch(Token.Type.RBRACE)) {
                throw new SyntaxException(
                        String.format("waited \"}\" after \"%s\": %s", current.getText(), current.getFullPosition()));
            }
            current = get(-1);
            IntermediateCode.setGoto(label + 2);
            IntermediateCode.setLabel(label + 1);
            IntermediateCode.setGoto(label + 3);
            label += 2;
            if (whileExpr.getResult() == null || !(Boolean) whileExpr.getResult())
                break;
            globalPos = whileExprPos;
            whileExpr = setExpression();
        }
        IntermediateCode.setLabel(label);
        IntermediateCode.setGoto(label + 1);
        IntermediateCode.setLabel(label + 1);
        state = new Statement(expr, states);
        return state;
    }

    /**
     * Задать постциклический оператор do {S+} while (E)
     * 
     * @return оператор
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Statement setPostcyclicStatement() throws SemanticException, SyntaxException {
        Token current = get(-1);
        Expression expr = null;
        Statement state = null;
        Expression whileExpr = null;
        ArrayList<Statement> states = new ArrayList<>();
        int whileExprPos = globalPos;
        do {
            current = get(-1);
            if (!isTypeMatch(Token.Type.LBRACE)) {
                throw new SyntaxException(String.format("waited \"{\" after \"do\": %s", current.getFullPosition()));
            }
            while (true) {
                state = setStatement();
                if (state == null)
                    break;
                states.add(state);
            }
            current = get(-1);
            if (!isTypeMatch(Token.Type.RBRACE)) {
                throw new SyntaxException(
                        String.format("waited \"}\" after \"%s\": %s", current.getText(), current.getFullPosition()));
            }
            current = get(-1);
            if (!isTypeMatch(Token.Type.WHILE)) {
                throw new SyntaxException(String.format("waited \"while\" after \"}\": %s", current.getText(),
                        current.getFullPosition()));
            }
            current = get(-1);
            if (!isTypeMatch(Token.Type.LPAREN)) {
                throw new SyntaxException(String.format("waited \"(\" after \"while\": %s", current.getText(),
                        current.getFullPosition()));
            }
            current = get(-1);
            whileExpr = setExpression();
            if (expr == null) {
                expr = whileExpr;
            }
            if (!((Object) whileExpr.getResult() instanceof Boolean)) {
                if (!((Object) expr.getResult() == null))
                throw new SemanticException(
                        String.format("waited class \"Boolean\" instead of \"%s\" with expression result \"%s\": %s",
                                whileExpr.getResult().getClass(), current.getText(), current.getFullPosition()));
            }
            if (!isTypeMatch(Token.Type.RPAREN)) {
                throw new SyntaxException(
                        String.format("waited \")\" after \"%s\": %s", current.getText(), current.getFullPosition()));
            }
            if (whileExpr.getResult() == null || !(Boolean) whileExpr.getResult())
                break;
            IntermediateCode.setIfTrue(whileExpr, label + 1);
            IntermediateCode.setLabel(label);
            IntermediateCode.setGoto(label + 2);
            IntermediateCode.setLabel(label + 1);
            label += 2;
            globalPos = whileExprPos;
        } while (true);
        IntermediateCode.setLabel(label);
        state = new Statement(states, expr);
        return state;
    }

    /**
     * Задать оператор возврата return E
     * 
     * @return оператор
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Statement setReturnableStatement() throws SyntaxException, SemanticException {
        Token current = get(-1);
        Statement state;
        current = get(-1);
        Expression expr = setExpression();
        try {
            state = new Statement("return", expr);
        } catch (SemanticException se) {
            throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
        }
        IntermediateCode.setOperation("return", expr);
        return state;
    }

    /**
     * Задать выражение E
     * 
     * @return выражение
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Expression setExpression() throws SyntaxException, SemanticException {
        Expression expr = setLogicalAdditionExpression();
        return expr;
    }

    /**
     * Задать выражение логического сложения E||E
     * 
     * @return выражение
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Expression setLogicalAdditionExpression() throws SemanticException, SyntaxException {
        Token current = get(0);
        Expression expr = setLogicalMultiplicationExpression();
        while (true) {
            if (isTypeMatch(Token.Type.BARBAR)) {
                try {
                    current = get(0);
                    expr = new Expression("||", expr, setLogicalMultiplicationExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            break;
        }
        return expr;
    }

    /**
     * Задать выражение логического умножения E&&E
     * 
     * @return выражение
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Expression setLogicalMultiplicationExpression() throws SemanticException, SyntaxException {
        Token current = get(0);
        Expression expr = setEqualityExpression();
        while (true) {
            if (isTypeMatch(Token.Type.AMPAMP)) {
                try {
                    current = get(0);
                    expr = new Expression("&&", expr, setEqualityExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            break;
        }
        return expr;
    }

    /**
     * Задать выражение равенства E==E (E!=E)
     * 
     * @return выражение
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Expression setEqualityExpression() throws SemanticException, SyntaxException {
        Token current = get(0);
        Expression expr = setComparisonExpression();
        if (isTypeMatch(Token.Type.EQEQ)) {
            try {
                current = get(0);
                expr = new Expression("==", expr, setComparisonExpression());
            } catch (SemanticException se) {
                throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
            }
            return expr;
        }
        if (isTypeMatch(Token.Type.EXCLEQ)) {
            try {
                current = get(0);
                expr = new Expression("!=", expr, setComparisonExpression());
            } catch (SemanticException se) {
                throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
            }
            return expr;
        }
        return expr;
    }

    /**
     * Задать выражение сравнения E>E E>=E E<E E<=E
     * 
     * @return выражение
     * @throws SemanticException семантическая ошибка
     * @throws SyntaxException   синтаксическая ошибка
     */
    private Expression setComparisonExpression() throws SemanticException, SyntaxException {
        Token current = get(0);
        Expression expr = setAdditionExpression();
        while (true) {
            if (isTypeMatch(Token.Type.GT)) {
                try {
                    current = get(0);
                    expr = new Expression(">", expr, setAdditionExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            if (isTypeMatch(Token.Type.GTEQ)) {
                try {
                    current = get(0);
                    expr = new Expression(">=", expr, setAdditionExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            if (isTypeMatch(Token.Type.LT)) {
                try {
                    current = get(0);
                    expr = new Expression("<", expr, setAdditionExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            if (isTypeMatch(Token.Type.LTEQ)) {
                try {
                    current = get(0);
                    expr = new Expression("<=", expr, setAdditionExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            break;
        }
        return expr;
    }

    /**
     * Задать выражение сложения E+E (вычитание E-E)
     * 
     * @return выражение
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Expression setAdditionExpression() throws SyntaxException, SemanticException {
        Token current = get(0);
        Expression expr = setMultiplicationExpression();
        while (true) {
            if (isTypeMatch(Token.Type.PLUS)) {
                try {
                    current = get(0);
                    expr = new Expression("+", expr, setMultiplicationExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            if (isTypeMatch(Token.Type.MINUS)) {
                try {
                    current = get(0);
                    expr = new Expression("-", expr, setMultiplicationExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            break;
        }
        return expr;
    }

    /**
     * Задать выражение умножения Е*Е (деление Е/E)
     * 
     * @return
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Expression setMultiplicationExpression() throws SyntaxException, SemanticException {
        Token current = get(0);
        Expression expr = setNegationExpression();
        while (true) {
            if (isTypeMatch(Token.Type.STAR)) {
                try {
                    current = get(0);
                    expr = new Expression("*", expr, setNegationExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            if (isTypeMatch(Token.Type.SLASH)) {
                try {
                    current = get(0);
                    expr = new Expression("/", expr, setNegationExpression());
                } catch (SemanticException se) {
                    throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
                }
                continue;
            }
            break;
        }
        return expr;
    }

    /**
     * Задать выражение отрицания -E !E
     * 
     * @return отрицание выражения
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Expression setNegationExpression() throws SyntaxException, SemanticException {
        Token current = get(0);
        Expression expr;
        if (isTypeMatch(Token.Type.EXCL)) {
            try {
                current = get(0);
                expr = new Expression("!", setEnclosingExpression());
            } catch (SemanticException se) {
                throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
            }
            return expr;
        }
        if (isTypeMatch(Token.Type.MINUS)) {
            try {
                current = get(0);
                expr = new Expression("-", setEnclosingExpression());
            } catch (SemanticException se) {
                throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
            }
            return expr;
        }
        if (isTypeMatch(Token.Type.PLUS)) {
            return setEnclosingExpression();
        }
        return setEnclosingExpression();
    }

    /**
     * Задать заключающее выражение (E)
     * 
     * @return заключенное выражение
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    private Expression setEnclosingExpression() throws SyntaxException, SemanticException {
        if (isTypeMatch(Token.Type.LPAREN)) {
            Token current = get(0);
            Expression expr;
            try {
                current = get(0);
                expr = new Expression("(", setExpression(), ")");
            } catch (SemanticException se) {
                throw new SemanticException(String.format("%s: %s", se.getMessage(), current.getFullPosition()));
            }
            if (isTypeMatch(Token.Type.RPAREN)) {
                current = get(0);
            } else
                throw new SyntaxException(
                        String.format("waited \")\" instead of %s: %s", current.getText(), current.getFullPosition()));
            return expr;
        }
        return setPrimitiveExpression();
    }

    /**
     * Задать примитивное выражение E
     * 
     * @return примитивное выражение
     * @throws NumberFormatException ошибка конвертации числа
     * @throws SyntaxException       синтаксическая ошибка
     * @throws SemanticException
     */
    private Expression setPrimitiveExpression() throws NumberFormatException, SyntaxException, SemanticException {
        Token current = get(0);
        if (current == null)
            return null;
        if (isTypeMatch(Token.Type.INT)) {
            Expression expr = new Expression(Integer.parseInt(current.getText()));
            return expr;
        }
        if (isTypeMatch(Token.Type.BOOL)) {
            Expression expr = new Expression(Boolean.parseBoolean(current.getText()));
            return expr;
        }
        if (isTypeMatch(Token.Type.STRING)) {
            Expression expr = new Expression(current.getText());
            return expr;
        }
        if (isTypeMatch(Token.Type.VAR)) {
            String var = current.getText();
            Expression expr = null;
            if (variables.get(var) != null)
                expr = variables.get(var);
            else
                throw new SemanticException(
                        String.format("variable \"%s\" not initialized: %s", var, current.getFullPosition()));
            return expr;
        }
        throw new SyntaxException(
                String.format("unknown expression \"%s\": %s", current.getText(), current.getFullPosition()));
    }

    /**
     * Парсинг
     * 
     * @return операторы
     * @throws SyntaxException   синтаксическая ошибка
     * @throws SemanticException семантическая ошибка
     */
    public List<Function> parse() throws SyntaxException, SemanticException {
        Expression.resetCounter();
        Statement.resetCounter();
        Function.resetCounter();
        IntermediateCode.resetICode();
        label = 0;
        while (true) {
            Function func = setFunction();
            if (func == null)
                break;
            functions.add(func);
            variables.clear();
        }
        return functions;
    }

    /**
     * Печать операторов
     * 
     * @return список операторов в формате строки
     */
    public String printFunctions() {
        String result = "";
        if (tokens.size() != 0)
            for (Function f : functions)
                if (f != null)
                    result += f.printFunction();
        return result;
    }
}