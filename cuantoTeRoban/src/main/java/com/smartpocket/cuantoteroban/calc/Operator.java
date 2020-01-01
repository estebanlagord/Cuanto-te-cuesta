package com.smartpocket.cuantoteroban.calc;

public class Operator
{
    private String  operString;
    private boolean unary;
    private boolean binary;
    private boolean ternary;
    private boolean rightAssociative;
    private boolean leftAssociative;
 
    // private static String[] orderOfOperations = { "br^", "bl* bl/", "bl+ bl-", "un-" };
 
    /**
     * Constructs an operator
     * 
     * @param operatorText
     *            the first character must denote unary status ('u' for unary, 'b' for binary, 't' for ternary)<br>
     *            the second character must denote associative status ('r' for right, 'l' for left, 'n' for none)
     * 
     */
    public Operator (String operatorText)
    {
        // check unary status
        if (operatorText.charAt(0) == 'u')
        {
            unary = true;
            binary = false;
            ternary = false;
        }
        else if (operatorText.charAt(0) == 'b')
        {
            unary = false;
            binary = true;
            ternary = false;
        }
        else if (operatorText.charAt(0) == 't')
        {
            unary = false;
            binary = false;
            ternary = true;
        }
        else
        {
            throw new SyntaxError();
        }
 
        // check associative status
        if (operatorText.charAt(1) == 'r')
        {
            rightAssociative = true;
            leftAssociative = false;
        }
        else if (operatorText.charAt(1) == 'l')
        {
            rightAssociative = false;
            leftAssociative = true;
        }
        else if (operatorText.charAt(1) == 'n')
        {
            rightAssociative = false;
            leftAssociative = false;
        }
        else
        {
            throw new SyntaxError();
        }
        this.operString = operatorText;
    }
 
    /**
     * @return the operator string (minus flags)
     */
    public String getOperator ()
    {
        return this.operString.substring(2);
    }
 
    public String toString ()
    {
        return this.getOperator();
    }
 
    /**
     * @param withFlags
     *            whether flags will be returned with string or not
     * @return the operator string
     */
    public String getOperator (boolean withFlags)
    {
        if (withFlags)
        {
            return this.operString;
        }
        else
        {
            return getOperator();
        }
    }
 
    /**
     * @return the unary
     */
    public boolean isUnary ()
    {
        return this.unary;
    }
 
    /**
     * @return the binary
     */
    public boolean isBinary ()
    {
        return this.binary;
    }
 
    /**
     * @return the ternary
     */
    public boolean isTernary ()
    {
        return this.ternary;
    }
 
    /**
     * @return the rightAssociative
     */
    public boolean isRightAssociative ()
    {
        return this.rightAssociative;
    }
 
    /**
     * @return the leftAssociative
     */
    public boolean isLeftAssociative ()
    {
        return this.leftAssociative;
    }
 
    public double evaluate (double[] args)
    {
        if (operString.equals("bl+"))
        {
            return args[0] + args[1];
        }
        else if (operString.equals("bl-"))
        {
            return args[0] - args[1];
        }
        else if (operString.equals("bl*"))
        {
            return args[0] * args[1];
        }
        else if (operString.equals("bl/"))
        {
            return args[0] / args[1];
        }
        else if (operString.equals("br^"))
        {
            return Math.pow(args[0], args[1]);
        }
        else if (operString.equals("un-"))
        {
            return -args[0];
        }
        else
        {
            throw new SyntaxError();
        }
    }
}