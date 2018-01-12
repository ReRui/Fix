package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.listener.BasicPatternFindingListener;

public class Startup {
	public static void main(String[] args) {
		String[] str = new String[]{
				"+classpath=out/production/heu_search",
//				"+search.class=p_heu.search.BFSearch",
				//"+search.class=p_heu.search.SingleExecutionSearch",
				"+search.class=p_heu.search.PatternDistanceBasedSearch",
				"CheckField"};

		Config config = new Config(str);
		BasicPatternFindingListener listener = new BasicPatternFindingListener();
		Filter filter = Filter.createFilePathFilter();
		listener.setPositionFilter(filter);
		JPF jpf = new JPF(config);
		jpf.addListener(listener);
		jpf.run();

		System.out.println(listener.getSequence());
	}
}
