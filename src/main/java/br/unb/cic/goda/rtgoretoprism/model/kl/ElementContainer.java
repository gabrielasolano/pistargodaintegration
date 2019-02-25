package br.unb.cic.goda.rtgoretoprism.model.kl;

import br.unb.cic.goda.rtgoretoprism.util.NameUtility;
import br.unb.cic.goda.rtgoretoprism.model.kl.SoftgoalContainer;
import br.unb.cic.goda.model.GeneralEntity;

import java.util.Hashtable;
import java.util.LinkedList;

public class ElementContainer {

    private String name = "";
    protected LinkedList<GoalContainer> goals;
    protected LinkedList<PlanContainer> plans;
    protected Const decomposition = Const.NONE;
    private Hashtable<SoftgoalContainer, String> contributions;

    public ElementContainer(GeneralEntity m) {
        this.name = NameUtility.adjustName(m.getName());
        contributions = new Hashtable<>();
    }
    
	public ElementContainer() {
		contributions = new Hashtable<SoftgoalContainer, String>();
	}

    public String getName() {
        return name;
    }

    public void addContribution(SoftgoalContainer sgcont, String metric) {
        contributions.put(sgcont, metric);
    }

    public Hashtable<SoftgoalContainer, String> getContributions() {
        return contributions;
    }

    public void createDecomposition(Const decomp) {
        decomposition = decomp;
    }

    public Const getDecomposition() {
        return decomposition;
    }
}