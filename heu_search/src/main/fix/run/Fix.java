package fix.run;

import fix.analyzefile.*;
import fix.entity.ImportPath;
import fix.io.ExamplesIO;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Fix {
    static ExamplesIO examplesIO = ExamplesIO.getInstance();
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    static String whichCLassNeedSync = "";//需要添加同步的类，此处需不需考虑在不同类之间加锁的情况？

    public static void main(String[] args) {
        Unicorn.PatternCounter patternCounter = Unicorn.getPatternCounterList().get(0);
        //拿到第一个元素
        System.out.println("定位到的pattern");
        System.out.println(patternCounter.getPattern().getNodes()[0]);
        System.out.println(patternCounter.getPattern().getNodes()[1]);
        if (patternCounter.getPattern().getNodes().length > 2) {
            System.out.println(patternCounter.getPattern().getNodes()[2]);
            if (patternCounter.getPattern().getNodes().length > 3) {
                System.out.println(patternCounter.getPattern().getNodes()[3]);
            }
        }

        //拿到该pattern对应的sequence
        //第一次在失败运行中出现的sequence
        RecordSequence.display(patternCounter.getFirstFailAppearPlace());

        //先将项目拷贝到exportExamples
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);

        //根据pattern知道需要在哪个类中加锁
        whichCLassNeedSync = patternCounter.getPattern().getNodes()[0].getPosition().split(":")[0].split("/")[1];

        //对拷贝的项目进行修复
        divideByLength(patternCounter);

        //检测修复完的程序是否正确，不正确继续修复
//        FixResult.checkFix();
    }

    //根据pattern的长度执行不同的fix策略
    private static void divideByLength(Unicorn.PatternCounter patternCounter) {
        int length = patternCounter.getPattern().getNodes().length;
        if (length == 2) {
            System.out.println("修复一");
            fixPatternOneToThree(patternCounter.getPattern());
        } else if (length == 3) {
            System.out.println("修复二");
            fixPatternFourToEight(patternCounter.getPattern());
        } else if (length == 4) {
            System.out.println("修复三");
            fixPatterNineToSeventeen(patternCounter.getPattern());
        }
    }

    //长度为4的添加同步
    private static void fixPatterNineToSeventeen(Pattern patternCounter) {
        //是否可行？
        //根据线程将三个结点分为两个list
        List<ReadWriteNode> threadA = new ArrayList<ReadWriteNode>();//线程A的结点
        List<ReadWriteNode> threadB = new ArrayList<ReadWriteNode>();//线程B的结点
        String threadName = "";
        for (int i = 0; i < 4; i++) {
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

        addSyncFourToEight(threadA);
        addSyncFourToEight(threadB);
    }

    //长度为3添加同步
    private static void fixPatternFourToEight(Pattern patternCounter) {
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

        //根据获得的list，进行加锁
        addSyncFourToEight(threadA);
        addSyncFourToEight(threadB);
    }

    private static void addSyncFourToEight(List<ReadWriteNode> rwnList) {
        int firstLoc = 0, lastLoc = 0;
        boolean varHasLock = false;
        String lockName = "";
        //判断A中有几个变量
        if (rwnList.size() > 1) {//两个变量
            //如果有两个变量，需要分析
            //判断它们在不在一个函数中
            boolean flagSame = UseASTAnalysisClass.assertSameFunction(rwnList, dirPath + "\\" + whichCLassNeedSync);
//            System.out.println("判断在不在同一个函数" + flagSame);
            if (flagSame) {//在一个函数中
                //判断它们有没有加锁，需要加何种锁，加锁位置
                //对A的list加锁
                for (int i = 0; i < rwnList.size(); i++) {
                    ReadWriteNode node = rwnList.get(i);
                    if (CheckWhetherLocked.check(node.getPosition(), node.getField())) {//检查是否存在锁
                        varHasLock = true;
                    }
                    //应该要加什么锁
                    //这个步骤实际是用分析字符串来完成的
                    //实际上是不对的
                    lockName = acquireLockName(node);
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
                if (!varHasLock) {
                    //判断加锁区域在不在构造函数，或者加锁变量是不是成员变量
                    if (!UseASTAnalysisClass.isConstructOrIsMemberVariable(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync)) {

                        //判断加锁会不会和for循环等交叉
                        UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                        firstLoc = lockLine.getFirstLoc();
                        lastLoc = lockLine.getLastLoc();

                        //加锁
                        examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);
                    }
                }
            } else {//不在一个函数中
                for (int i = 0; i < rwnList.size(); i++) {
                    ReadWriteNode node = rwnList.get(i);
                    firstLoc = Integer.parseInt(node.getPosition().split(":")[1]);
                    lastLoc = firstLoc;
                    //每个都检查是不是加锁
                    if (!CheckWhetherLocked.check(node.getPosition(), node.getField())) {
                        //然后检查是不是成员变量或构造函数
                        if (!UseASTAnalysisClass.isConstructOrIsMemberVariable(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync)) {
                            //最后得到需要加什么锁
                            lockName = acquireLockName(node);
                            //判断加锁会不会和for循环等交叉
                            UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                            firstLoc = lockLine.getFirstLoc();
                            lastLoc = lockLine.getLastLoc();

                            //加锁
                            examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);
                        }
                    }
                }
            }
        } else {
            //对于一个变量，检查它是否已经被加锁
            ReadWriteNode node = rwnList.get(0);
            if (!CheckWhetherLocked.check(node.getPosition(), node.getField())) {
                //没被加锁，获得需要加锁的行数
                firstLoc = Integer.parseInt(node.getPosition().split(":")[1]);
                lastLoc = firstLoc;
                //然后获得需要加何种锁
                lockName = acquireLockName(node);

                //判断加锁会不会和for循环等交叉
                UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                firstLoc = lockLine.getFirstLoc();
                lastLoc = lockLine.getLastLoc();

                //然后加锁
                examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);
            }
        }
        //关联变量处理
        LockPolicyPopularize.fixRelevantVar(firstLoc, lastLoc, rwnList.get(0).getThread(), whichCLassNeedSync, lockName, dirPath + "\\" + whichCLassNeedSync);//待定
        System.out.println("对" + rwnList.get(0) + "加锁起止位置" + firstLoc + "->" + lastLoc);
    }

    //读到那一行，然后对字符串处理
    //获取锁的名称
    private static String acquireLockName(ReadWriteNode node) {
        BufferedReader br = null;
        String read = "";//用来读
        String result = "";//用来处理
        int line = 0;
        int poi = Integer.parseInt(node.getPosition().split(":")[1]);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dirPath + "\\" + whichCLassNeedSync)), "UTF-8"));
            while (((read = br.readLine()) != null)) {
                line++;
                if (line == poi) {//找到哪一行
                    String field = node.getField();//得的变量
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\w+)\\." + field);
                    Matcher m = p.matcher(read);
                    if(m.matches()){
                        result = m.group(1);
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
        System.out.println("锁的名字" + result.trim());
        return result.trim();
    }

    //输出锁的名称
    //此处根据pattern读到锁的那行，然后使用字符串匹配
  /*  private static String existLockName(ReadWriteNode node) {
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
            System.out.println("添加信号量");
            addSignal(patternCounter);
        } else {
            System.out.println("添加同步");
            //为长度为2的pattern添加同步
            addSyncPatternOneToThree(patternCounter);
        }
    }


    //对长度为2的pattern添加同步
    private static void addSyncPatternOneToThree(Pattern patternCounter) {

        String existLockName = "";//已有锁的锁名

        /*//检查是否有锁
        for (int i = 0; i < 2; i++) {
            if (CheckWhetherLocked.check(patternCounter.getNodes()[i].getPosition(), patternCounter.getNodes()[i].getField())) {
                //如果有锁，记录下这个锁用来修复其他的
                existLockName = existLockName(patternCounter.getNodes()[i]);
                System.out.println("当前已有的锁" + existLockName);
                break;
            }
        }*/

        int firstLoc = 0, lastLoc = 0;
        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
//            System.out.println(position);
            String[] positionArg = position.split(":");

            //获取要加锁的锁名
            String lockName = acquireLockName(patternCounter.getNodes()[i]);

            //此处就在一行加锁，所以行数一样
            firstLoc = Integer.parseInt(positionArg[1]);
            lastLoc = firstLoc;

            if (!UseASTAnalysisClass.isConstructOrIsMemberVariable(Integer.parseInt(positionArg[1]), Integer.parseInt(positionArg[1]) + 1, dirPath + "\\" + whichCLassNeedSync)) {
                //加锁
                //检查是否存在锁再加锁
                if (!CheckWhetherLocked.check(position, patternCounter.getNodes()[i].getField())) {
                    System.out.println("加锁位置" + Integer.parseInt(positionArg[1]));
                    //判断一下能不能用当前的锁直接进行修复
                    //这里主要是jpf中得不到具体对象的问题，如果能得到的话，就不用这么麻烦了
                    /*if (existLockName.equals(lockName)){
                        examplesIO.addLockToOneVar(Integer.parseInt(positionArg[1]), Integer.parseInt(positionArg[1]) + 1, existLockName, dirPath + "\\" + whichCLassNeedSync);//待定
                    } else {*/
                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();
                    examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);//待定
//                    }
                }
            }
        }
    }

    //添加信号量修复顺序违背
    private static void addSignal(Pattern patternCounter) {
        //得到pattern中较小的行数
        int flagDefineLocation = Integer.MAX_VALUE;//flag应该在哪行定义
        int flagAssertLocation = Integer.MIN_VALUE;//flag应该在那行判断
        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
//            System.out.println(position);
            String[] positionArg = position.split(":");
            flagDefineLocation = Integer.parseInt(positionArg[1]) < flagDefineLocation ? Integer.parseInt(positionArg[1]) : flagDefineLocation;
            flagAssertLocation = Integer.parseInt(positionArg[1]) > flagAssertLocation ? Integer.parseInt(positionArg[1]) : flagAssertLocation;
        }

        System.out.println("信号量定位位置:" + flagDefineLocation);
        System.out.println("信号量使用位置:" + flagAssertLocation);

        //构造函数不能加信号量
        if (!UseASTAnalysisClass.isConstructOrIsMemberVariable(flagAssertLocation, flagAssertLocation, dirPath + "\\" + whichCLassNeedSync) &&
                !UseASTAnalysisClass.isConstructOrIsMemberVariable(flagAssertLocation, flagAssertLocation, dirPath + "\\" + whichCLassNeedSync)) {
            //添加信号量的定义
            examplesIO.addVolatileDefine(flagDefineLocation, "volatile bool flagFix = false;", dirPath + "\\" + whichCLassNeedSync);//待修订


            //添加信号量判断,
            //待定，只执行一句我就加了分号，这样是否可行？
            examplesIO.addVolatileIf(flagAssertLocation, dirPath + "\\" + whichCLassNeedSync);//待修订

            //添加信号为true的那条语句，那条语句应该在定义的后一行
            examplesIO.addVolatileToTrue(flagDefineLocation + 1, dirPath + "\\" + whichCLassNeedSync);//待修订
        }

    }
}
