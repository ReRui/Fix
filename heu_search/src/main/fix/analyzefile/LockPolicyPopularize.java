package fix.analyzefile;

import p_heu.entity.ReadWriteNode;

import java.util.ArrayList;
import java.util.List;

//加锁策略的推广
//就是关联变量加同步
public class LockPolicyPopularize {

    public static List<String> relevantVar(int firstLoc, int lastLoc, String threadName) {
        //存放关联变量
        List<String> relevantVariableList = new ArrayList<String>();
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
            if (poi >= firstLoc && poi <= lastLoc && nodeThread.equals(threadName)) {
//                System.out.println(node + "只是测试");
                relevantVariableList.add(node.getField());
            } else {
                continue;
            }
        }
        return relevantVariableList;
    }
}
