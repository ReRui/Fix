package soot.util;

import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;

import java.util.Iterator;

public class PetriNet {
    private String methodName;
    private UnitGraph graph;


    public PetriNet(String name, UnitGraph graph) {
        this.methodName = name;
        this.graph = graph;
    }


    @Override
    public String toString() {

        String result = "";

        Iterator iterator = this.graph.iterator();

        while(iterator.hasNext()) {
            Unit u = (Unit)iterator.next();

            LineNumberTag lineNumberTag = (LineNumberTag)u.getTag("LineNumberTag");

            if( lineNumberTag != null ) {
                result += lineNumberTag.getLineNumber() + "\t";
            }

            result += u.toString() + "\n";
        }
        return result;
    }
}
