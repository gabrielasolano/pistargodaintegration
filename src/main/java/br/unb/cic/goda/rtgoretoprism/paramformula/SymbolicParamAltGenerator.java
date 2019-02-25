package br.unb.cic.goda.rtgoretoprism.paramformula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolicParamAltGenerator {

	public String getAlternativeFormula (String[] nodes, boolean reliability) {
		String xor[] = new String[nodes.length];

		Map<String,String> list = generateList(nodes, xor);

		String param = generateCombinations(list, xor, reliability);

		return param;
	}

	public static String generateCombinations (Map<String, String> list, String[] xors, boolean reliability) {

		String formula = new String();
		formula = "(";

		int n = xors.length; 
		for(int num = 0;num < (1 << n);num++) { 
			List<String> combination = new ArrayList<>(); 
			for(int ndx = 0;ndx < n;ndx++) { 
				// (is the bit "on" in this number?) 
				if((num & (1 << ndx)) != 0) { 
					// then it's included in the list 
					combination.add(xors[ndx]); 
				} 
			} 

			if (combination.size() > 0) {
				formula = formula + printFormula(list, combination, reliability);
			}
		}
		formula = formula.replaceFirst(" \\+ ", " ");
		return formula + " )";
	}

	private static String printFormula(Map<String, String> list, List<String> combination, boolean isReliability) {

		String[] nodes = new String[combination.size()];
		String all_xors = new String();

		int index = 0;
		for(String elem : combination) {
			if (all_xors.length() == 0) {
				all_xors = elem;
			}
			else {
				all_xors = all_xors.concat(" * " + elem);
			}
			nodes[index] = list.get(elem);
			index++;
		}

		int total = nodes.length;
		StringBuilder reliability = new StringBuilder();
		StringBuilder cost = new StringBuilder();
		for (int i = 0; i < total; i++) {
			if (total%2 != 0) {
				//Odd: +
				reliability.append(" + " + all_xors + " * " + nodes[i]);
				cost.append(" + " + all_xors + " * R_" + nodes[i] + " * " + nodes[i]);
			}
			else {
				//Even: -
				reliability.append(" - " + all_xors + " * " + nodes[i]);
				cost.append(" - " + all_xors + " * R_" + nodes[i] + " * " + nodes[i]);
			}
		}
		
		if (isReliability) return reliability.toString();
		return cost.toString();
	}

	private static Map<String, String> generateList(String[] nodes, String[] xor_list) {

		Map<String,String> list = new HashMap<String,String>();
		for (int i = 0; i < nodes.length; i++) {
			String xor = "XOR_" + nodes[i];
			xor_list[i] = xor;
			list.put(xor, nodes[i]);
		}
		return list;
	}
	
	/*private static String[] generateList(String[] nodes) {

		String[] list = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			String xor = "XOR_" + nodes[i];
			list[i] = xor;
		}
		return list;
	}
	
	//Generation of cost formula for alternative annotation.
	public String getCostAlternativeFormula (String[] nodes) {
		
		String xor[] = generateList(nodes);
        List<String[]> list = new ArrayList<String[]>();

        GenerateCombination comb1 = new GenerateCombination(xor, 2) ;
        while (comb1.hasNext()) {
        	list.add(comb1.next());
        }

		StringBuilder param = new StringBuilder();
		
		for (String node : nodes) {
			if (param.length() == 0) {
				param.append("(( 2 * " + node + " * XOR_" + node);
			}
			else {
				param.append(" + 2 * " + node + " * XOR_" + node);
			}
		}
		for (String node : nodes) {
			for (String[] comb : list) {
				if (comb[0].contains(node) || comb[1].contains(node)) {
					param.append(" + 2 * " + node + " * " + comb[0] + " * " + comb[1]);
					String otherNode = getOtherNode(comb, node);
					param.append(" - " + node + " * R_" + otherNode + " * " + comb[0] + " * " + comb[1]);
				}
			}
		}
		
		param.append(" ) / 2)");
		return param.toString();
	}

	private String getOtherNode(String[] comb, String node) {
		
		if (comb[0].contains(node)) return comb[1].substring(4, comb[1].length());
		
		return comb[0].substring(4, comb[0].length());
	}*/
	
}