package br.unb.cic.goda.rtgoretoprism.generator.goda.producer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.unb.cic.goda.model.Actor;
import br.unb.cic.goda.model.Goal;
import br.unb.cic.goda.rtgoretoprism.generator.CodeGenerationException;
import br.unb.cic.goda.rtgoretoprism.generator.goda.parser.CostParser;
import br.unb.cic.goda.rtgoretoprism.generator.goda.writer.ManageWriter;
import br.unb.cic.goda.rtgoretoprism.generator.goda.writer.ParamWriter;
import br.unb.cic.goda.rtgoretoprism.generator.kl.AgentDefinition;
import br.unb.cic.goda.rtgoretoprism.model.kl.Const;
import br.unb.cic.goda.rtgoretoprism.model.kl.GoalContainer;
import br.unb.cic.goda.rtgoretoprism.model.kl.PlanContainer;
import br.unb.cic.goda.rtgoretoprism.model.kl.RTContainer;
import br.unb.cic.goda.rtgoretoprism.paramformula.SymbolicParamAndGenerator;
import br.unb.cic.goda.rtgoretoprism.paramwrapper.ParamWrapper;

public class PARAMProducer {

	private String sourceFolder;
	private String targetFolder;
	private String toolsFolder;
	private Set<Actor> allActors;
	private Set<Goal> allGoals;
	private AgentDefinition ad;

	private String agentName;
	private List<String> leavesId = new ArrayList<String>();
	private Map<String,String> ctxInformation = new HashMap<String,String>();
	private List<String> varReliabilityInformation = new ArrayList<String>();
	private List<String> varCostInformation = new ArrayList<String>();
	private Map<String,String> reliabilityByNode = new HashMap<String,String>();

	public PARAMProducer(Set<Actor> allActors, Set<Goal> allGoals, String in, String out, String tools) {

		this.sourceFolder = in;
		this.targetFolder = out;
		this.toolsFolder = tools;
		this.allActors = allActors;
		this.allGoals = allGoals;
	}

	public PARAMProducer(AgentDefinition ad, Set<Actor> selectedActors, Set<Goal> selectedGoals,
			String sourceFolder, String targetFolder, String toolsFolder) {
		this.sourceFolder = sourceFolder;
		this.targetFolder = targetFolder;
		this.toolsFolder = toolsFolder;
		this.allActors = selectedActors;
		this.allGoals = selectedGoals;
		this.ad = ad;
		this.agentName = "EvaluationActor";
	}

	public void run() throws CodeGenerationException, IOException {

		for(Actor actor : allActors){

			long startTime = 0;
			
			if (this.ad == null) {
				RTGoreProducer producer = new RTGoreProducer(allActors, allGoals, sourceFolder, targetFolder);
				AgentDefinition ad = producer.run();

				this.ad = ad;
				agentName = ad.getAgentName();
			}

			System.out.println("Generating PARAM formulas for: " + agentName);

			// Compose goal formula
			startTime = new Date().getTime();
			String reliabilityForm = composeNodeForm(ad.rootlist.getFirst(), true);
			String costForm = composeNodeForm(ad.rootlist.getFirst(), false);

			reliabilityForm = cleanNodeForm(reliabilityForm, true);
			costForm = cleanNodeForm(costForm, false);

			//Print formula
			printFormula(reliabilityForm, costForm);
			System.out.println( "PARAM formulas created in " + (new Date().getTime() - startTime) + "ms.");
		}
	}

	private String cleanNodeForm(String nodeForm, boolean reliability) {
		
		if (!reliability) {
			nodeForm = replaceReliabilites(nodeForm);
			nodeForm = cleanMultipleContexts(nodeForm);
		}
		
		nodeForm = nodeForm.replaceAll("\\s+", "");
		return nodeForm;
	}

	private String cleanMultipleContexts(String nodeForm) {
		
		String[] plusSignalSplit = nodeForm.split("\\+");
		
		for (String exp1 : plusSignalSplit) {
			String[] minusSignalSplit = exp1.split("-");
			for (String exp2 : minusSignalSplit) {
				String aux = exp2.replaceAll("\\(","");
				aux = aux.replaceAll("\\)","");
				aux = aux.replaceAll("\\s+", "");
				if (!aux.equals("1") && !aux.equals("")) {
					String[] multSignalSplit = exp2.split("\\*");
					nodeForm = replaceCtxRepetition(nodeForm, multSignalSplit);
				}
			}
		}
		
		return nodeForm;
	}

	private String replaceCtxRepetition(String nodeForm, String[] multSignalSplit) {
		Set<String> lump = new HashSet<String>();
		String withoutRepetition = new String();
		String withRepetition = new String();
		 
		for (String i : multSignalSplit) {
			if (i.startsWith(" (")) i = i.substring(1, i.length());
			
			i = i.replaceAll("\\(", "");
			i = i.replaceAll("\\)", "");

			if (withRepetition.isEmpty()) withRepetition = i;
			else withRepetition = withRepetition + "\\*" + i;

			i = i.replaceAll("\\s+", "");

			if (!lump.contains(i)) {
				lump.add(i);
				if (!i.equals("1")) {
					if (withoutRepetition.isEmpty()) withoutRepetition = i;
					else withoutRepetition = withoutRepetition + "*" + i;
				}
		    }
		}
		
		nodeForm = nodeForm.replaceAll(withRepetition, withoutRepetition);
		return nodeForm;
	}

	private String replaceReliabilites(String nodeForm) {
		if (nodeForm.contains(" R_")) {
			for (Map.Entry<String, String> entry : this.reliabilityByNode.entrySet()){
				String reliability = entry.getValue();
				String id = entry.getKey();
				nodeForm = nodeForm.replaceAll(" R_" + id + " ", " " + reliability + " ");
			}
		}
		return nodeForm;
	}

	private void printFormula(String reliabilityForm, String costForm) throws CodeGenerationException {

		reliabilityForm = composeFormula(reliabilityForm, true);
		costForm = composeFormula(costForm, false);

		String output = targetFolder + "/";
		
		PrintWriter reliabiltyFormula = ManageWriter.createFile("reliability.out", output);
		PrintWriter costFormula = ManageWriter.createFile("cost.out", output);
		
		ManageWriter.printModel(reliabiltyFormula, reliabilityForm);
		ManageWriter.printModel(costFormula, costForm);
	}

	private String composeFormula(String nodeForm, boolean isReliability) throws CodeGenerationException {

		String body = nodeForm + "\n\n";
		for (String ctxKey : ctxInformation.keySet()) {

			body = body + "//" + ctxKey + " = " + ctxInformation.get(ctxKey) + "\n";
		}
		for (String var : this.varReliabilityInformation) {
			body = body + var;
		}
		if (!isReliability) {
			for (String var : this.varCostInformation) {
				body = body + var;
			}
		}

		return body;
	}

	//true: compose reliability, false: compose cost
	private String composeNodeForm(RTContainer rootNode, boolean reliability) throws IOException, CodeGenerationException {

		Const decType;
		String rtAnnot;
		String nodeForm;
		String nodeId;
		List<String> ctxAnnot = new ArrayList<String>();
		LinkedList<GoalContainer> decompGoal = new LinkedList<GoalContainer>();
		LinkedList<PlanContainer> decompPlans = new LinkedList<PlanContainer>();

		if(rootNode instanceof GoalContainer) nodeId = rootNode.getClearUId();
		else nodeId = rootNode.getClearElId();

		decompGoal = rootNode.getDecompGoals();
		decompPlans = rootNode.getDecompPlans();
		decType = rootNode.getDecomposition();
		rtAnnot = rootNode.getRtRegex();
		ctxAnnot = rootNode.getFulfillmentConditions();

		//nodeForm = getNodeForm(decType, rtAnnot, nodeId, reliability);
		nodeForm = getNodeForm(decType, rtAnnot, nodeId, reliability, rootNode);
		
		/*Run for sub goals*/
		for (GoalContainer subNode : decompGoal) {
			String subNodeId = subNode.getClearUId();
			String subNodeForm = composeNodeForm(subNode, reliability);
			nodeForm = replaceSubForm(nodeForm, subNodeForm, nodeId, subNodeId);
		}

		/*Run for sub tasks*/
		for (PlanContainer subNode : decompPlans) {
			String subNodeId = subNode.getClearElId();
			String subNodeForm = composeNodeForm(subNode, reliability);
			nodeForm = replaceSubForm(nodeForm, subNodeForm, nodeId, subNodeId);
		}

		/*If leaf task*/
		if ((decompGoal.size() == 0) && (decompPlans.size() == 0)) {

			this.leavesId.add(nodeId);

			if (reliability) {
				//Create DTMC model (param)
				ParamWriter writer = new ParamWriter(sourceFolder, nodeId);
				String model = writer.writeModel();

				//Call to param (reliability)
				ParamWrapper paramWrapper = new ParamWrapper(toolsFolder, nodeId);
				nodeForm = paramWrapper.getFormula(model);
				nodeForm = nodeForm.replaceFirst("1\\*", "");
				
				this.varReliabilityInformation.add("//R_" + nodeId + " = reliability of node " + nodeId + "\n");
				this.varReliabilityInformation.add("//F_" + nodeId + " = frequency of node " + nodeId + "\n");
				if (rootNode.isOptional()) {
					nodeForm += "*OPT_" + nodeId;
					this.varReliabilityInformation.add("//OPT_" + nodeId + " = optionality of node " + nodeId + "\n");	
				}
			}
			else {
				//Cost
				nodeForm = getCostFormula(rootNode);
				this.varCostInformation.add("//" + nodeForm + " = cost of node " + nodeId + "\n");
			}

			if (!ctxAnnot.isEmpty()) {
				nodeForm = insertCtxAnnotation(nodeForm, ctxAnnot, rootNode);
			}	
		}
		if (reliability) this.reliabilityByNode.put(nodeId, nodeForm);

		return nodeForm;
	}

	private String getCostFormula(RTContainer rootNode) throws IOException {
		PlanContainer plan = (PlanContainer) rootNode;
	
		if (plan.getCostRegex() != null) {
			Object [] res = CostParser.parseRegex(plan.getCostRegex());
			return (String) res[2];
		}
		
		return "W_"+rootNode.getClearElId();
	}

	private String insertCtxAnnotation(String nodeForm, List<String> ctxAnnot, RTContainer rootNode) {

		List<String> cleanCtx = clearCtxList(ctxAnnot);

		//Check if context if from non-deterministic node
		String contextId = getContextId(rootNode);
		
		//String ctxParamId = "CTX_" + nodeId;
		String ctxParamId = "CTX_" + contextId;
		nodeForm = ctxParamId + "*" + nodeForm;

		String ctxConcat = new String();
		for (String ctx : cleanCtx) {
			if (ctxConcat.length() == 0) {
				ctxConcat = "(" + ctx + ")";
			}
			else {
				ctxConcat = ctxConcat.concat(" & (" + ctx + ")");
			}
		}

		ctxInformation.put(ctxParamId, ctxConcat);

		return nodeForm;
	}

	private String getContextId(RTContainer node) {		
		RTContainer root = node.getRoot();
		RTContainer child = node;
		while (root != null) {
			if (root.isDecisionMaking()) {
				if(child instanceof GoalContainer) return child.getClearUId();
				else return child.getClearElId();
			}
			child = root;
			root = root.getRoot();
		}
		if(node instanceof GoalContainer) return node.getClearUId();
		else return node.getClearElId();
	}

	private List<String> clearCtxList(List<String> ctxAnnot) {

		List<String> clearCtx = new ArrayList<String>();
		for (String ctx : ctxAnnot) {
			String[] aux;
			if (ctx.contains("assertion condition")) {
				aux = ctx.split("^assertion condition\\s*");
			}
			else {
				aux = ctx.split("^assertion trigger\\s*");
			}
			clearCtx.add(aux[1]);
		}

		return clearCtx;
	}

	private String replaceSubForm(String nodeForm, String subNodeForm, String nodeId, String subNodeId) {

		if (nodeForm.equals(nodeId)) {
			nodeForm = subNodeForm;
		}
		else {
			subNodeId = restricToString(subNodeId);
			subNodeForm = restricToString(subNodeForm);
			nodeForm = nodeForm.replaceAll(subNodeId, subNodeForm);
		}
		
		if (subNodeForm.contains("CTX") && nodeForm.contains("XOR")) {
			for (Map.Entry<String, String> entry : ctxInformation.entrySet())
			{
				if (entry.getKey().contains(subNodeId.trim())) {
					nodeForm = nodeForm.replaceAll("XOR_" + subNodeId.trim(), entry.getKey());
					return nodeForm;
				}
			}
		}
		return nodeForm;
	}

	private String restricToString(String subNodeString) {
		return " " + subNodeString + " ";
	}
	
	//Get node form for AND/OR-decompositions and DM annotation only
	private String getNodeForm(Const decType, String rtAnnot, String uid, boolean reliability, RTContainer rootNode) throws IOException {
		
		List<String> childrenNodes = getChildrenId(rootNode);
		String formula = new String();
		if (rtAnnot == null) {
			if (childrenNodes.size() <= 1) return uid;
		
			if (decType.equals(Const.AND)) {
				if (reliability) {
					formula = "( ";
					for (String id : childrenNodes) {
						formula += id + " * ";
					}
					formula = formula.substring(0, formula.length()-2);
					formula += " )";
				}
				else {
					SymbolicParamAndGenerator param = new SymbolicParamAndGenerator();
					formula = param.getSequentialAndCost((String[]) childrenNodes.toArray(new String[0]));
				}
			}
			else {
				formula = "( - " + childrenNodes.get(0) + " * " + childrenNodes.get(1) + " + " + childrenNodes.get(0) + " + " + childrenNodes.get(1) + " ) ";
				String removeFromFormula = new String();
				String sumCost = new String();
				if (!reliability) {
					formula = formula.replaceAll(childrenNodes.get(0), "R_" + childrenNodes.get(0));
					formula = formula.replaceAll(childrenNodes.get(1), "R_" + childrenNodes.get(1));
					removeFromFormula = " - R_" + childrenNodes.get(0) + " * " + childrenNodes.get(1);
					sumCost = childrenNodes.get(0) + " + " + childrenNodes.get(1);
				}

				for (int i = 2; i < childrenNodes.size(); i++) {
					if (!reliability) {
						removeFromFormula += " - " + formula + " * " + childrenNodes.get(i);
						sumCost += " + " + childrenNodes.get(i);
					}
					formula = "( - " + formula + " * " + childrenNodes.get(i) + " + " + formula + " + " + childrenNodes.get(i) + " ) ";
					if (!reliability) formula = formula.replaceAll(childrenNodes.get(i), "R_" + childrenNodes.get(i));
				}
				if (!reliability) {
					formula = " ( " + formula + " * ( " + sumCost + " ) " + removeFromFormula + " ) "; 
				}
			}
			return formula;
		}
		else {
			if (childrenNodes.size() == 1) {
				if (reliability) return " ( " + childrenNodes.get(0) + " )";
				formula = " ( R_" + childrenNodes.get(0) + " * " + childrenNodes.get(0) + " )";
			}
			else {
				formula = "( - " + childrenNodes.get(0) + " * " + childrenNodes.get(1) + " + " + childrenNodes.get(0) + " + " + childrenNodes.get(1) + " ) ";
				String removeFromFormula = new String();
				String sumCost = new String();
				if (!reliability) {
					formula = formula.replaceAll(childrenNodes.get(0), "R_" + childrenNodes.get(0));
					formula = formula.replaceAll(childrenNodes.get(1), "R_" + childrenNodes.get(1));
					removeFromFormula = " - R_" + childrenNodes.get(0) + " * " + childrenNodes.get(1);
					sumCost = childrenNodes.get(0) + " + " + childrenNodes.get(1);
				}

				for (int i = 2; i < childrenNodes.size(); i++) {
					if (!reliability) {
						removeFromFormula += " - " + formula + " * " + childrenNodes.get(i);
						sumCost += " + " + childrenNodes.get(i);
					}
					formula = "( - " + formula + " * " + childrenNodes.get(i) + " + " + formula + " + " + childrenNodes.get(i) + " ) ";
					if (!reliability) formula = formula.replaceAll(childrenNodes.get(i), "R_" + childrenNodes.get(i));
				}
				if (!reliability) {
					formula = " ( " + formula + " * ( " + sumCost + " ) " + removeFromFormula + " ) "; 
				}
			}
		}
		return formula;
	}

	private List<String> getChildrenId(RTContainer rootNode) {
		List<String> ids = new ArrayList<String>();
		LinkedList<RTContainer> children = rootNode.getDecompElements();
		
		if (children.isEmpty()) return ids;
		
		for (RTContainer child : children) {
			if(child instanceof GoalContainer) ids.add(child.getClearUId());
			else ids.add(child.getClearElId());
		}
		
		if (ids.size() == 1) return ids;
		
		return ids;
	}
}