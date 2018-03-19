package p_heu.run;

import fix.analyzefile.RecordSequence;
import fix.entity.ImportPath;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.SequenceProduceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Unicorn {
    //测试用的main函数
    public static void main(String[] args) {
        Unicorn.m();
    }

    //原来是main函数
    public static List<PatternCounter> m() {
        List<PatternCounter> patternCounters = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            String[] str = new String[]{
                    "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    ImportPath.projectName + "." + ImportPath.mainClassName
            };
            Config config = new Config(str);
            JPF jpf = new JPF(config);
            SequenceProduceListener listener = new SequenceProduceListener();

            Filter filter = Filter.createFilePathFilter();
            listener.setPositionFilter(filter);

            jpf.addListener(listener);
            jpf.run();
//            System.out.println(listener.getSequence().getNodes() + "getNodes");

            Sequence seq = listener.getSequence();
            outer:
            for (Pattern pattern : seq.getPatterns()) {
                for (PatternCounter p : patternCounters) {
                    if (p.getPattern().isSameExecptThread(pattern)) {
                        if (!seq.getResult() && p.getFirstFailAppearPlace() == null) {
                            p.setFirstFailAppearPlace(seq);
                        }
                        p.addOne(seq.getResult());
                        continue outer;
                    }
                }
                patternCounters.add(new PatternCounter(pattern, seq.getResult(), seq.getResult() ? null : seq));
            }
        }

        Collections.sort(patternCounters, new Comparator<PatternCounter>() {
            @Override
            public int compare(PatternCounter o1, PatternCounter o2) {
                double r1 = (double) o1.getSuccessCount() / (o1.getSuccessCount() + o1.getFailCount());
                double r2 = (double) o2.getSuccessCount() / (o2.getSuccessCount() + o2.getFailCount());
                return Double.compare(r1, r2);
            }
        });

        for (PatternCounter p : patternCounters) {
            System.out.println(p);
        }
        return patternCounters;
    }

    public static class PatternCounter {
        private Pattern pattern;
        private int successCount;
        private int failCount;
        private Sequence firstFailAppearPlace;

        public Sequence getFirstFailAppearPlace() {
            return firstFailAppearPlace;
        }

        public void setFirstFailAppearPlace(Sequence firstFailAppearPlace) {
            this.firstFailAppearPlace = firstFailAppearPlace;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public PatternCounter(Pattern pattern, boolean result) {
            this.pattern = pattern;
            this.firstFailAppearPlace = null;
            if (result) {
                successCount = 1;
                failCount = 0;
            } else {
                successCount = 0;
                failCount = 1;
            }
        }

        public PatternCounter(Pattern pattern, boolean result, Sequence ffap) {
            this.pattern = pattern;
            this.firstFailAppearPlace = ffap;
            if (result) {
                successCount = 1;
                failCount = 0;
            } else {
                successCount = 0;
                failCount = 1;
            }
        }

        public void addOne(boolean result) {
            if (result) {
                successCount += 1;
            } else {
                failCount += 1;
            }
        }

        public String toString() {
            return pattern.toString() + "\nsuccess count: " + this.successCount + "\nfail count: " + this.failCount;
        }
    }
}
