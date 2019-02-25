package br.unb.cic.goda.rtgoretoprism.generator.goda.producer;

import br.unb.cic.goda.model.Actor;
import br.unb.cic.goda.model.GeneralEntity;
import br.unb.cic.goda.model.Goal;
import br.unb.cic.goda.model.Plan;
import br.unb.cic.goda.rtgoretoprism.generator.CodeGenerationException;
import br.unb.cic.goda.rtgoretoprism.generator.goda.parser.CostParser;
import br.unb.cic.goda.rtgoretoprism.generator.goda.parser.RTParser;
import br.unb.cic.goda.rtgoretoprism.generator.goda.writer.PrismWriter;
import br.unb.cic.goda.rtgoretoprism.generator.kl.AgentDefinition;
import br.unb.cic.goda.rtgoretoprism.model.kl.Const;
import br.unb.cic.goda.rtgoretoprism.model.kl.GoalContainer;
import br.unb.cic.goda.rtgoretoprism.model.kl.PlanContainer;
import br.unb.cic.goda.rtgoretoprism.model.kl.RTContainer;
import br.unb.cic.goda.rtgoretoprism.util.FileUtility;
import br.unb.cic.goda.rtgoretoprism.util.kl.TroposNavigator;

import java.io.IOException;
import java.util.*;

public class RTGoreProducer {

	private TroposNavigator tn; 
	private String inputFolder;
	private String outputFolder;
	private Set<Actor> allActors;
	private Set<Goal> allGoals;

	/** memory for the parsed RT regex */
	List<String> rtDMGoals;

	public RTGoreProducer(Set<Actor> allActors, Set<Goal> allGoals, String in, String out) {

		tn = new TroposNavigator();

		this.inputFolder = in;
		this.outputFolder = out;
		this.allActors = allActors;
		this.allGoals = allGoals;

		this.rtDMGoals = new ArrayList<String>();
	}

	/**
	 * Run the process by which the Jadex source code is generated for the
	 * specified Tropos (system) actors
	 * 
	 * @throws CodeGenerationException 
	 * @throws IOException 
	 */
	public AgentDefinition run() throws CodeGenerationException, IOException {
		System.out.println("Starting PRISM Model Generation Process (Knowledge Level)" );
		System.out.println("\tTemplate Input Folder: " + inputFolder );
		System.out.println("\tOutput Folder: " + outputFolder );

		long startTime = new Date().getTime();
		AgentDefinition ad = null;

		for( Actor a : allActors ) {
			System.out.println( "Generating MDP model for: " + a.getName() );

			//generate the AgentDefinition object for the current actor
			ad = new AgentDefinition( a );

			// analyse all root goals
			for( Goal rootgoal : tn.getRootGoals(a) ) {
				Const type = Const.ACHIEVE; 
				Const request = Const.NONE;
				
				//create the goalcontainer for this one
				GoalContainer gc = ad.createGoal(rootgoal, type);
				gc.setRequest(request);

				//add to the root goal list
				ad.addRootGoal(gc);
				addGoal(rootgoal, gc, ad, false);
			}		
			List<Plan> planList = a.getPlanList();

			PrismWriter writer = new PrismWriter(ad, planList, inputFolder, outputFolder, false);
			writer.writeModel();

			//Generate pctl formulas
			generatePctlFormulas(ad);
		}
		System.out.println( "MDP model created in " + (new Date().getTime() - startTime) + "ms.");
		return ad;
	}

	private void generatePctlFormulas(AgentDefinition ad) throws IOException {

		StringBuilder pmax = new StringBuilder("Pmax=? [ F \"success\" ]");
		StringBuilder pmin = new StringBuilder("Pmin=? [ F \"success\" ]");
		StringBuilder rmax = new StringBuilder("R{\"cost\"}max=? [ F \"success\" ]");
		StringBuilder rmin = new StringBuilder("R{\"cost\"}min=? [ F \"success\" ]");

//		FileUtility.deleteFile(outputFolder + "/AgentRole_" + ad.getAgentName() + "/ReachabilityMax.pctl", false);
//		FileUtility.deleteFile(outputFolder + "/AgentRole_" + ad.getAgentName() + "/ReachabilityMin.pctl", false);
//		FileUtility.deleteFile(outputFolder + "/AgentRole_" + ad.getAgentName() + "/CostMax.pctl", false);
//		FileUtility.deleteFile(outputFolder + "/AgentRole_" + ad.getAgentName() + "/CostMin.pctl", false);

		FileUtility.writeFile(pmax.toString(), outputFolder + "/ReachabilityMax.pctl");
		FileUtility.writeFile(pmin.toString(), outputFolder + "/ReachabilityMin.pctl");
		FileUtility.writeFile(rmax.toString(), outputFolder + "/CostMax.pctl");
		FileUtility.writeFile(rmin.toString(), outputFolder + "/CostMin.pctl");
	}

	private void addGoal(Goal g, GoalContainer gc, final AgentDefinition ad, boolean included) throws IOException {

		included = included || allGoals.isEmpty() || allGoals.contains(g);
		gc.setIncluded(included);
		
		String rtRegex = gc.getRtRegex();
		boolean dmRT = false;
		dmRT = storeRegexResults(gc.getUid(), rtRegex, gc.getDecomposition());

		List<Goal> declist = g.getDecompositionList();
		sortIntentionalElements(declist);
		if (g.isAndDecomposition())
			gc.createDecomposition(Const.AND);			
		else if (g.isOrDecomposition())
			gc.createDecomposition(Const.OR);	

		if (dmRT) gc.setDecisionMaking(this.rtDMGoals);

		iterateGoals(ad, gc, declist, included);
		iterateMeansEnds(g, gc, ad, included);

		if (gc.isDecisionMaking()) {
			storeDecisionMakingNodes(gc);
		}

		if (gc.getClearElId().contains("X")) {
			gc.setOptional(true);
			PlanContainer unknownPlan = new PlanContainer((Plan) gc);
			unknownPlan.setElId("TX");
			unknownPlan.setTimePath(gc.getTimePath());
			unknownPlan.setTimeSlot(gc.getTimeSlot());
			unknownPlan.setPrevTimePath(gc.getPrevTimePath());
			unknownPlan.setFutTimePath(gc.getFutTimePath());
			unknownPlan.setOptional(true);
			gc.addMERealPlan(unknownPlan);
		}
	}

	private void iterateGoals(AgentDefinition ad, GoalContainer gc, List<Goal> decList, boolean include) throws IOException{

		Integer prevPath = gc.getPrevTimePath();
		Integer rootFutPath = gc.getFutTimePath();
		Integer rootPath = gc.getTimePath();
		Integer rootTime = gc.getTimeSlot();
		gc.setRootTimeSlot(rootTime);

		for (Goal dec : decList) {
			boolean newgoal = !ad.containsGoal(dec);

			boolean first = false;
			if (gc.getDecompGoals().isEmpty()) first = true;

			GoalContainer deccont = ad.createGoal(dec, Const.ACHIEVE);
			gc.addDecomp(deccont);

			if (this.rtDMGoals.contains(deccont.getElId())) {
				deccont.setPrevTimePath(gc.getPrevTimePath()+1);
				deccont.setFutTimePath(gc.getFutTimePath()+1);
				deccont.setTimePath(rootPath+1);
				deccont.setTimeSlot(deccont.getPrevTimePath() + 1);	

				if (!first) deccont.setFutTimePath(rootPath+1);
			}
			else {
				if (!first) {
					deccont.setPrevTimePath(gc.getFutTimePath());
					deccont.setFutTimePath(gc.getFutTimePath()+1);
					deccont.setTimePath(rootPath);
					deccont.setTimeSlot(deccont.getPrevTimePath() + 1);
				}
				else{ 
					deccont.setPrevTimePath(prevPath);
					deccont.setFutTimePath(rootFutPath);
					deccont.setTimePath(rootPath);
					deccont.setTimeSlot(gc.getPrevTimePath()+1);
				}
			}
			deccont.addFulfillmentConditions(gc.getFulfillmentConditions());
			if (newgoal){
				addGoal(dec, deccont, ad, include);	
				gc.setFutTimePath(Math.max(deccont.getTimeSlot(), deccont.getFutTimePath()));		
			}	
		}		
	}

	private void addPlan(Plan p, PlanContainer pc, final AgentDefinition ad) throws IOException {

		List<Plan> decList = p.getEndPlans();
        sortIntentionalElements(decList);
        if (p.isAndDecomposition())
        	pc.createDecomposition(Const.AND);
        else
        	pc.createDecomposition(Const.OR);
		
		boolean dmRT = false;
		if (!decList.isEmpty()){
			dmRT = storeRegexResults(pc.getUid(), pc.getRtRegex(), pc.getDecomposition());
		}
		else { //P is a leaf-task
			pc.setCostRegex(pc.getRtRegex());
			pc.setRtRegex(null);
			storeCostResults(pc);
		}

		if (dmRT) pc.setDecisionMaking(this.rtDMGoals);

        iteratePlans(ad, pc, decList);
		if (pc.isDecisionMaking()) {
			storeDecisionMakingNodes(pc);
		}

		if (pc.getClearElId().contains("X")) pc.setOptional(true);
	}

	private void storeDecisionMakingNodes(RTContainer pc) {

		List<String> idNodes = pc.getDecisionMaking();
		LinkedList<RTContainer> nodes = new LinkedList<RTContainer>();

		for (String id : idNodes) {
			nodes.add(pc.getDecompElement(id));
		}
		pc.setDecisionNodes(nodes);
	}

	private void iteratePlans(AgentDefinition ad, PlanContainer pc, List<Plan> decList) throws IOException{

		Integer prevPath = pc.getPrevTimePath();
		Integer rootFutPath = pc.getFutTimePath();
		Integer rootPath = pc.getTimePath();
		for (Plan dec : decList) {
			boolean newplan = !ad.containsPlan(dec);

			boolean first = false;
			if (pc.getDecompPlans().isEmpty()) first = true;

			PlanContainer deccont = ad.createPlan(dec);
			pc.addDecomp(deccont);

			if (this.rtDMGoals.contains(deccont.getElId())) {
				deccont.setPrevTimePath(pc.getPrevTimePath()+1);
				deccont.setFutTimePath(pc.getFutTimePath()+1);
				deccont.setTimePath(rootPath+1);
				deccont.setTimeSlot(deccont.getPrevTimePath() + 1);	

				if (!first) deccont.setFutTimePath(rootPath+1);

			}
			else { 
				if (!first) {
					deccont.setPrevTimePath(pc.getFutTimePath());
					deccont.setFutTimePath(pc.getFutTimePath()+1);
					deccont.setTimePath(rootPath);
					deccont.setTimeSlot(deccont.getPrevTimePath() + 1);
				}else{
					deccont.setPrevTimePath(prevPath);
					deccont.setFutTimePath(rootFutPath);
					deccont.setTimePath(rootPath);
					deccont.setTimeSlot(prevPath+1);
				}
			}
			deccont.addFulfillmentConditions(pc.getFulfillmentConditions());

			if (newplan){
				addPlan(dec, deccont, ad);									
				pc.setFutTimePath(Math.max(deccont.getTimeSlot(), deccont.getFutTimePath()));
			}
		}
	}

	private void iterateMeansEnds(Goal g, GoalContainer gc, final AgentDefinition ad, boolean included) throws IOException{

		if (included && !g.getMeansToAnEndPlans().isEmpty()){
			List<Plan> melist = g.getMeansToAnEndPlans();
			sortIntentionalElements(melist);
			gc.createDecomposition(Const.ME);
			
			for (Plan p : melist) {
				boolean newplan = !ad.containsPlan(p);

				PlanContainer pc = ad.createPlan(p);
				gc.addMERealPlan(pc);

				pc.setPrevTimePath(gc.getPrevTimePath());
				pc.setFutTimePath(gc.getFutTimePath());
				pc.setTimePath(gc.getTimePath());
				pc.setTimeSlot(gc.getPrevTimePath()+1);

				pc.addFulfillmentConditions(gc.getFulfillmentConditions());

				if (newplan){
					addPlan(p, pc, ad);					
					gc.setFutTimePath(Math.max(pc.getTimeSlot(), pc.getFutTimePath()));
				}
			}			

//			List<Goal> megoallist = tn.getMeansEndMeanGoals(g);
//
//			for (Goal go : megoallist) {
//				boolean newgoal = !ad.containsGoal(go);
//
//				GoalContainer pc = ad.createGoal(go, Const.ACHIEVE);
//				gc.addDecomp(pc);
//				if (newgoal)
//					addGoal(go, pc, ad, true);
//			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean storeRegexResults(String uid, String rtRegex, Const decType) throws IOException {
		if(rtRegex != null){
			Object [] res = RTParser.parseRegex(uid, rtRegex + '\n', decType, false);
			rtDMGoals.addAll((List<String>) res [2]);

			List<String> dmList = (List<String>) res[2];
			if (!dmList.isEmpty()) return true;
		}
		return false;
	}

	private void storeCostResults(PlanContainer pc) throws IOException {
		if (pc.getCostRegex() != null) {
			Object [] res = CostParser.parseRegex(pc.getCostRegex());
			pc.setCostValue((String) res[0]);
			pc.setCostVariable((String) res[1]);
		}
	}

	private void sortIntentionalElements(List<? extends GeneralEntity> elements){

		Collections.sort(elements, new Comparator<GeneralEntity>() {
			@Override
			public int compare(GeneralEntity gA, GeneralEntity gB) {
				String idA = AgentDefinition.parseElId(gA.getName()).replaceAll("[TG]", "");
				String idB = AgentDefinition.parseElId(gB.getName()).replaceAll("[TG]", "");
				String idAStringPart = idA.replaceAll("\\d", "");
				String idBStringPart = idB.replaceAll("\\d", "");

				if(idAStringPart.equalsIgnoreCase(idBStringPart))
				{
					return extractInt(idA) - extractInt(idB);
				}
				return idA.compareToIgnoreCase(idB);
			}
		});
	}

	int extractInt(String s) {
		String num = s.replaceAll("\\D", "");
		return num.isEmpty() ? 0 : Integer.parseInt(num);
	}
}