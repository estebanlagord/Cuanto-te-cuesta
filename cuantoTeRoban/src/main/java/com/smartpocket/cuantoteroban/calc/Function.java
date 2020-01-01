package com.smartpocket.cuantoteroban.calc;

public class Function extends Operator
{
    private String name;
    private int    numArgs;
 
    /**
     * creates a function with a name and a list of args
     * 
     * @param name
     */
    public Function (String name)
    {
        super("unFUNCTION");
        this.name = name;
        numArgs = 0;
    }
 
    /**
     * Returns an object with what this function evaluates to. Note: Functions must override this method
     * 
     * @return
     */
    public double evaluate (double[] args)
    {
        if (this.name.equals("sin"))
        {
            return Math.sin(args[0]);
        }
        else
        {
            throw new SyntaxError();
        }
    }
 
    public void increaseNumArgs ()
    {
        numArgs++;
    }
 
    public void setNumArgs (int numArgs)
    {
        this.numArgs = numArgs;
    }
 
    public int getNumArgs ()
    {
        return numArgs;
    }
 
    public String toString ()
    {
        return name;
    }
}