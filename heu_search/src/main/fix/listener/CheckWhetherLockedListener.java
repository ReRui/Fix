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
        System.out.println("输出加锁:" + lockedObject.toString() + "," + currentThread.getName());
        LocKSequence locKSequence = new LocKSequence(lockedObject.toString(),currentThread.getName());
        LockVector.add(locKSequence);
    }

    @Override
    public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath,true)));//往file里面增加内容
            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                //对应当前释放的锁
                if(ls.lockName.equals(unlockedObject.toString())&& currentThread.getName().equals(ls.threadName)){
                    //写入文件
                    bw.write(ls.lockName + "\t" + ls.threadName + "\n");
                    for(LockElement le : ls.sequence){
                        bw.write(le.toString() + "\n");
                    }
                    bw.write("-----------------\n");

                    //清空对应的sequence
//					ls.sequence.clear();
                    break;
                }
            }
            bw.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            String res = fins.getFileLocation();
            String[] className = res.split("/");
//            System.out.println("hah"+className[className.length -  1]);
            if(className[className.length -  1].contains("Test")){
                System.out.println("里面的是" +ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());//输出锁中的所有信息
                System.out.println(LockVector.get(LockVector.size() - 1).lockName + "结果v");
            }
/*            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                if(ls.lockName.equals(ei.toString()) && currentThread.getName().equals(ls.threadName)){
//                    System.out.println("里面的是" +  "," + ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());
                    //找到对应的锁之后，找锁中有没有需要找的变量
                    if(fi.getName().equals(fieldName) && fins.getFileLocation().equals(fieldLoc)){
                        checkFlag = true;
                        System.out.println(ei.toString() + "," + fi.getName() + "," + currentThread.getName() + "," + fins.getFileLocation());
                        break;
                    }

//                    ls.sequence.add(new LockElement(ei.toString(), fi.getName(), currentThread.getName(), fins.getFileLocation()));

                }
            }*/
        }
    }
}
