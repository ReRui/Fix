package p_heu.run;

import fix.entity.ImportPath;
import fix.entity.type.UnicornType;
import fix.io.InsertCode;
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

    //修复所依赖的pattern
    static List<PatternCounter> patternCountersList = new ArrayList<>();

    //验证修复结果
    static boolean verifyFlag = true;

    //测试用的main函数
    public static void main(String[] args) {
        /*List<PatternCounter> patternCounters = Unicorn.getPatternCounterList();
        for (PatternCounter p : patternCounters) {
            System.out.println(p);
        }*/

        //获取sequence信息
//        System.out.println(patternCounters.get(0).getFirstFailAppearPlace() + "sequence");
    }

    //获取pattern
    public static List<PatternCounter> getPatternCounterList(String classpath) {
        useUnicorn(UnicornType.getPattern, classpath);
        return patternCountersList;
    }

    //获取验证结果
    public static boolean verifyFixSuccessful(String classpath) {
        useUnicorn(UnicornType.verify, classpath);
        //将拿到的pattern写入文件中
        InsertCode.writeToFile(patternCountersList.toString(), ImportPath.examplesRootPath + "\\logFile\\验证得到的pattern.txt");
        return verifyFlag;
    }

    private static void useUnicorn(int type, String classpath) {

        //将原来的清空
        patternCountersList.clear();

        /*if (type == UnicornType.getPattern) {
            classpath = ImportPath.examplesRootPath + "\\out\\production\\Patch";
        } else if (type == UnicornType.verify) {
            classpath = ImportPath.verifyPath + "\\generateClass";
        }*/

        if (type == UnicornType.verify) {
            //先将生成补丁后的程序编译成class文件
            //因为jpf文件要对class文件处理
            //源路径，目标路径
            GenerateClass.compileJava(ImportPath.verifyPath + "\\exportExamples\\" + ImportPath.projectName, classpath);
        }

        for (int i = 0; i < 50; ++i) {
            String[] str = new String[]{
                    "+classpath=" + classpath,
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
            seq = reduceSeq(seq);

            if (type == UnicornType.verify) {
                if (!seq.getResult()) {
                    verifyFlag = false;
                }
            }

            outer:
            for (Pattern pattern : seq.getPatterns()) {
                for (PatternCounter p : patternCountersList) {
                    if (p.getPattern().isSameExecptThread(pattern)) {
                        if (!seq.getResult() && p.getFirstFailAppearPlace() == null) {
                            p.setFirstFailAppearPlace(seq);
                        }
                        p.addOne(seq.getResult());
                        continue outer;
                    }
                }
                patternCountersList.add(new PatternCounter(pattern, seq.getResult(), seq.getResult() ? null : seq));
            }
        }

        Collections.sort(patternCountersList, new Comparator<PatternCounter>() {
            @Override
            public int compare(PatternCounter o1, PatternCounter o2) {
                double r1 = (double) o1.getSuccessCount() / (o1.getSuccessCount() + o1.getFailCount());
                double r2 = (double) o2.getSuccessCount() / (o2.getSuccessCount() + o2.getFailCount());
                return Double.compare(r1, r2);
            }
        });
    }


    private static Sequence reduceSeq(Sequence seq) {
        List<Node> nodesList = seq.getNodes();
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.get(i) instanceof ReadWriteNode) {
                for (int j = i - 1; j >= 0; j--) {
                    if (nodesList.get(j) instanceof ReadWriteNode) {
                        ReadWriteNode rwi = (ReadWriteNode) nodesList.get(i);
                        ReadWriteNode rwj = (ReadWriteNode) nodesList.get(j);
                        if ((rwi.getId() != rwj.getId()) && rwi.getElement().equals(rwj.getElement()) && rwi.getField().equals(rwj.getField()) && rwi.getType().equals(rwj.getType()) && rwi.getPosition().equals(rwj.getPosition())) {
                            seq.getNodes().remove(j);
                            i--;
                        }
                    }
                }
            }
        }
        return seq;
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
