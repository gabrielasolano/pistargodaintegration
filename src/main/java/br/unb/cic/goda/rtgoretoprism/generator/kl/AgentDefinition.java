package br.unb.cic.goda.rtgoretoprism.generator.kl;

import br.unb.cic.goda.model.Actor;
import br.unb.cic.goda.model.Goal;
import br.unb.cic.goda.model.Plan;
import br.unb.cic.goda.rtgoretoprism.model.kl.*;
import br.unb.cic.goda.rtgoretoprism.util.NameUtility;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentDefinition {

    public LinkedList<GoalContainer> rootlist = new LinkedList<>();
    public Hashtable<String, PlanContainer> planbase;
    private String agentname;
    protected Hashtable<String, SoftgoalContainer> softgoalbase;
    protected Hashtable<String, GoalContainer> goalbase;

    public AgentDefinition(Actor a) {
        softgoalbase = new Hashtable<>();
        goalbase = new Hashtable<>();
        planbase = new Hashtable<>();
        agentname = NameUtility.adjustName(a.getName());
    }

    public static String parseElId(String name) {
       // String patternString = "(^[GT]\\d+\\.?\\d*):";
        String patternString = "(^[GT]\\w+\\.?\\w*):";
        Pattern pattern = Pattern.compile(patternString);
        java.util.regex.Matcher matcher = pattern.matcher(name);
        if (matcher.find())
            return matcher.group(1);
        else
            return null;
    }

    private static String parseRTRegex(String name) {
        String patternString = "\\[(.*)\\]";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())
        	return matcher.group(1);
        else
            return null;
    }

    public void addRootGoal(GoalContainer rootgoal) {
        rootlist.add(rootgoal);
    }

    public GoalContainer createGoal(Goal goal, Const type) {
        GoalContainer gc = new GoalContainer(goal, type);
        setRTAttributes(gc);
        if (goalbase.containsKey(gc.getName()))
            return goalbase.get(gc.getName());
        goalbase.put(gc.getName(), gc);
        return gc;
    }

    public PlanContainer createPlan(Plan p) {
        PlanContainer pc = new PlanContainer(p);
        setRTAttributes(pc);
        if (planbase.containsKey(pc.getName()))
            return planbase.get(pc.getName());
        planbase.put(pc.getName(), pc);
        return pc;
    }

    public boolean containsGoal(Goal goal) {
        ElementContainer gc = new ElementContainer(goal);
        return goalbase.containsKey(gc.getName());
    }

    public boolean containsPlan(Plan plan) {
        ElementContainer pc = new ElementContainer(plan);
        return planbase.containsKey(pc.getName());
    }

    public String getAgentName() {
        return agentname;
    }

    public List<GoalContainer> getRootGoalList() {
    	return rootlist;
    }

    private void setRTAttributes(RTContainer gc) {
        gc.setElId(parseElId(gc.getName()));
        gc.setRtRegex(parseRTRegex(gc.getName()));
    }
}