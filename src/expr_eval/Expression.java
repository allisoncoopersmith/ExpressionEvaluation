package expr_eval;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structure.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                

	/**
	 * Populates the scalars list with simple scalar variables
	 * Scalar characters in the expression 
	 */
	ArrayList<ScalarVariable> scalars;

	/**
	 * Populates the arrays list with simple array variables
	 * Array characters in the expression
	 */
	ArrayList<ArrayVariable> arrays;

	/**
	 * String containing all delimiters (characters other than variables and constants), 
	 * to be used with StringTokenizer
	 */
	public static final String delims = " \t*+-/()[]";

	/**
	 * Initializes this Expression object with an input expression. Sets all other
	 * fields to null.
	 * 
	 * @param expr Expression
	 */
	public Expression(String expr) {
		this.expr = expr;
	}

	/**
	 * Populates the scalars and arrays lists with characters for scalar and array
	 * variables in the expression. For every variable, a SINGLE character is created and stored,
	 * even if it appears more than once in the expression.
	 * At this time, values for all variables are set to
	 * zero - they will be loaded from a file in the loadVariableValues method.
	 */
	private boolean findDuplicate (String name) {

		for(int i = 0; i < arrays.size(); i++) {
			if (arrays.get(i).name.equals(name)) {
				return false;
			}
		}
		for (int x=0; x<scalars.size(); x++) {
			if (scalars.get(x).name.equals(name)) {
				return false;
			}
		}
		return true;
	}
	private boolean checkIfNumber (String name) {
		int count=0;

		for (int x=0; x<name.length(); x++) {
			if (Character.isDigit(name.charAt(x))) {
				count++;
			}
		}
		if (count == name.length()) {
			return false;
		}
		return true;
	}
	public void buildVariable() {
		scalars=new ArrayList<ScalarVariable>();
		arrays=new ArrayList<ArrayVariable>(); 
				
				
		String noSpace = "";
		for (int x=0; x<expr.length(); x++) { //gets rid of spaces
			if (expr.charAt(x) != ' ' && expr.charAt(x) != '\t') {
				noSpace += expr.charAt(x);
			}


		}
		String newName = "";
		for (int y=0; y<noSpace.length(); y++) {
			if (noSpace.charAt(y)=='[') {
				if (!newName.equals("")) {
					if (findDuplicate(newName) && (checkIfNumber(newName))) {

						ArrayVariable newArray = new ArrayVariable(newName);
						arrays.add(newArray);
					}
					newName="";
				}

			}
			else if (delims.indexOf(noSpace.charAt(y)) >= 0) {
				if (!newName.equals("")) {
					if (findDuplicate(newName) && (checkIfNumber(newName))) {
						ScalarVariable newScalar = new ScalarVariable(newName);
						scalars.add(newScalar); 
					}
					newName="";
				}
			}
			else {
				newName += noSpace.charAt(y);
			}


		}
		if (noSpace.charAt(noSpace.length()-1) != ']') { //takes care of last case
			if (!newName.equals("")) {
				if (findDuplicate(newName) && (checkIfNumber(newName))) {
					ScalarVariable newScalar = new ScalarVariable(newName);
					scalars.add(newScalar); 
				}
			}
		}

	}





	/**
	 * Loads values for scalars and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input 
	 */
	public void loadVariableValues(Scanner sc) 
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String varl = st.nextToken();
			ScalarVariable scal = new ScalarVariable(varl);
			ArrayVariable arr = new ArrayVariable(varl);
			int scali = scalars.indexOf(scal);
			int arri = arrays.indexOf(arr);
			if (scali == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar character
				scalars.get(scali).value = num;
			} else { // array character
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,value) pairs
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok," (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;              
				}
			}
		}
	}


	/**
	 * Evaluates the expression, and can use RECURSION to evaluate subexpressions and to evaluate array 
	 * subscript expressions.
	 * 
	 * @param scalars The scalar array list, with values for all scalar items
	 * @param arrays The array array list, with values for all array items
	 * 
	 * @return Result of evaluation
	 */

	public double evaluate() {		
		String noSpace = "";
		Stack<Double> numberStack = new Stack<Double>();
		Stack<Character> operatorStack = new Stack<Character>();
		Stack<String> arrayStack = new Stack<String>();

		for (int x=0; x<expr.length(); x++) { //gets rid of spaces
			if (expr.charAt(x) != ' ' && expr.charAt(x) != '\t') {
				noSpace += expr.charAt(x);
			}
		}
		
		
		while (noSpace.length() != 0) {
			if ((Character.isDigit(noSpace.charAt(0)) &&  (noSpace.length() == 1 || !Character.isDigit(noSpace.charAt(1))))) { //one character number
				char x = noSpace.charAt(0);
				double number = x-'0';
				numberStack.push(number);
				noSpace = noSpace.substring(1);

			}


			else if ((Character.isDigit(noSpace.charAt(0)) && (noSpace.length() > 1 && Character.isDigit(noSpace.charAt(1))))) { //multi digit number
				int x=0;
				String numberAppender= "";
				while (x < noSpace.length() && Character.isDigit(noSpace.charAt(x))) {
					numberAppender += noSpace.charAt(x);
					x++;
				}
				int number = Integer.parseInt(numberAppender);
				Double num = (double) number;
				numberStack.push(num);
				noSpace= noSpace.substring(x);
			}



			else if (Character.isLetter(noSpace.charAt(0)) &&  (noSpace.length() == 1 || !Character.isLetter(noSpace.charAt(1)))) { //one letter variable
				if (noSpace.length() != 1 && noSpace.charAt(1)== '['){ //for one letter array variables
					arrayStack.push(noSpace.substring(0,1));

				}
				else  {
				String name= "";              //for one letter scalar variables
				char x = noSpace.charAt(0);
				name += x;
				int number = findValue(name);
				double num = (double) number;
				numberStack.push(num);
				}
				noSpace=noSpace.substring(1);

			}
			else if ((Character.isLetter(noSpace.charAt(0)) && (noSpace.length() > 1 && Character.isLetter(noSpace.charAt(1))))) { //multi letter variable
				int x=0;
				String letterAppender= "";
				while (x < noSpace.length() && Character.isLetter(noSpace.charAt(x))) {
					letterAppender += noSpace.charAt(x);
					x++;
				}
				if (x != noSpace.length() && noSpace.charAt(x)== '[') { //for multi letter array variables
					arrayStack.push(letterAppender);
				}

				else {
					int number = findValue(letterAppender); //for one letter scalar variable
					double num = (double) number;
					numberStack.push(num);
				
				}
				noSpace=noSpace.substring(x);
			}

			else if (noSpace.charAt(0)=='(') { //left parenthesis
				operatorStack.push('(');
				noSpace=noSpace.substring(1);
			}

			else if (noSpace.charAt(0)==')') {

				while (operatorStack.peek() != '(') { //get 2 numbers and an operator, do the math and push the result to the stack. do that til you hit the end of the parenths
					double b = numberStack.pop();
					double a = numberStack.pop();
					char op = operatorStack.pop();
					double result = mathPerformer(a, b, op);
					numberStack.push(result);
				}
				operatorStack.pop();

				noSpace=noSpace.substring(1);	
			}
			else if (noSpace.charAt(0)=='[') {
				operatorStack.push('[');
				noSpace=noSpace.substring(1);
			}
			else if (noSpace.charAt(0)==']') {
				while (operatorStack.peek()!= '[') {
					double b =numberStack.pop();
					double a =numberStack.pop();
					char op = operatorStack.pop();
					int result = (int) mathPerformer(a, b, op);
					double roundResult = (double) result;
					numberStack.push(roundResult);
				}
				String varName = arrayStack.pop();	
				double dIndex =  numberStack.pop();
				int index = (int) dIndex;
				operatorStack.pop();
				noSpace = noSpace.substring(1);
				int arrayValue = findArrayValue (index, varName);
				double arrayDouble = (double) arrayValue;
				numberStack.push(arrayDouble);
				
				
			}



			else if (noSpace.charAt(0)=='+' || noSpace.charAt(0)== '-') { //for add and subtract
				while (!operatorStack.isEmpty() && (operatorStack.peek() == '+' || operatorStack.peek() == '-'|| operatorStack.peek() == '*' || operatorStack.peek() == '/')) {
					double b = numberStack.pop();
					double a = numberStack.pop();
					char op = operatorStack.pop();
					double result = mathPerformer(a, b, op);
					numberStack.push(result);
				}
				operatorStack.push(noSpace.charAt(0));
				noSpace=noSpace.substring(1);
			}
			else if (noSpace.charAt(0)=='*' || noSpace.charAt(0)== '/') { //for multiply and divide
				while (!operatorStack.isEmpty() && (operatorStack.peek() == '*' || operatorStack.peek() == '/')) {
					double b = numberStack.pop();
					double a = numberStack.pop();
					char op = operatorStack.pop();
					double result = mathPerformer(a, b, op);
					numberStack.push(result);
				}
				operatorStack.push(noSpace.charAt(0));
				noSpace=noSpace.substring(1);


			}


		}

		while (!operatorStack.isEmpty()) {
			double b = numberStack.pop();
			double a = numberStack.pop();
			char op = operatorStack.pop();
			double result = mathPerformer(a, b, op);
			numberStack.push(result);

		}
		return numberStack.pop();
	}
	private double mathPerformer (double a, double b, char op) {
		if (op=='+') {
			return a+b;
		}
		if (op=='-') {
			return a-b;
		}
		if (op=='/') {
			return a/b;
		}
		return a*b;
	}

	private int findValue (String name) {
		for (int x=0; x<scalars.size(); x++) {
			if (scalars.get(x).name.equals(name)) {
				return scalars.get(x).value;
			}
		}
		return -1;
	}
	
	private int findArrayValue (int a, String name) {
		int k=-1;
		
		for (int x=0; x<arrays.size(); x++) {
			if (arrays.get(x).name.equals(name)) {
				k= arrays.get(x).values[a];
				
			}
		}
		 return k;
		 
	}

	/**
	 * Utility method, prints the characters in the scalars list
	 */
	public void printScalars() {
		for (ScalarVariable ss: scalars) {
			System.out.println(ss);
		}
	}

	/**
	 * Utility method, prints the characters in the arrays list
	 */
	public void printArrays() {
		for (ArrayVariable as: arrays) {
			System.out.println(as);
		}
	}

}

