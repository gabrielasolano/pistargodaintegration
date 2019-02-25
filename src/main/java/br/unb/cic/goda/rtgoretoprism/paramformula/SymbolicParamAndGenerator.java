package br.unb.cic.goda.rtgoretoprism.paramformula;

import java.util.Map;

public class SymbolicParamAndGenerator {

	public String getSequentialAndCost (String[] nodes) {
		
		StringBuilder reliability = getReliability(nodes);
		
		StringBuilder formula = new StringBuilder();
		formula.append("(");
		for (String node : nodes) {
			formula.append(reliability + " " + node + " +");
		}
		formula.deleteCharAt(formula.length()-1);
		formula.append(" )");
		return formula.toString();
	}

	public String getParallelAndCost(String[] nodes) {
	
		int numNodes = nodes.length;
		
		StringBuilder reliability = getReliability(nodes);
		StringBuilder formula = new StringBuilder();
		formula.append("(");
		
		for (String node : nodes) {
			//Number even of nodes
			if ((numNodes % 2) == 0) {
				if (formula.length() == 1) formula.append("(");
				formula.append(" " + Integer.toString(numNodes+1) + " *" + reliability + " " + node + " +");
			}
			//Number odd of nodes
			else {
				formula.append(" 2 *" + reliability + " " + node + " +");
			}	
		}
		
		formula.deleteCharAt(formula.length()-1);
		formula.append(")");
		if ((numNodes % 2) == 0) formula.append("/2)");
		return formula.toString();
	}

	private StringBuilder getReliability(String[] nodes) {
		StringBuilder reliability = new StringBuilder();
		for (String node : nodes) {
			reliability.append(" R_" + node + " *");
		}
		return reliability;
	}
}
