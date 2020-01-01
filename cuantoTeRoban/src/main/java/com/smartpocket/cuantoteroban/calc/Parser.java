package com.smartpocket.cuantoteroban.calc;
import java.util.EmptyStackException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 
/**
 * A parser for a mathematical expression. Handles numbers, operators, and functions.
 * 
 * @author Andrew
 * 
 */
public class Parser
{
    // allowed number forms
    private static Pattern   numRegex          = Pattern.compile("([0-9]*[.]?[0-9]+[eE]{1}-?[0-9]+)"
                                                       + "|([0-9]+[.]?[0-9]*[eE]{1}-?[0-9]+)" + "|([0-9]*[.]?[0-9]+)"
                                                       + "|([0-9]+)");
    // allowed operators
    private Pattern          operRegex         = Pattern.compile("([\\Q()+--*/^\\E]{1})");
    // allowed function names
    private Pattern          funcRegex         = Pattern.compile("[a-zA-Z]+[_1-9a-zA-Z]*");
    // order of operations (the higher the index, the higher the precedence. same index = same precedence
    private String[]         orderOfOperations = { "bl+ bl-", "bl* bl/", "un-", "br^", "unFUNCTION" };
    // type constants
    private static final int NONE              = -1;
    private static final int NUMBER            = 0;
    private static final int OPERATOR          = 1;
    private static final int LPARENTHESIS      = 2;
    private static final int RPARENTHESIS      = 3;
    private static final int FUNCTION          = 4;
    private static final int SEPARATOR         = 5;
    // private variables
    private Stack<Object>    output;
    private Stack<Object>    operator;
    private Object           holding;
    private Object           previous;
    private Matcher          numMatcher;
    private Matcher          operMatcher;
    private Matcher          funcMatcher;
    private String           equation;
    private int              counter;
    private boolean          parsed;
 
    /**
     * Creates an empty parser
     */
    public Parser ()
    {}
 
    /**
     * Creates a new parser on an equation.
     * 
     * @param equation
     */
    public Parser (String equation)
    {
        // initialize everything
        this.equation = equation;
        numMatcher = numRegex.matcher(equation);
        operMatcher = operRegex.matcher(equation);
        funcMatcher = funcRegex.matcher(equation);
        // build the RPN stack
        parse();
        parsed = true;
    }
 
    /**
     * builds an RPN stack for this expression.
     */
    private void parse ()
    {
        // initialize
        counter = 0;
        holding = null;
        previous = null;
        output = new Stack<Object>();
        operator = new Stack<Object>();
        while (counter < equation.length())
        {
            // get a token
            int held = getNext();
            // If the token is a number, then add it to the output queue.
            if (held == NUMBER)
            {
                output.push(holding);
            }
            // If the token is a function token, then push it onto the stack.
            else if (held == FUNCTION)
            {
                operator.push(holding);
            }
 
            // If the token is a function argument separator (e.g., a comma):
            else if (held == SEPARATOR)
            {
                while (true)
                {
                    // Until the token at the top of the stack is a left parenthesis
                    // pop operators off the stack onto the output queue.
                    if (operator.isEmpty())
                    {
                        // separator misplaces or parentheses mismatched
                        throw new SyntaxError();
                    }
                    Object temp = operator.pop();
                    held = getType(temp);
                    if (held == LPARENTHESIS)
                    {
                        // found left parenthesis
                        // get function handle
                        Function func = (Function) operator.pop();
                        func.increaseNumArgs();
                        // put function handle back on stack
                        operator.push(func);
                        // put left parenthesis back on stack
                        operator.push(temp);
                        break;
                    }
                    output.push(temp);
                }
            }
            // If the token is an operator, then:
            else if (held == OPERATOR)
            {
                // while there is an operator at the top of the stack
                while (!operator.isEmpty() && getType(operator.peek()) == OPERATOR)
                {
                    Operator top = (Operator) operator.peek();
                    // if holding is associative or left-associative and has a lower or equal precedence to the top or
                    // holding is right-associative and has lower precedence
                    if (checkPrecedence((Operator) holding, top))
                    {
                        // pop top onto output
                        output.push(operator.pop());
                    }
                    else
                    {
                        break;
                    }
                }
 
                // push holding onto the stack.
                operator.push(holding);
 
            }
            // If the token is a left parenthesis, then push it onto the stack.
            else if (held == LPARENTHESIS)
            {
                if (getType(previous) == FUNCTION)
                {
                    // previous item was a function set args to 0
                    Function temp = (Function) operator.pop();
                    temp.setNumArgs(1);
                    operator.push(temp);
                }
                operator.push(holding);
            }
            // If the token is a right parenthesis:
            else if (held == RPARENTHESIS)
            {
                try
                {
                    while (getType(operator.peek()) != LPARENTHESIS)
                    {
                        // pop operators off the stack onto the output queue
                        output.push(operator.pop());
                    }
                    // Pop the left parenthesis from the stack
                    operator.pop();
                    // If the token at the top of the stack is a function token, pop it onto the output queue.
                    if (!operator.isEmpty() && getType(operator.peek()) == FUNCTION)
                    {
                        output.push(operator.pop());
                    }
                }
                catch (EmptyStackException e)
                {
                    // Parenthesis mismatch
                    throw new SyntaxError();
                }
            }
        }
 
        // While there are still operator tokens in the stack:
        while (!operator.empty())
        {
            holding = operator.pop();
            if (getType(holding) == LPARENTHESIS)
            {
                // There are mismatched parenthesis
                throw new SyntaxError();
            }
            // Pop the operator onto the output queue
            output.push(holding);
        }
    }
 
    /**
     * Determines the order of operations relationship between op1 and op2
     * 
     * @param op1
     * @param op2
     * @return true if:<br>
     *         - if op1 is left-associative and has a lower or equal precedence to op2<br>
     *         - if op1 is right-associative and has lower precedence to op2<br>
     *         - if op1 is not associative and has lower precedence to op2<br>
     *         false otherwise
     */
    private boolean checkPrecedence (Operator op1, Operator op2)
    {
        int op1Prec = -1;
        int op2Prec = -1;
        for (int i = 0; i < orderOfOperations.length; i++)
        {
            Scanner reader = new Scanner(orderOfOperations[i]);
            while (reader.hasNext())
            {
                String comparing = reader.next();
                if (comparing.equals(op1.getOperator(true)))
                {
                    // found a match!
                    op1Prec = i;
                }
                if (comparing.equals(op2.getOperator(true)))
                {
                    // found a match!
                    op2Prec = i;
                }
            }
            reader.close();
        }
        if (op1Prec == -1 || op2Prec == -1)
        {
            // invalid operator
            throw new SyntaxError();
        }
        if (op1.isLeftAssociative())
        {
            return op1Prec <= op2Prec;
        }
        else if (op1.isRightAssociative())
        {
            return op1Prec < op2Prec;
        }
        else
        {
            return op1Prec < op2Prec;
        }
    }
 
    /**
     * puts the parsed item starting at counter into holding.
     * 
     * @return the type of the object now being held
     */
    private int getNext ()
    {
        while (Character.isWhitespace(equation.charAt(counter)))
        {
            counter++;
        }
        // set previous
        previous = holding;
        if (equation.charAt(counter) == '(')
        {
            // left parenthesis
            holding = "(";
            counter++;
            return LPARENTHESIS;
        }
        else if (equation.charAt(counter) == ')')
        {
            // right parenthesis
            holding = ")";
            counter++;
            return RPARENTHESIS;
        }
        else if (equation.charAt(counter) == ',')
        {
            // separator
            holding = ",";
            counter++;
            return SEPARATOR;
        }
        else if (funcMatcher.find(counter) && funcMatcher.start() == counter)
        {
            // function
            holding = new Function(funcMatcher.group());
            ((Function) holding).setNumArgs(1);
            counter = funcMatcher.end();
            return FUNCTION;
        }
        else if (numMatcher.find(counter) && numMatcher.start() == counter)
        {
            // a number
            holding = Double.parseDouble(numMatcher.group());
            counter = numMatcher.end();
            return NUMBER;
        }
        else if (operMatcher.find(counter) && operMatcher.start() == counter)
        {
            // an operator
            String operFlags;
            if (getType(previous) != NUMBER && getType(previous) != RPARENTHESIS)
            {
                // unary operator
                operFlags = "u";
                // determine associative status for unary operator
                for (int i = 0; i < orderOfOperations.length; i++)
                {
                    Scanner reader = new Scanner(orderOfOperations[i]);
                    while (reader.hasNext())
                    {
                        String temp = reader.next();
                        if (temp.charAt(0) == 'u' && temp.substring(2).equals(operMatcher.group()))
                        {
                            // found matching operator
                            operFlags += temp.charAt(1);
                        }
                    }
                    reader.close();
                }
            }
            else
            {
                operFlags = "b";
                // determine associative status for binary operator
                for (int i = 0; i < orderOfOperations.length; i++)
                {
                    Scanner reader = new Scanner(orderOfOperations[i]);
                    while (reader.hasNext())
                    {
                        String temp = reader.next();
                        if (temp.charAt(0) == 'b' && temp.substring(2).equals(operMatcher.group()))
                        {
                            // found matching operator
                            operFlags += temp.charAt(1);
                        }
                    }
                    reader.close();
                }
            }
            holding = new Operator(operFlags + operMatcher.group());
            counter = operMatcher.end();
            return OPERATOR;
        }
        else
        {
            // invalid token
            throw new SyntaxError();
        }
    }
 
    /**
     * Returns the token type of an object
     * 
     * @param object
     * @return
     */
    private int getType (Object object)
    {
        if (object instanceof Double)
        {
            return NUMBER;
        }
        else if (object instanceof Function)
        {
            return FUNCTION;
        }
        else if (object instanceof Operator)
        {
            return OPERATOR;
        }
        else if (object instanceof String && object.equals("("))
        {
            return LPARENTHESIS;
        }
        else if (object instanceof String && object.equals(")"))
        {
            return RPARENTHESIS;
        }
        else
        {
            return NONE;
        }
    }
 
    /**
     * Evaluates the expression. Only evaluates if parsed.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
	public double evaluate ()
    {
        if (!parsed)
        {
            throw new SyntaxError();
        }
        // create a temp place for output stack
        Stack<Object> temp = (Stack<Object>) output.clone();
        double answer = eval();
        output = temp;
        return answer;
    }
 
    /**
     * helper function for evaluate
     * 
     * @return
     */
    private double eval ()
    {
        Object held;
        while (!output.isEmpty())
        {
            held = output.pop();
            if (getType(held) == OPERATOR)
            {
                if (((Operator) held).isUnary())
                {
                    double[] args = { this.eval() };
                    return ((Operator) held).evaluate(args);
                }
                else if (((Operator) held).isBinary())
                {
                    double temp = this.eval();
                    double[] args = { this.eval(), temp };
                    return ((Operator) held).evaluate(args);
                }
                else
                {
                    throw new SyntaxError();
                }
            }
            else if (getType(held) == FUNCTION)
            {
                double[] args = new double[((Function) held).getNumArgs()];
                for (int i = args.length - 1; i >= 0; i--)
                {
                    args[i] = this.eval();
                }
                return ((Function) held).evaluate(args);
            }
            else if (getType(held) == NUMBER)
            {
                return ((Double) held).doubleValue();
            }
            else
            {
                throw new SyntaxError();
            }
        }
        throw new SyntaxError();
    }
 
    /**
     * Returns a string representation of the expression. Empty if un-parsed
     */
    @SuppressWarnings("unchecked")
	public String toString ()
    {
        if (!parsed)
        {
            return "";
        }
        // create a temporary version of stack
        Stack<Object> temp = (Stack<Object>) output.clone();
        String answer = toStringHelper();
        output = temp;
        return answer;
    }
 
    /**
     * toString helper method
     * 
     * @return
     */
    private String toStringHelper ()
    {
        String answer = "";
        Object held;
        while (!output.isEmpty())
        {
            held = output.pop();
            if (getType(held) == OPERATOR)
            {
                if (((Operator) held).isUnary())
                {
                    return "(" + held.toString() + this.toStringHelper() + ")";
                }
                else if (((Operator) held).isBinary())
                {
                    String temp = this.toStringHelper();
                    return "(" + this.toStringHelper() + held.toString() + temp + ")";
                }
                else
                {
                    throw new SyntaxError();
                }
            }
            else if (getType(held) == FUNCTION)
            {
                return held + "(" + this.toStringHelper() + ")";
            }
            else if (getType(held) == NUMBER)
            {
                return held.toString();
            }
            else
            {
                throw new SyntaxError();
            }
        }
        return answer;
    }
}