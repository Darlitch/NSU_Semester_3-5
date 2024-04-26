package operations;

import Except.CalcExceptions;

import java.util.Stack;

public class Push extends Oper {
    public double doOper(Object[] args) throws CalcExceptions {
        if (args.length != 2) {
            throw new CalcExceptions("Error: invalid count of args");
        }
        Stack<Double> stack = (Stack<Double>)args[0];
        double val = (double)args[1];
        stack.push(val);
        return 0;
    }
}
