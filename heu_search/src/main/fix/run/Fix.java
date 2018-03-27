package fix.run;

import fix.analyzefile.*;
import fix.entity.ImportPath;
import fix.entity.lock.ExistLock;
import fix.entity.type.FixType;
import fix.io.ExamplesIO;
import fix.io.InsertCode;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Fix {
    static ExamplesIO examplesIO = ExamplesIO.getInstance();
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;//第一次修复的文件路径
    static String iterateDirPath = ImportPath.examplesRootPath + "\\exportExamples\\" + ImportPath.projectName;//迭代修复的文件路径

    static String whichCLassNeedSync = "";//需要添加同步的类，此处需不需考虑在不同类之间加锁的情况？
    static LockAdjust lockAdjust = LockAdjust.getInstance();//当锁交叉时，用来合并锁

    static String fixMethods = "";//记录修复方法，写入文件中

    static String sourceClassPath = "";//源代码的生成类，记录下来，以后用jpf分析class

    public static void main(String[] args) {
        fix(FixType.firstFix);
//        fix(FixType.iterateFix);
    }

    private static void fix(int type) {
        String verifyClasspath = ImportPath.verifyPath + "\\generateClass";//要验证的class路径
        if (type == FixType.firstFix) {
            //先将项目拷贝到exportExamples
            dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);
            sourceClassPath = ImportPath.examplesRootPath + "\\out\\production\\Patch";
        } else if (type == FixType.iterateFix) {
            dirPath = iterateDirPath;
            sourceClassPath = ImportPath.verifyPath + "\\generateClass";
        }

        //拿到第一个元素
        Unicorn.PatternCounter patternCounter = Unicorn.getPatternCounterList(sourceClassPath).get(0);

        //将拿到的pattern写入文件中
        InsertCode.writeLogFile(patternCounter.toString(), "修复得到的pattern");

        //拿到该pattern对应的sequence
        //第一次在失败运行中出现的sequence
        RecordSequence.display(patternCounter.getFirstFailAppearPlace());

        //将sequence写入文件中
        InsertCode.writeLogFile(patternCounter.getFirstFailAppearPlace().toString(), "修复得到的sequence");

        //根据pattern知道需要在哪个类中加锁
        whichCLassNeedSync = patternCounter.getPattern().getNodes()[0].getPosition().split(":")[0].split("/")[1];

        //对拷贝的项目进行修复
        divideByLength(patternCounter);

        //检测修复完的程序是否正确
        fixMethods += "结果: ";
        if (Unicorn.verifyFixSuccessful(verifyClasspath)) {
            fixMethods += "修复成功";
        } else {
            fixMethods += "修复失败";
        }

        //将修复方法写入文件中
        InsertCode.writeLogFile(fixMethods, "修复方法及结果");
    }

    //根据pattern的长度执行不同的fix策略
    private static void divideByLength(Unicorn.PatternCounter patternCounter) {
        int length = patternCounter.getPattern().getNodes().length;
        if (length == 2) {
            fixMethods += "修复一\n";
            fixPatternOneToThree(patternCounter.getPattern());
        } else if (length == 3) {
            fixMethods += "修复二\n";
            fixPatternFourToEight(patternCounter.getPattern());
        } else if (length == 4) {
            fixMethods += "修复三\n";
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

        addSynchronized(threadA);
        addSynchronized(threadB);
    }

    //长度为3添加同步
    private static void fixPatternFourToEight(Pattern patternCounter) {
        //根据线程将三个结点分为两个list
        List<ReadWriteNode> threadA = new ArrayList<ReadWriteNode>();//线程A的结点
        List<ReadWriteNode> threadB = new ArrayList<ReadWriteNode>();//线程B的结点
        String threadName = "";
        for (int i = 0; i < patternCounter.getNodes().length; i++) {
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
        addSynchronized(threadA);
        lockAdjust.setOneLockFinish(true);//表示第一次执行完
        addSynchronized(threadB);
        lockAdjust.adjust(dirPath + "\\" + whichCLassNeedSync);//合并锁
    }

    private static void addSynchronized(List<ReadWriteNode> rwnList) {
        int firstLoc = 0, lastLoc = 0;

        String lockName = "";
        //判断A中有几个变量
        if (rwnList.size() > 1) {//两个变量
            //如果有两个变量，需要分析
            //判断它们在不在一个函数中
            boolean flagSame = UseASTAnalysisClass.assertSameFunction(rwnList, dirPath + "\\" + whichCLassNeedSync);
//            System.out.println("判断在不在同一个函数" + flagSame);
            if (flagSame) {//在一个函数中
                //先找找原来有没有锁
                boolean varHasLock = false;//记录当前pattern是否加锁
                ExistLock existLock = null;
                //判断它们有没有加锁，需要加何种锁，加锁位置
                //对A的list分析
                for (int i = 0; i < rwnList.size(); i++) {
                    ReadWriteNode node = rwnList.get(i);
                    if (CheckWhetherLocked.check(node.getPosition(), node.getField(), sourceClassPath)) {//检查是否存在锁
                        if (i == 1 && varHasLock == true) {//表示两个都有锁
                            return;//直接结束
                        } else {
                            varHasLock = true;//有锁标为true
                            existLock = existLockName(rwnList.get(i));
                        }
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
                //判断加锁区域在不在构造函数，或者加锁变量是不是成员变量
                if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync)) {

                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();

                    //两个地方都没有加锁
                    if (!varHasLock) {
                        //加锁
                        examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);
                    } else {//有加锁的，直接修改原有锁
                        UseOldSyncToFix.adjustOldSync(existLock.getLockName(), firstLoc, lastLoc, existLock.getStartLine(), existLock.getEndLine(), dirPath + "\\" + whichCLassNeedSync);
                    }
                }
            } else {//不在一个函数中
                for (int i = 0; i < rwnList.size(); i++) {
                    ReadWriteNode node = rwnList.get(i);
                    firstLoc = Integer.parseInt(node.getPosition().split(":")[1]);
                    lastLoc = firstLoc;
                    //每个都检查是不是加锁
                    if (!CheckWhetherLocked.check(node.getPosition(), node.getField(), sourceClassPath)) {
                        //然后检查是不是成员变量或构造函数
                        if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync)) {
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
            if (!CheckWhetherLocked.check(node.getPosition(), node.getField(), sourceClassPath)) {
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

        //记录加锁位置
        //便于以后调整
        if (!lockAdjust.isOneLockFinish()) {
            lockAdjust.setOneLockName(lockName);
            lockAdjust.setOneFirstLoc(firstLoc);
            lockAdjust.setOneLastLoc(lastLoc + 1);
        } else {
            lockAdjust.setTwoLockName(lockName);
            lockAdjust.setTwoFirstLoc(firstLoc);
            lockAdjust.setTwoLastLoc(lastLoc + 1);
        }

        //关联变量处理
        LockPolicyPopularize.fixRelevantVar(firstLoc, lastLoc, rwnList.get(0).getThread(), whichCLassNeedSync, lockName, dirPath + "\\" + whichCLassNeedSync);//待定
        //表示能加锁
        if (firstLoc > 0 && lastLoc > 0) {
            fixMethods += "对" + rwnList.get(0) + "加锁起止位置" + firstLoc + "->" + lastLoc + '\n';
        }
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
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\s+(\\w+)\\." + field + ".*$");
                    Matcher m = p.matcher(read);
                    if (m.matches()) {
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
        fixMethods += "锁的名字" + result.trim() + '\n';
        return result.trim();
    }

    //输出锁的名称
    private static ExistLock existLockName(ReadWriteNode node) {
        int number = Integer.parseInt(node.getPosition().split(":")[1]);
        String filepath = dirPath + "\\" + whichCLassNeedSync;
        ExistLock existLock = UseASTAnalysisClass.useASTCFindLockLine(node, filepath);
        existLock = AcquireSyncName.acquireSync(existLock, filepath);
        return existLock;
    }

    private static void fixPatternOneToThree(Pattern patternCounter) {
        ReadWriteNode readNode = null;
        ReadWriteNode writeNode = null;
        for (int i = 0; i < 2; i++) {
            if (patternCounter.getNodes()[i].getType().equals("READ")) {
                readNode = patternCounter.getNodes()[i];
            } else if (patternCounter.getNodes()[i].getType().equals("WRITE")) {
                writeNode = patternCounter.getNodes()[i];
            }
        }
        /*System.out.println("read" + readNode);
        System.out.println("write" + writeNode);
        System.out.println(!RecordSequence.isLast(readNode));
        System.out.println(!RecordSequence.isFirst(writeNode));*/
        if (readNode != null && writeNode != null && (!RecordSequence.isLast(readNode) || !RecordSequence.isFirst(writeNode))) {
            fixMethods += "添加同步\n";
            //为长度为2的pattern添加同步
            addSyncPatternOneToThree(patternCounter);
        } else {
            //为长度为2的pattern添加同步
            fixMethods += "添加信号量\n";
            addSignal(patternCounter);
        }
    }


    //对长度为2的pattern添加同步
    private static void addSyncPatternOneToThree(Pattern patternCounter) {

        /*List<String> existLockName = new ArrayList<String>();//已有锁的锁名

        //检查是否有锁
        for (int i = 0; i < 2; i++) {
            if (CheckWhetherLocked.check(patternCounter.getNodes()[i].getPosition(), patternCounter.getNodes()[i].getField(), sourceClassPath)) {
                //如果有锁，记录下这个锁用来修复其他的
                existLockName.add(existLockName(patternCounter.getNodes()[i]));
            }
        }*/

        int firstLoc = 0, lastLoc = 0;
        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
//            System.out.println(position);
            String[] positionArg = position.split(":");

            //获取要加锁的锁名
            //如果已有锁，直接用现有的锁
            //如果没有，再寻找新锁
            String lockName = acquireLockName(patternCounter.getNodes()[i]);

            //此处就在一行加锁，所以行数一样
            firstLoc = Integer.parseInt(positionArg[1]);
            lastLoc = firstLoc;

            if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(Integer.parseInt(positionArg[1]), Integer.parseInt(positionArg[1]) + 1, dirPath + "\\" + whichCLassNeedSync)) {
                //加锁
                //检查是否存在锁再加锁
                if (!CheckWhetherLocked.check(position, patternCounter.getNodes()[i].getField(), sourceClassPath)) {
                    fixMethods += "加锁位置" + Integer.parseInt(positionArg[1]) + '\n';
                    //判断一下能不能用当前的锁直接进行修复
                    //这里主要是jpf中得不到具体对象的问题，如果能得到的话，就不用这么麻烦了
                    /*if (existLockName.equals(lockName)){
                        examplesIO.addLockToOneVar(Integer.parseInt(positionArg[1]), Integer.parseInt(positionArg[1]) + 1, existLockName, dirPath + "\\" + whichCLassNeedSync);//待定
                    } else {*/

                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, dirPath + "\\" + whichCLassNeedSync);
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();
                    if (!lockAdjust.isOneLockFinish()) {
                        lockAdjust.setOneLockName(lockName);
                        lockAdjust.setOneFirstLoc(firstLoc);
                        lockAdjust.setOneLastLoc(lastLoc + 1);
                        lockAdjust.setOneLockFinish(true);
                    } else {
                        lockAdjust.setTwoLockName(lockName);
                        lockAdjust.setTwoFirstLoc(firstLoc);
                        lockAdjust.setTwoLastLoc(lastLoc + 1);
                    }
                    examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, dirPath + "\\" + whichCLassNeedSync);//待定

                    lockAdjust.adjust(dirPath + "\\" + whichCLassNeedSync);
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

        fixMethods += "信号量定义位置:" + flagDefineLocation + '\n';
        fixMethods += "信号量使用位置:" + flagAssertLocation + '\n';

        //构造函数不能加信号量
        if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(flagAssertLocation, flagAssertLocation, dirPath + "\\" + whichCLassNeedSync) &&
                !UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(flagAssertLocation, flagAssertLocation, dirPath + "\\" + whichCLassNeedSync)) {
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
