package fix.listener;

import fix.entity.lock.LocKSequence;
import fix.entity.lock.LockElement;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

import java.util.*;

public class CheckWhetherLockedListener extends PropertyListenerAdapter {
    public Vector<LocKSequence> lockVector = new Vector<LocKSequence>();//存放遇到的所有锁。
    private String filePath;//要输出的文件的地址
    private String fieldName;//要寻找的参数名一
    private String fieldLoc;// 变量的具体位置一
    private String fieldNameTwo;//要寻找的参数名二
    private String fieldLocTwo;// 变量的具体位置二
    private boolean checkFlag = false;

    public boolean isCheckFlag() {
        return checkFlag;
    }

    public CheckWhetherLockedListener() {
    }

    public CheckWhetherLockedListener(String filePath, String fieldName, String fieldLoc) {
        this.filePath = filePath;
        this.fieldName = fieldName;
        this.fieldLoc = fieldLoc;
    }

    public CheckWhetherLockedListener(String filePath, String fieldName, String fieldLoc, String fieldNameTwo, String fieldLocTwo) {
        this.filePath = filePath;
        this.fieldName = fieldName;
        this.fieldLoc = fieldLoc;
        this.fieldNameTwo = fieldNameTwo;
        this.fieldLocTwo = fieldLocTwo;
    }

    @Override
    public void objectLocked(VM vm, ThreadInfo currentThread, ElementInfo lockedObject) {
//        System.out.println("输出加锁:" + lockedObject.toString() + "\t" + currentThread.getName() + "\t");
        LocKSequence locKSequence = new LocKSequence(lockedObject.toString(),currentThread.getName());
        lockVector.add(locKSequence);
    }


    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            String res = fins.getFileLocation();
            //将每次变量都添加进去
            //判断里面有没有
            boolean flag = true;

           /* String[] className = res.split("/");
            System.out.println("hah"+className[className.length -  1]);
            if(className[className.length -  1].contains("Test") *//*&& lockVector.size() > 0*//*){
                System.out.println("里面的是" +ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());//输出锁中的所有信息
                System.out.println(lockVector.get(lockVector.size() - 1).lockName + "结果v");
                System.out.println(lockVector.size());
            }*/
//            System.out.println("里面的是" +ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());//输出锁中的所有信息
            for(int i = lockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = lockVector.get(i);
                if(ls.lockName.equals(ei.toString()) && currentThread.getName().equals(ls.threadName)){
                    ls.sequence.add(new LockElement(ei.toString(), fi.getName(), currentThread.getName(), fins.getFileLocation()));
                    break;
                }
            }
        }
    }


    @Override
    public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {
        boolean checkOne = false;
        boolean checkTwo = false;
        for(int i = lockVector.size() - 1; i >= 0; i--){//从后往前找
            LocKSequence ls = lockVector.get(i);
            //找到当前对应当前释放的锁
            if(ls.lockName.equals(unlockedObject.toString())&& currentThread.getName().equals(ls.threadName)){
                //寻找当前锁中有没有需要寻找的变量
                for(LockElement le : ls.sequence){
//                    System.out.println("锁里面的实例:" +le.instance);
                    //线检查有没变量
                    if(le.field.equals(fieldName) && le.location.equals(fieldLoc)){
//                        System.out.println("*************" + le.toString());
                        checkOne = true;
//                        System.out.println("1有");
                    }
                    if(le.field.equals(fieldNameTwo) && le.location.equals(fieldLocTwo)){
//                        System.out.println("*************" + le.toString());
                        checkTwo = true;
//                        System.out.println("2有");
                    }
                    if(checkOne == true && checkTwo == true){
                        checkFlag = true;
                        break;
                    }
                }
                //清空当前释放锁里面的内容
                lockVector.get(i).clearAll();
                lockVector.remove(i);
            }
        }
//        System.out.println("输出释放锁:" + unlockedObject.toString() + "," + currentThread.getName() );
/*        LocKSequence unlock = new LocKSequence(unlockedObject.toString(),currentThread.getName());
        lockVector.remove(unlock);
        for(int i = lockVector.size() - 1; i >= 0;i--){
            if(lockVector.get(i).lockName.equals(unlockedObject.toString()) && lockVector.get(i).threadName.equals(currentThread.getName()))
                lockVector.remove(i);
        }
        System.out.println("输出释放锁:" + unlockedObject.toString() + "," + currentThread.getName() );
        System.out.println("释放锁后的长度:" + lockVector.size());
*/
    }
}
