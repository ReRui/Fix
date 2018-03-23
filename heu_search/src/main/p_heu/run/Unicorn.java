package p_heu.run;

import fix.entity.ImportPath;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
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
        Unicorn.getPatternCounterList();
    }

    //原来是main函数
    public static List<PatternCounter> getPatternCounterList() {
        List<PatternCounter> patternCounters = new ArrayList<>();

        for (int i = 0; i < 50; ++i) {
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


            Sequence seq = listener.getSequence();
            //sequence中有时候会出现同一个线程对某个地方重复执行两次的情况
            //我们只记录第二次，放弃第一次
            //因为实际产生效果的是第二次
            //jpf中产生这种情况的原因不明
            reduceSeq(seq);

            System.out.println(listener.getSequence().getNodes() + "getNodes");

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

        //输出pattern信息
        /*for (PatternCounter p : patternCounters) {
            System.out.println(p);
        }*/
        return patternCounters;
    }

    private static void reduceSeq(Sequence seq) {
        List<Node> nodesList = seq.getNodes();
        for(int i = 0;i < nodesList.size();i++){
            if(nodesList.get(i) instanceof ReadWriteNode){
                for(int j = i - 1; j >= 0; j--){
                    if(nodesList.get(j) instanceof ReadWriteNode){
                        ReadWriteNode rwi = (ReadWriteNode) nodesList.get(i);
                        ReadWriteNode rwj = (ReadWriteNode) nodesList.get(j);
                        if((rwi.getId() != rwj.getId()) && rwi.getElement().equals(rwj.getElement()) && rwi.getField().equals(rwj.getField()) && rwi.getType().equals(rwj.getType()) && rwi.getPosition().equals(rwj.getPosition())){
                            seq.getNodes().remove(j);
                            i--;
                        }
                    }
                }
            }
        }
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
