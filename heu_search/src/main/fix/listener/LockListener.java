package fix.listener;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

import java.io.*;
import java.util.Vector;


public class LockListener extends PropertyListenerAdapter{
    public Vector<LocKSequence> LockVector = new Vector<LocKSequence>();//存放遇到的所有锁。
    private String filePath;//要输出的文件的地址
    private String fieldName;//要寻找的参数名
    private Vector<String> oneLockfieldVector = new Vector<String>();

    public Vector<String> getOneLockfieldVector() {
        return oneLockfieldVector;
    }


    public LockListener(String fieldName) {
        super();
        this.fieldName = fieldName;
    }


    public LockListener(String filePath, String fieldName) {
        super();
        this.filePath = filePath;
        this.fieldName = fieldName;
    }


    @Override
    public void objectLocked(VM vm, ThreadInfo currentThread, ElementInfo lockedObject) {
//		System.out.println("输出加锁:" + lockedObject.toString() + "," + currentThread.getName());
        LocKSequence locKSequence = new LocKSequence(lockedObject.toString(),currentThread.getName());
        LockVector.add(locKSequence);

    }

    @Override
    public void searchStarted(Search search) {
        //每次启动前，检查是否有上次的文件残留
        File f = new File(filePath);
        if(f.exists())
            f.delete();
    }

    @Override
    public void searchFinished(Search search) {

    }

    @Override
    public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {
//		System.out.println("输出释放锁:" + unlockedObject.toString() + "," + currentThread.getName());
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath,true)));//往file里面增加内容
            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                //对应当前释放的锁
                if(ls.lockName.equals(unlockedObject.toString())&& currentThread.getName().equals(ls.threadName)){
                    //写入文件
                    bw.write(ls.lockName + "," + ls.threadName + "\n");
                    for(LockElement le : ls.sequence){
                        bw.write(le.toString() + "\n");
                    }
                    bw.write("-----------------\n");

                    //返回变量名
                    if(ls.matchField(fieldName)){
                        Vector<String> v = ls.fieldOnSameLock();
                        for(String s : v){
                            if(!oneLockfieldVector.contains(s)){
                                oneLockfieldVector.add(s);//去重加入
                            }
                        }

                    }

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
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
                                    Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            for(int i = LockVector.size() - 1; i >= 0; i--){//从后往前找
                LocKSequence ls = LockVector.get(i);
                if(ls.lockName.equals(ei.toString()) && currentThread.getName().equals(ls.threadName)){
                    ls.sequence.add(new LockElement(ei.toString(), fi.getName(), currentThread.getName(), fins.getFileLocation()));
                    break;
                }
            }
        }
    }

    //定义LockPath类，用来存放获取锁之后的执行序列
    public static class LocKSequence{
        public String lockName;
        public String threadName;
        public Vector<LockElement> sequence = new Vector<LockElement>();

        public LocKSequence(String lockName, String threadName) {
            this.lockName = lockName;
            this.threadName = threadName;
        }

        //根据输入的name，检查当前的LockSequence是否是需要找的
        public boolean matchField(String fieldName){
            for(LockElement le : this.sequence){
                if(le.field.equals(fieldName))
                    return true;
            }
            return false;
        }

        //返回sequence里面所有的变量,去重
        public Vector<String> fieldOnSameLock(){
            Vector<String> resultVector = new Vector<String>();
            for(LockElement le : this.sequence){
                if(!resultVector.contains(le.field)){
                    resultVector.add(le.field);//去重
                }

            }
            return resultVector;
        }
    }

    //存放锁序列中的每一个变量
    public static class LockElement{
        public String instance;
        public String field;
        public String thread;
        public String location;

        public LockElement(String instance, String field, String thread, String location) {
            super();
            this.instance = instance;
            this.field = field;
            this.thread = thread;
            this.location = location;
        }

        public String toString(){
            return "instance: " + this.instance + "\tfield: " + this.field
                    + "\tthread: " + this.thread + "\tlocation: " + this.location;
        }

    }

}
