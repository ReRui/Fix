package soot;

import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Functions;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        String path = "D:\\test\\src";
        Functions.initSoot(path);

        SootClass appclass = Scene.v().loadClassAndSupport("examples.account.Account");

        //appclass.get

        SootMethod method = appclass.getMethodByName("transfer");

        printCallFromMethod(method);
        //UnitGraph unitGraph = new ExceptionalUnitGraph(method.retrieveActiveBody());
        //System.out.print(unitGraph.size());
        //
        //PetriNet petriNet = new PetriNet(method.getName(), unitGraph);
        //
        //System.out.println(petriNet);
    }

    public static void printCallFromMethod(SootMethod method) {
        JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);

        Iterator<Unit> it = g.iterator();

        while(it.hasNext()) {
            Stmt stmt = (Stmt)it.next();
            LineNumberTag lineNumberTag = (LineNumberTag)stmt.getTag("LineNumberTag");
            if(stmt.containsInvokeExpr()) {
                //System.out.println("the invoke is : " + stmt.toString());
                if (lineNumberTag != null) {
                    System.out.print("In line " + lineNumberTag.getLineNumber());
                    System.out.println(" call method : " + stmt.getInvokeExpr().getMethod());
                }
            }
        }
    }
}