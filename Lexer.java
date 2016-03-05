/*
 * Kevin Jeffries
 * MiniJava compiler
 * March 2016
 */

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

abstract class Token {
	public String toString() {
		return getClass().getName();
	}
}

class Identifier extends Token {
	String id;
	Identifier(String id) {
		this.id = id;
	}
	
	public String toString() {
		return "Identifier(" + id + ")";
	}
}

class IntegerLiteral extends Token {
	int val;
	IntegerLiteral(String valString) {
		val = Integer.parseInt(valString);
	}
	
	public String toString() {
		return "IntegerLiteral(" + val + ")";
	}
}

abstract class BinOp extends Token {}
class AndOp extends BinOp {}
class LessThanOp extends BinOp {}
class PlusOp extends BinOp {}
class MinusOp extends BinOp {}
class TimesOp extends BinOp {}

class ClassToken extends Token {}
class PublicToken extends Token {}
class StaticToken extends Token {}
class VoidToken extends Token {}
class MainToken extends Token {}
class StringToken extends Token {}
class ExtendsToken extends Token {}
class ReturnToken extends Token {}
class IntToken extends Token {}
class BooleanToken extends Token {}
class IfToken extends Token {}
class ElseToken extends Token {}
class WhileToken extends Token {}
class SystemOutPrintlnToken extends Token {}
class LengthToken extends Token {}
class TrueToken extends Token {}
class FalseToken extends Token {}
class ThisToken extends Token {}
class NewToken extends Token {}

class LeftBrace extends Token {}
class RightBrace extends Token {}
class LeftParen extends Token {}
class RightParen extends Token {}
class LeftBracket extends Token {}
class RightBracket extends Token {}
class Semicolon extends Token {}
class Comma extends Token {}
class Equals extends Token {}
class Dot extends Token {}
class Bang extends Token {}

public class Lexer {
	public static List<Token> lex(Reader input) throws IOException {
		List<Token> tokens = new ArrayList<Token>();
		
		int state = 0;
		int commentNestingLevel = 0;
		boolean readNewCharacter = true;
		String currStr = "";
		char currChar = '\0';
		while(true) {
			if(readNewCharacter) {
				int c = input.read();
				if(c < 0) {
					// end of input; handle last token
					if(state == 0) {
						break; // all is well
					}
					else if(state == 1) {
						tokens.add(makeIdentifierOrKeyword(currStr));
						break;
					}
					else if(state == 2) {
						tokens.add(new IntegerLiteral(currStr));
						break;
					}
					else {
						return null; // illegal ending state
					}
				}
				currChar = (char) c;
				currStr = currStr + currChar;
			}
			readNewCharacter = true;
			
			switch(state) {
			case 0: // start state
				if(Character.isLetter(currChar)) {
					state = 1;
				}
				else if(Character.isDigit(currChar)) {
					state = 2;
				}
				else if(currChar == '&') {
					state = 3;
				}
				else if(currChar == '<') {
					tokens.add(new LessThanOp());
					currStr = "";
					state = 0;
				}
				else if(currChar == '+') {
					tokens.add(new PlusOp());
					currStr = "";
					state = 0;
				}
				else if(currChar == '-') {
					tokens.add(new MinusOp());
					currStr = "";
					state = 0;
				}
				else if(currChar == '*') {
					tokens.add(new TimesOp());
					currStr = "";
					state = 0;
				}
				else if(currChar == '{') {
					tokens.add(new LeftBrace());
					currStr = "";
					state = 0;
				}
				else if(currChar == '}') {
					tokens.add(new RightBrace());
					currStr = "";
					state = 0;
				}
				else if(currChar == '(') {
					tokens.add(new LeftParen());
					currStr = "";
					state = 0;
				}
				else if(currChar == ')') {
					tokens.add(new RightParen());
					currStr = "";
					state = 0;
				}
				else if(currChar == '[') {
					tokens.add(new LeftBracket());
					currStr = "";
					state = 0;
				}
				else if(currChar == ']') {
					tokens.add(new RightBracket());
					currStr = "";
					state = 0;
				}
				else if(currChar == ';') {
					tokens.add(new Semicolon());
					currStr = "";
					state = 0;
				}
				else if(currChar == ',') {
					tokens.add(new Comma());
					currStr = "";
					state = 0;
				}
				else if(currChar == '=') {
					tokens.add(new Equals());
					currStr = "";
					state = 0;
				}
				else if(currChar == '.') {
					tokens.add(new Dot());
					currStr = "";
					state = 0;
				}
				else if(currChar == '!') {
					tokens.add(new Bang());
					currStr = "";
					state = 0;
				}
				else if(currChar == '/') {
					state = 4;
				}
				else if(Character.isWhitespace(currChar)) {
					currStr = "";
					state = 0;
				}
				else {
					return null;
				}
				break;
			case 1: // identifier or keyword
				if(Character.isLetter(currChar) || Character.isDigit(currChar) || currChar == '_') {
					// keep growing the identifier
				}
				else {
					tokens.add(makeIdentifierOrKeyword(currStr.substring(0, currStr.length() - 1)));
					currStr = "" + currChar;
					state = 0;
					readNewCharacter = false;
				}
				break;
			case 2: // integer literal
				if(Character.isDigit(currChar)) {
					// keep growing the integer literal
				}
				else {
					tokens.add(new IntegerLiteral(currStr.substring(0, currStr.length() - 1)));
					currStr = "" + currChar;
					state = 0;
					readNewCharacter = false;
				}
				break;
			case 3: // saw first '&' of "&&"
				if(currChar == '&') {
					tokens.add(new AndOp());
					currStr = "";
					state = 0;
				}
				else {
					return null;
				}
				break;
			case 4: // saw first '/' of comment
				if(currChar == '/') {
					state = 5;
				}
				else if(currChar == '*') {
					state = 6;
					commentNestingLevel = 1;
				}
				else {
					return null;
				}
				break;
			case 5: // in a single-line comment
				if(currChar == '\n') {
					currStr = "";
					state = 0;
				}
				break;
			case 6: // in a multi-line comment
				if(currChar == '*') {
					state = 7;
				}
				if(currChar == '/') {
					state = 8; // possible start of nested comment
				}
				break;
			case 7: // in a multi-line comment, saw a star
				if(currChar == '/') {
					commentNestingLevel--;
					if(commentNestingLevel == 0) {
						currStr = "";
						state = 0;
					}
					else {
						state = 6;
					}
				}
				else {
					state = 6;
				}
				break;
			case 8: // in a multi-line comment, saw a slash that doesn't end a comment
				if(currChar == '*') {
					commentNestingLevel++;
				}
				state = 6;
				break;
			}
		}
		
		handleSystemOutPrintln(tokens);
		
		return tokens;
	}
	
	private static Token makeIdentifierOrKeyword(String s) {
		switch(s) {
		case "boolean": return new BooleanToken();
		case "class":   return new ClassToken();
		case "else":    return new ElseToken();
		case "extends": return new ExtendsToken();
		case "false":   return new FalseToken();
		case "if":      return new IfToken();
		case "int":     return new IntToken();
		case "length":  return new LengthToken();
		case "main":    return new MainToken();
		case "new":     return new NewToken();
		case "public":  return new PublicToken();
		case "return":  return new ReturnToken();
		case "static":  return new StaticToken();
		case "String":  return new StringToken();
		case "this":    return new ThisToken();
		case "true":    return new TrueToken();
		case "void":    return new VoidToken();
		case "while":   return new WhileToken();
		default:        return new Identifier(s);
		}
	}
	
	private static void handleSystemOutPrintln(List<Token> tokens) {
		for(int i = 0; i < tokens.size(); i++) {
			if(tokens.get(i) instanceof Identifier && ((Identifier)tokens.get(i)).id.equals("System")) {
				if((i + 5 <= tokens.size()) &&
				   (tokens.get(i+1) instanceof Dot) &&
				   (tokens.get(i+2) instanceof Identifier && ((Identifier)tokens.get(i+2)).id.equals("out")) &&
				   (tokens.get(i+3) instanceof Dot) &&
				   (tokens.get(i+4) instanceof Identifier && ((Identifier)tokens.get(i+4)).id.equals("println"))) {
					   
					   tokens.set(i, new SystemOutPrintlnToken());
					   tokens.remove(i+1); // remove dot
					   tokens.remove(i+1); // remove "out"
					   tokens.remove(i+1); // remove dot
					   tokens.remove(i+1); // remove "println"
					   
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		List<Token> tokens = lex(new InputStreamReader(System.in));
		if(tokens == null) {
			System.out.println("Lexer error");
		}
		else {
			for(Token token : tokens) {
				System.out.println(token);
			}
		}
	}
}
