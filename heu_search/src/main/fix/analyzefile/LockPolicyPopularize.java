package fix.analyzefile;

import fix.run.AddLockAfterAcquireVariable;
import p_heu.entity.ReadWriteNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//加锁策略的推广
//就是关联变量加同步
public class LockPolicyPopularize {

    public static void fixRelevantVar(int firstLoc, int lastLoc, String threadName, String className, String lockName) {
        //获取到关联变量
        Set<String> relevantVariabSet = acquireRekevantVar(firstLoc, lastLoc, threadName, className);

        System.out.println("set中的元素");
        for(String s: relevantVariabSet){
            System.out.println(s);
        }
        if(relevantVariabSet.size() > 1){
            //对相关变量加锁
            //set中大于等于两个变量才有加锁的意义
            //不然加锁没意义
            AddLockAfterAcquireVariable.lock(relevantVariabSet, lockName);
        }
    }

    private static Set<String> acquireRekevantVar(int firstLoc, int lastLoc, String threadName, String className) {
        //存放关联变量
        Set<String> relevantVariableSet = new HashSet<String>();
        //拿到sequence序列
        List<ReadWriteNode> nodeSequenceList = RecordSequence.getReadWriteNodeList();
        //根据起始和终止位置，加上线程名，找出加的锁中的所有共享变量
        //很显然是在原来的sequence序列中找
        for (ReadWriteNode node : nodeSequenceList) {
            //首先得到行数
            int poi = Integer.parseInt(node.getPosition().split(":")[1]);
            //然后得到线程
            String nodeThread = node.getThread();
            //判断行数在不在这之间，是不是同一个线程
            //考虑线程是因为有可能会有其他线程在这里操作
            //还有考虑是不是同一个java文件，之前就因为没考虑这个而出错
            String nowCLassName = node.getPosition().split(":")[0].split("/")[1];
            if (poi >= firstLoc && poi <= lastLoc && nodeThread.equals(threadName) && nowCLassName.equals(className)) {
//                System.out.println(node + "只是测试");
                relevantVariableSet.add(node.getField());
            } else {
                continue;
            }
        }
        return relevantVariableSet;
    }
}
