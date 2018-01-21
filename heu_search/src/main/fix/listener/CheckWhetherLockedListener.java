package fix.listener;

import fix.entity.lock.LocKSequence;
import fix.entity.lock.LockElement;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class CheckWhetherLockedListener extends PropertyListenerAdapter {
    public Vector<LocKSequence> LockVector = new Vector<LocKSequence>();//存放遇到的所有锁。
    private String filePath;//要输出的文件的地址
    private String fieldName;//要寻找的参数名
    private String fieldLoc;// 变量的具体位置
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

    @Override
    public void objectLocked(VM vm, ThreadInfo currentThread, ElementInfo lockedObject) {
//        System.out.println("输出加锁:" + lockedObject.toString() + "\t" + currentThread.getName() + "\t");
        LocKSequence locKSequence = new LocKSequence(lockedObject.toString(),currentThread.getName());
        LockVector.add(locKSequence);
    }


    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            String res = fins.getFileLocation();
           /* String[] className = res.split("/");
            System.out.println("hah"+className[className.length -  1]);
            if(className[className.length -  1].contains("Test") *//*&& LockVector.size() > 0*//*){
                System.out.println("里面的是" +ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());//输出锁中的所有信息
                System.out.println(LockVector.get(LockVector.size() - 1).lockName + "结果v");
                System.out.println(LockVector.size());
            }*/
            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                if(ls.lockName.equals(ei.toString()) && currentThread.getName().equals(ls.threadName)){
                    ls.sequence.add(new LockElement(ei.toString(), fi.getName(), currentThread.getName(), fins.getFileLocation()));
                    break;
                }
            }
        }
    }


    @Override
    public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {
        for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
            LocKSequence ls = LockVector.get(i);
            //找到当前对应当前释放的锁
            if(ls.lockName.equals(unlockedObject.toString())&& currentThread.getName().equals(ls.threadName)){
                //寻找当前锁中有没有需要寻找的变量
                for(LockElement le : ls.sequence){
                    System.out.println("锁里面的内容:" +le.toString());
                    if(le.field.equals(fieldName) && le.location.equals(fieldLoc)){
                        System.out.println("*************" + le.toString());
                        this.checkFlag = true;
                        break;
                    }
                }
                //清空当前释放锁里面的内容
                LockVector.get(i).clearAll();
                LockVector.remove(i);
            }
        }
//        System.out.println("输出释放锁:" + unlockedObject.toString() + "," + currentThread.getName() );
/*        LocKSequence unlock = new LocKSequence(unlockedObject.toString(),currentThread.getName());
        LockVector.remove(unlock);
        for(int i = LockVector.size() - 1; i >= 0;i--){
            if(LockVector.get(i).lockName.equals(unlockedObject.toString()) && LockVector.get(i).threadName.equals(currentThread.getName()))
                LockVector.remove(i);
        }
        System.out.println("输出释放锁:" + unlockedObject.toString() + "," + currentThread.getName() );
        System.out.println("释放锁后的长度:" + LockVector.size());
*/
    }
}
