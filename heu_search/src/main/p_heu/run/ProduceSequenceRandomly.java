package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.listener.SequenceProduceListener;

public class ProduceSequenceRandomly {
    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
				"+search.class=p_heu.search.SingleExecutionSearch",
                "hashcodetest.HashCodeTest"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        System.out.println(listener.getSequence());
    }
}
