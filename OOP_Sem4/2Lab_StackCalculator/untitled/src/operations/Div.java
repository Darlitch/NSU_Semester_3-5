package operations;

import Calc.ExecutionContext;
import Except.CalcExceptions;

import java.util.Stack;

public class Div extends Oper {
    public double doOper(Object[] args) throws CalcExceptions {
//        if (args.length != 2) {
//            throw new CalcExceptions("Error: invalid count of args");
//        }
//        double arg1 = (double)args[0];
//        double arg2 = (double)args[1];
//        if (arg1 == 0) {
//            throw new CalcExceptions("Error: division by zero");
//        }
//        return arg1 / arg2;
        if (args.length != 1) {
            throw new CalcExceptions("Error: invalid count of argv");
        }
        ExecutionContext ec = (ExecutionContext)args[0];
        Stack<Double> stack = ec.getStack();
        if (stack.size() < 2) {
            throw new CalcExceptions("Error: There are not enough elements in the stack to perform the operation");
        }
        double arg1 = stack.pop();
        double arg2 = stack.pop();
        return arg2 / arg1;
    }
}
