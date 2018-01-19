package fix.listener;

import fix.entity.lock.LocKSequence;
import fix.entity.lock.LockElement;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

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
//        System.out.println("输出加锁:" + lockedObject.toString() + "," + currentThread.getName());
        LocKSequence locKSequence = new LocKSequence(lockedObject.toString(),currentThread.getName());
        LockVector.add(locKSequence);
    }

    @Override
    public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {

    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                if(ls.lockName.equals(ei.toString()) && currentThread.getName().equals(ls.threadName)){
                    //找到对应的锁之后，找锁中有没有需要找的变量
                    if(fi.getName().equals(fieldName) && fins.getFileLocation().equals(fieldLoc)){
                        checkFlag = true;
                        System.out.println(ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());
                        break;
                    }

//                    ls.sequence.add(new LockElement(ei.toString(), fi.getName(), currentThread.getName(), fins.getFileLocation()));

                }
            }
        }
    }
}
