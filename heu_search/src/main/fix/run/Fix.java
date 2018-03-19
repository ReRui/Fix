package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.analyzefile.LockPolicyPopularize;
import fix.analyzefile.RecordSequence;
import fix.entity.ImportPath;
import fix.entity.record.MatchResult;
import fix.io.ExamplesIO;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Fix {
    static ExamplesIO examplesIO = ExamplesIO.getInstance();
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args) {
        List<Unicorn.PatternCounter> p = Unicorn.m();
        //拿到第一个元素
        System.out.println("定位到的pattern");
        System.out.println(p.get(0).getPattern().getNodes()[0]);
        System.out.println(p.get(0).getPattern().getNodes()[1]);
        if (p.get(0).getPattern().getNodes().length > 2) {
            System.out.println("dayu 2");
            System.out.println(p.get(0).getPattern().getNodes()[2]);
        }
        divideByLength(p.get(0));
    }

    //根据pattern的长度执行不同的fix策略
    private static void divideByLength(Unicorn.PatternCounter patternCounter) {
        int length = patternCounter.getPattern().getNodes().length;
        if (length == 2) {
            fixPatternOneToThree(patternCounter.getPattern());
        } else if (length == 3) {
            fixPatternFourToEight(patternCounter);
        } else if (length == 4) {
            fixPatterNineToSeventeen(patternCounter);
        }
    }

    private static void fixPatterNineToSeventeen(Unicorn.PatternCounter patternCounter) {
        addSyncPatternNineToSeventeen(patternCounter.getPattern());
    }

    private static void addSyncPatternNineToSeventeen(Pattern patternCounter) {
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);
        System.out.println("第四步");
        int[] arrLoc = new int[4];
        for (int i = 0; i < 4; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");
            arrLoc[i] = Integer.parseInt(positionArg[1]);
        }

        //待定，此处只是排序后将前两个加锁，后两个加锁
        examplesIO.addLockToOneVar(arrLoc[0], arrLoc[1] + 1, "obj", dirPath + "\\Account.java");
        examplesIO.addLockToOneVar(arrLoc[2], arrLoc[3] + 1, "obj", dirPath + "\\Account.java");
    }

    private static void fixPatternFourToEight(Unicorn.PatternCounter patternCounter) {
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //获取读写节点
        ReadWriteNode[] nodesArr = patternCounter.getPattern().getNodes();

        System.out.println("第三步");
        addSyncPatternFourToEight(patternCounter.getPattern());

        /*//如果该变量没有加锁则引入一个新锁,并且添加同步
        if(checkWhetherLocked.check(nodesArr[1].getPosition(),nodesArr[1].getElement())){//j没被加锁
            addSyncPatternFourToEight(patternCounter.getPattern());
        }
        //直接添加同步
        else{

        }*/
    }

    //长度为3添加同步
    private static void addSyncPatternFourToEight(Pattern patternCounter) {
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);
        int[] arr = new int[3];
        //根据线程将三个结点分为两个list
        List<ReadWriteNode> threadA = new ArrayList<ReadWriteNode>();//线程A的结点
        List<ReadWriteNode> threadB = new ArrayList<ReadWriteNode>();//线程B的结点
        String threadName = "";
        for (int i = 0; i < 3; i++) {
            ReadWriteNode node = patternCounter.getNodes()[i];
            if (i == 0) {//把第一个结点放入A的list
                threadName = node.getThread();
                threadA.add(node);
            } else {
                if (threadName.equals(node.getThread())) {//线程相同，放入同一个list
                    threadA.add(node);
                } else {//不同就放入另一个list
                    threadB.add(node);
                }
            }
        }


        int firstLoc = 0, lastLoc = 0;
        boolean threadAHasLock = false, threadBHasLock = false;
//        String lockNameA = "";
        //对A的list加锁
        for (int i = 0; i < threadA.size(); i++) {
            ReadWriteNode node = threadA.get(i);
            if (CheckWhetherLocked.check(node.getPosition(), node.getField())) {//检查是否存在锁
                threadAHasLock = true;
//                lockNameA = lockName(node);
            }
            int poi = Integer.parseInt(node.getPosition().split(":")[1]);
            if (i == 0) {
                firstLoc = poi;
                lastLoc = firstLoc;
            } else {
                if (poi < firstLoc) {
                    firstLoc = poi;
                } else {
                    lastLoc = poi;
                }
            }
        }
        System.out.println("a" + firstLoc + ",b" + lastLoc);
        System.out.println(threadAHasLock);

        if (threadAHasLock) {

        } else {
            //对每个变量进行判断，知道它需要加何种锁
            String lockName = "";
            for (int i = 0; i < threadA.size(); i++) {
                ReadWriteNode node = threadA.get(i);
                lockName = acquireLockName(node.getPosition());
            }
            examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\Account.java");
//            LockPolicyPopularize.fixRelevantVar(firstLoc, lastLoc, threadA.get(0).getThread(), lockName);//待定
        }

        //对B的list加锁
        for (int i = 0; i < threadB.size(); i++) {
            ReadWriteNode node = threadB.get(i);
            if (CheckWhetherLocked.check(node.getPosition(), node.getField())) {//检查是否存在锁
                threadBHasLock = true;
//                System.out.println("锁名称：" + lockName(node));
            }
            int poi = Integer.parseInt(node.getPosition().split(":")[1]);
            if (i == 0) {
                firstLoc = poi;
                lastLoc = firstLoc;
            } else {
                if (poi < firstLoc) {
                    firstLoc = poi;
                } else {
                    lastLoc = poi;
                }
            }
        }
        System.out.println("a" + firstLoc + ",b" + lastLoc);
        System.out.println(threadBHasLock);
        if (threadBHasLock) {

        } else {
            //对每个变量进行判断，知道它需要加何种锁
            String lockName = "";
            for (int i = 0; i < threadB.size(); i++) {
                ReadWriteNode node = threadB.get(i);
                lockName = acquireLockName(node.getPosition());
            }
            examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\Account.java");
//            LockPolicyPopularize.fixRelevantVar(firstLoc, lastLoc, threadA.get(0).getThread(), lockName);//待定
        }
    }

    //读到那一行，然后对字符串处理
    private static String acquireLockName(String position) {
        BufferedReader br = null;
        String read = "";//用来读
        String result = "";//用来处理
        int line = 0;
        int poi = Integer.parseInt(position.split(":")[1]);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dirPath + "\\Account.java")), "UTF-8"));
            while (((read = br.readLine()) != null)) {
                line++;
                if (line == poi) {//找到哪一行
                    String[] res = read.split("\\.");
                    if (res.length > 1) {
                        result = res[0];
                    } else {
                        result = "this";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.trim();
    }

   /* //输出锁的名称
    //此处根据pattern读到锁的那行，然后使用字符串匹配
    private static String lockName(ReadWriteNode node) {
        int number = Integer.parseInt(node.getPosition().split(":")[1]);
        String name = "";
        try {
            name = YaoShan.shan(number);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }*/

    private static void fixPatternOneToThree(Pattern patternCounter) {
        if (RecordSequence.isLast(patternCounter.getNodes()[0]) || RecordSequence.isFirst(patternCounter.getNodes()[1])) {
            addSignal(patternCounter);
        } else {
            //为长度为2的pattern添加同步
            addSyncPatternOneToThree(patternCounter);
        }
    }


    //对长度为2的pattern添加同步
    private static void addSyncPatternOneToThree(Pattern patternCounter) {

        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);

        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");

            //加锁
            examplesIO.addLockToOneVar(Integer.parseInt(positionArg[1]), Integer.parseInt(positionArg[1]) + 1, "obj", dirPath + "\\Account.java");//待定

        }

        System.out.println(dirPath + "=============");
    }

    //添加信号量修复顺序违背
    private static void addSignal(Pattern patternCounter) {
        //得到pattern中较小的行数
        int flagDefineLocation = Integer.MAX_VALUE;//flag应该在哪行定义
        int flagAssertLocation = Integer.MIN_VALUE;//flag应该在那行判断
        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");
            flagDefineLocation = Integer.parseInt(positionArg[1]) < flagDefineLocation ? Integer.parseInt(positionArg[1]) : flagDefineLocation;
            flagAssertLocation = Integer.parseInt(positionArg[1]) > flagAssertLocation ? Integer.parseInt(positionArg[1]) : flagAssertLocation;

        }

        System.out.println(flagDefineLocation);
        System.out.println(flagAssertLocation);
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);

        //添加信号量的定义
        examplesIO.addVolatileDefine(flagDefineLocation, "volatile bool flag = false;", dirPath + "\\Account.java");//待修订

        //添加信号为true的那条语句，那条语句应该在定义的后一行
        examplesIO.addVolatileToTrue(flagDefineLocation + 1, dirPath + "\\Account.java");//待修订


        //添加信号量判断,
        //待定，只执行一句我就加了分号，这样是否可行？
        examplesIO.addVolatileIf(flagAssertLocation, dirPath + "\\Account.java");//待修订
    }

}
