package net.objecthunter.exp4j.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.tokens.FunctionToken;
import net.objecthunter.exp4j.tokens.NumberToken;
import net.objecthunter.exp4j.tokens.OperatorToken;
import net.objecthunter.exp4j.tokens.Token;
import net.objecthunter.exp4j.tokens.VariableToken;

public class DoubleExpression extends Expression<Double> {
	final Map<String, Double> variables = new HashMap<String, Double>();
	
	public DoubleExpression(String expression, List<Token> tokens) {
		super(expression, tokens);
	}
	
	@Override
	public Expression setVariable(String name, Double value) {
		variables.put(name, value);
		return this;
	}
	
	@Override
	public Expression setVariables(Map<String, Double> variables) {
		this.variables.putAll(variables);
		return this;
	}

	@Override
	public Double evaluate() {
		return evaluate(null);
	}

	@Override
	public Double evaluate(Map<String, Double> variables) {
		if (variables != null) {
			this.setVariables(variables);
		}
		final Stack<Double> stack = new Stack<>();
		for (Token t : tokens) {
			switch (t.getType()) {
			case Token.NUMBER:
				stack.push((Double) ((NumberToken) t).getValue());
				break;
			case Token.VARIABLE:
				stack.push(this.variables.get(((VariableToken) t).getName()));
				break;
			case Token.OPERATOR:
				Operator<Double> op = ((OperatorToken) t).getOperator();
				if (stack.size() < op.getArgumentCount()) {
					throw new IllegalArgumentException(
							"Not enough operands for operator "
									+ op.getSymbol());
				}
				Double[] operands = new Double[op.getArgumentCount()];
				for (int i = 0; i < op.getArgumentCount(); i++) {
					operands[i] = stack.pop();
				}
				stack.push(op.apply(operands));
				break;
			case Token.FUNCTION:
				final Function<Double> func = ((FunctionToken) t).getFunction();
				Double[] args;
				if (func.getArgumentCount() > 0) {
					if (stack.size() < func.getArgumentCount()) {
						throw new IllegalArgumentException(
								"Not enough operands for function "
										+ func.getName());
					}
					args = new Double[func.getArgumentCount()];
					for (int i = 0; i < func.getArgumentCount(); i++) {
						args[i] = stack.pop();
					}
				} else {
					args = new Double[stack.size()];
					int count = 0;
					while (!stack.isEmpty()) {
						args[count++] = stack.pop();
					}
				}
				stack.push(func.apply(args));
				break;
			}
		}
		if (stack.size() != 1) {
			throw new IllegalArgumentException(
					"The user has input too many values");
		}
		return stack.pop();
	}
}
