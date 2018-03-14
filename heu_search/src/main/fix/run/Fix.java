package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.entity.ImportPath;
import fix.io.ExamplesIO;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;

import java.util.Arrays;
import java.util.List;

public class Fix {
    static ExamplesIO examplesIO = ExamplesIO.getInstance();
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args){
        List<Unicorn.PatternCounter> p = Unicorn.m();
        System.out.println("**********");
        System.out.println(p.get(1).getPattern().getNodes()[0]);
        System.out.println(p.get(1).getPattern().getNodes()[1]);
        divideByLength(p.get(1));
    }

    //根据pattern的长度执行不同的fix策略
    private static void divideByLength(Unicorn.PatternCounter patternCounter) {
        int length = patternCounter.getPattern().getNodes().length;
        if(length == 2){
//            fixPatternOneToThree(patternCounter.getPattern());
        }
        else if(length == 3){
            fixPatternFourToEight(patternCounter);
        }
        else if(length == 4){
            fixPatterNineToSeventeen(patternCounter);
        }
    }

    private static void fixPatterNineToSeventeen(Unicorn.PatternCounter patternCounter) {
        addSyncPatternNineToSeventeen(patternCounter.getPattern());
    }

    private static void addSyncPatternNineToSeventeen(Pattern patternCounter) {
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);
        System.out.println("第四步");
        int[] arrLoc = new int[4];
        for(int i = 0;i < 4;i++){
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");
            arrLoc[i] = Integer.parseInt(positionArg[1]);
        }

        //待定，此处只是排序后将前两个加锁，后两个加锁
        examplesIO.addLockToOneVar(arrLoc[0],arrLoc[1] + 1,"obj",dirPath + "\\Account.java");
        examplesIO.addLockToOneVar(arrLoc[2],arrLoc[3] + 1,"obj",dirPath + "\\Account.java");
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
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);
        int[] arr = new int[3];
        for(int i = 0;i < 3;i++){
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");
            arr[i] = Integer.parseInt(positionArg[1]);
        }
        Arrays.sort(arr);
        System.out.println("first :" + arr[0] + "lastLoc : " + arr[2]);

        //待定，此处只是将前两处加一个锁
        examplesIO.addLockToOneVar(arr[0],arr[1] + 1,"obj",dirPath + "\\Account.java");
        examplesIO.addLockToOneVar(arr[2],arr[2] + 1,"obj",dirPath + "\\Account.java");
    }

    private static void fixPatternOneToThree(Pattern patternCounter) {
//        addSignal(patternCounter);

        //为长度为2的pattern添加同步
        addSyncPatternOneToThree(patternCounter);
    }


    //对长度为2的pattern添加同步
    private static void addSyncPatternOneToThree(Pattern patternCounter) {

        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);

        for(int i = 0; i < 2;i++){
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");

            //加锁
            examplesIO.addLockToOneVar(Integer.parseInt(positionArg[1]),Integer.parseInt(positionArg[1]) + 1,"obj",dirPath + "\\Account.java");//待定

        }

        System.out.println(dirPath + "=============");


    }

    //添加信号量修复顺序违背
    private static void addSignal(Pattern patternCounter) {
        System.out.println("-------------");
        //得到pattern中较小的行数
        int flagDefineLocation = Integer.MAX_VALUE;//flag应该在哪行定义
        int flagAssertLocation = Integer.MIN_VALUE;//flag应该在那行判断
        for(int i = 0; i < 2;i++){
            String position = patternCounter.getNodes()[i].getPosition();
            System.out.println(position);
            String[] positionArg = position.split(":");
            flagDefineLocation = Integer.parseInt(positionArg[1]) < flagDefineLocation ? Integer.parseInt(positionArg[1]):flagDefineLocation;
            flagAssertLocation = Integer.parseInt(positionArg[1]) > flagAssertLocation ? Integer.parseInt(positionArg[1]):flagAssertLocation;

        }

        System.out.println(flagDefineLocation);
        System.out.println(flagAssertLocation);
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);

        //添加信号为true的那条语句
        examplesIO.addVolatileToTrue(flagDefineLocation,dirPath + "\\Account.java");//待修订

        //添加信号量的定义
        examplesIO.addVolatileDefine(flagDefineLocation,"volatile bool flag = false;",dirPath + "\\Account.java");//待修订

        //添加信号量判断
        examplesIO.addVolatileIf(flagAssertLocation,dirPath + "\\Account.java");//待修订
    }

}
