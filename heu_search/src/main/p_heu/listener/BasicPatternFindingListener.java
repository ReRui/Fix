package p_heu.listener;

import gov.nasa.jpf.ListenerAdapter;

import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.SearchState;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.search.*;

import java.util.ArrayList;

public class BasicPatternFindingListener extends ListenerAdapter {

    private Sequence sequence;
    private SearchState currentState;
    private ArrayList<Node> currentStateNodes;
    private int nodeId;
    private int lastStateId;
    private boolean execResult;
    private Filter positionFilter;

    public BasicPatternFindingListener() {

        this.sequence = new Sequence();
        currentState = null;
        currentStateNodes = null;
        nodeId = 0;
        lastStateId = 0;
        execResult = true;
        positionFilter = null;
    }

    private void initCurrentState(VM vm) {
        ChoiceGenerator<?> currentCG = vm.getChoiceGenerator();
        if(currentCG != null){
            currentState = new SearchState(vm.getStateId(),vm.getRestorableState());
        }else{
            currentState = new SearchState(vm.getStateId(),vm.getRestorableState());
        }
        currentStateNodes = new ArrayList<>();
    }

    private void saveLastState() {
        sequence = sequence.advance(currentState.getStateId(),currentState.getState(), currentStateNodes);
    }

    public Sequence getSequence() {
        return sequence;
    }

    private int getNodeId() {
        return nodeId++;
    }

    public void setPositionFilter(Filter filter) {
        positionFilter = filter;
    }

    @Override
    public void searchStarted(Search search) {
        super.searchStarted(search);
        //System.out.println("search i am the first you garbage");
        VM vm = search.getVM();
        initCurrentState(vm);
    }

    public void stateAdvanced(Search search) {
        DistanceBasedSearch dbsearch = (DistanceBasedSearch) search;
        //System.out.println("listener i am the first");
        if (currentState != null) {
            saveLastState();
            dbsearch.addQueue(sequence);
        }
        VM vm = search.getVM();
        initCurrentState(vm);
    }

    @Override
    public void stateRestored(Search search) {
        super.stateRestored(search);
    }

    @Override
    public void propertyViolated(Search search) {
        super.propertyViolated(search);
        execResult = false;
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        super.instructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;

            if (positionFilter != null && !positionFilter.filter(fins.getFileLocation())) {
                return;
            }

            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);

            String type = fins.isRead() ? "READ" : "WRITE";
            String eiString = ei == null ? "null" : ei.toString();
            String fiName = fi.getName();
            ReadWriteNode node = new ReadWriteNode(getNodeId(), eiString, fiName, type, currentThread.getName(), fins.getFileLocation());
            currentStateNodes.add(node);
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
        super.choiceGeneratorAdvanced(vm, currentCG);
        if (currentCG instanceof ThreadChoiceFromSet) {

            ThreadInfo[] threads = ((ThreadChoiceFromSet)currentCG).getAllThreadChoices();
            if (threads.length == 1) {
                return;
            }
            ThreadInfo ti = (ThreadInfo)currentCG.getNextChoice();
            Instruction insn = ti.getPC();
            String type = insn.getClass().getName();
            ScheduleNode node = new ScheduleNode(getNodeId(), ti.getName(), insn.getFileLocation(), type);
            currentStateNodes.add(node);
        }
    }

    @Override
    public void searchFinished(Search search) {
        super.searchFinished(search);
        sequence = sequence.advanceToEnd(currentState.getStateId(), currentState.getState(), currentStateNodes, execResult);
    }
}
