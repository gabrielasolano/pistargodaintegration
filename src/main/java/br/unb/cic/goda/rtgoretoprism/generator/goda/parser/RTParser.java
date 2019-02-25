package br.unb.cic.goda.rtgoretoprism.generator.goda.parser;

import br.unb.cic.RTRegexBaseVisitor;
import br.unb.cic.RTRegexLexer;
import br.unb.cic.RTRegexParser;
import br.unb.cic.RTRegexParser.*;
import br.unb.cic.goda.rtgoretoprism.model.kl.Const;
import br.unb.cic.goda.rtgoretoprism.generator.goda.parser.CustomRTRegexVisitor;
import br.unb.cic.goda.rtgoretoprism.generator.goda.parser.ThrowingErrorListener;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RTParser {

	public static Object[] parseRegex(String uid, String regex, Const decType, boolean param) throws IOException{
		//Reading the DSL script
		InputStream is = new ByteArrayInputStream(regex.getBytes("UTF-8"));

		//Loading the DSL script into the ANTLR stream.
		CharStream cs = new ANTLRInputStream(is);

		//Passing the input to the lexer to create tokens
		RTRegexLexer lexer = new RTRegexLexer(cs);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		//Passing the tokens to the parser to create the parse trea. 
		RTRegexParser parser = new RTRegexParser(tokens);
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

		ParseTree tree = parser.rt();
		CustomRTRegexVisitor rtRegexVisitor = new CustomRTRegexVisitor(uid, decType, param);
		rtRegexVisitor.visit(tree);

		return new Object [] 	{rtRegexVisitor.reliabilityFormula,
				rtRegexVisitor.costFormula,
				rtRegexVisitor.decisionMemory};
    }
}

class CustomRTRegexVisitor extends  RTRegexBaseVisitor<String> {

	final String uid;
	final Const decType;
	final boolean param;
	String reliabilityFormula = new String();
	String costFormula = new String();	
	List<String> decisionMemory = new ArrayList<String>();

	public CustomRTRegexVisitor(String uid, Const decType, boolean param) {
		this.decType = decType;
		this.param = param;

		if (uid.contains("_")) {
			this.uid = uid.substring(0, uid.indexOf('_'));
		}
		else {
			this.uid = uid;
		}
	}

	@Override
	public String visitPrintExpr(PrintExprContext ctx) {
		visit(ctx.expr());		
		return "Goals sorted";
	}
	
	@Override
	public String visitGId(GIdContext ctx) {
		String gid = ctx.t.getText() + ctx.id().getText();
		if(ctx.t.getType() == RTRegexParser.TASK) {
			gid = uid + '_' + gid;
		}
		return gid;
	}
	
	@Override
	public String visitGDM(GDMContext ctx) {
		String gidAo = super.visit(ctx.expr(0));
		String gidBo = super.visit(ctx.expr(1));
		
		String [] gidAs = gidAo.split("-");
		String [] gidBs = gidBo.split("-");		
		String [] gids = new String[gidAs.length + gidBs.length];
		int i = 0;
		for(String gidB : gidBs){
			for(String gidA : gidAs){
				if(!decisionMemory.contains(gidA))
					decisionMemory.add(gidA);
				gids[i] = gidA.replaceAll("\\.", "_");
				i++;
			}
			if(!decisionMemory.contains(gidB))
				decisionMemory.add(gidB);
			gids[i] = gidB.replaceAll("\\.", "_");
			i++;
		}
		return gidAo + '-' + gidBo;
	}
}