package br.unb.cic.goda.rtgoretoprism.generator.goda.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import br.unb.cic.CostRegexBaseVisitor;
import br.unb.cic.CostRegexLexer;
import br.unb.cic.CostRegexParser;
import br.unb.cic.CostRegexParser.GExpressionContext;
import br.unb.cic.CostRegexParser.GFloatContext;
import br.unb.cic.CostRegexParser.GVariableContext;


public class CostParser{

	public static Object[] parseRegex(String regex) throws IOException{
		//Reading the DSL script
		InputStream is = new ByteArrayInputStream(regex.getBytes("UTF-8"));

		//Loading the DSL script into the ANTLR stream.
		CharStream cs = new ANTLRInputStream(is);

		//Passing the input to the lexer to create tokens
		CostRegexLexer lexer = new CostRegexLexer(cs);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		//Passing the tokens to the parser to create the parse trea. 
		CostRegexParser parser = new CostRegexParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);

		//Semantic model to be populated
		//Graph g = new Graph();

		//Adding the listener to facilitate walking through parse tree. 
		//parser.addParseListener(new MyRTRegexBaseListener());

		//invoking the parser. 
		//parser.prog();

		//Graph.printGraph(g);

		//ParseTreeWalker walker = new ParseTreeWalker();
		//walker.walk(new MyRTRegexBaseListener(), parser.prog());

		ParseTree tree = parser.cost();
		CostRegexVisitor costRegexVisitor = new CostRegexVisitor();
		costRegexVisitor.visit(tree);

		return new Object [] {costRegexVisitor.costValue, costRegexVisitor.costVariable, costRegexVisitor.costFormula};
	}
}

class CostRegexVisitor extends  CostRegexBaseVisitor<String> {
	String costValue = null;
	String costVariable = null;
	String costFormula = null;

	@Override
	public String visitGVariable(GVariableContext ctx) {
		costValue = "1";
		costVariable = ctx.VAR().getText();

		costFormula = costValue + "*" + costVariable;
		return costFormula;
	}

	@Override
	public String visitGFloat(GFloatContext ctx) {
		costValue = ctx.FLOAT().getText();
		costFormula = costValue;
		return costFormula;
	}

	@Override
	public String visitGExpression(GExpressionContext ctx) {
		costValue = ctx.FLOAT().getText();
		costVariable = ctx.VAR().getText();

		costFormula = costValue + "*" + costVariable;
		return costFormula;
	}

}