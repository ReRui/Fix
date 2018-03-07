package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.entity.ImportPath;
import fix.io.ExamplesIO;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;

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
            fixPatternOneToThree(patternCounter.getPattern());
        }
        else if(length == 3){
            fixPatternFourToEight(patternCounter);
        }
        else if(length == 4){
            fixPatterNineToSeventeen(patternCounter);
        }
    }

    private static void fixPatterNineToSeventeen(Unicorn.PatternCounter patternCounter) {

    }

    private static void fixPatternFourToEight(Unicorn.PatternCounter patternCounter) {
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //获取读写节点
        ReadWriteNode[] nodesArr = patternCounter.getPattern().getNodes();

        //如果该变量没有加锁则引入一个新锁,并且添加同步
        if(checkWhetherLocked.check(nodesArr[1].getPosition(),nodesArr[1].getElement())){//j没被加锁

        }
        //直接添加同步
        else{

        }
    }

    private static void fixPatternOneToThree(Pattern patternCounter) {
        System.out.println("-------------");
        //得到变量位置
        System.out.println(patternCounter.getNodes()[0].getPosition());
        //添加信号量
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);
        examplesIO.addVolatileDefine(16,"flag",dirPath + "\\Account.java");
    }

}
