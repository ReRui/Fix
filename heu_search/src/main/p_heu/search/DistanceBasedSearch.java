package p_heu.search;

import java.util.*;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.Node;
import p_heu.entity.sequence.Sequence;

public abstract class DistanceBasedSearch extends Search {

	protected Set<Sequence> correctSeqs;
	protected LinkedList<Sequence> queue;
	protected int scheduleThreshod;

	protected DistanceBasedSearch(Config config, VM vm) {
		super(config, vm);
		this.correctSeqs = new HashSet<>();
		this.queue = new LinkedList<>();
		scheduleThreshod = 2;
	}

	@Override
	public boolean requestBacktrack () {
		doBacktrack = true;

		return true;
	}

	@Override
	public boolean supportsBacktrack () {
		return true;
	}


	@Override
	public void search() {
		// TODO 编写search函数
		notifySearchStarted();
		int currentRun = 1;
		int currentNumberOfChoice = 0;

		while (!done){

			for(int run = 0; run < currentRun; run++) {
				try {
					Sequence sequence = queue.getLast();
					//TODO needs fix
					currentNumberOfChoice = -1;
				} catch (Exception e) {
					currentNumberOfChoice = -1;
				}

				if (currentNumberOfChoice <= 1) {
					//System.out.println("search i am the first");
					if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState()) {
						break;
					}

					if (forward()) {
						depth++;
						notifyStateAdvanced();

						if (currentError != null) {
							notifyPropertyViolated();

							if (hasPropertyTermination()) {
								break;
							}
						}

						if (!checkStateSpaceLimit()) {
							notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
							// can't go on, we exhausted our memory
							break;
						}
					} else { // forward did not execute any instructions
						notifyStateProcessed();
					}
					if (currentNumberOfChoice != -1) {
						queue.removeFirst();
					}
				} else {

					for (int j = 0; j < currentNumberOfChoice; j++) {

						if (forward()) {
							depth++;
							notifyStateAdvanced();

							if (currentError != null) {
								notifyPropertyViolated();

								if (hasPropertyTermination()) {
									break;
								}
							}

							if (!checkStateSpaceLimit()) {
								notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
								// can't go on, we exhausted our memory
								break;
							}
						} else { // forward did not execute any instructions
							notifyStateProcessed();
						}
						if (!backtrack()) { // backtrack not possible, done
							break;
						}
					}
					queue.removeFirst();
				}
				if(currentNumberOfChoice > 1){

					if(run == 0) {
						sortQueue();
						while (queue.size() > scheduleThreshod) {
							queue.removeLast();
						}
					}
					vm.restoreState(queue.getFirst().getLastState().getState());
					vm.resetNextCG();

					if(queue.size() < scheduleThreshod){
						if(queue.size() == 0){
							currentRun = 1;
						}else{
							currentRun = queue.size();
						}
					}else{
						currentRun = queue.size();
					}
				}
			}
		}

		notifySearchFinished();
	}

	protected void addCorrectSeq(Sequence seq) {
		correctSeqs.add(seq);
	}

	public void addQueue(Sequence seq) {
		queue.add(seq);
	}

	protected Sequence findSequenceByLastState(int lastStateId) {
		for (Sequence seq : queue) {
			if (seq.getLastState().getStateId() == lastStateId) {
				return seq;
			}
		}
		return null;
	}

	public void stateAdvance(int lastStateId, List<Node> nodes) {
		Sequence seq = findSequenceByLastState(lastStateId);
		queue.remove(seq);

	}

	protected void sortQueue() {
		Collections.sort(this.queue, getComparator());
	}

	protected abstract Comparator<Sequence> getComparator();
}

