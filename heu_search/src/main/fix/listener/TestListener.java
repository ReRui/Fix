package fix.listener;

import fix.entity.lock.LocKSequence;
import fix.entity.lock.LockElement;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

public class TestListener extends PropertyListenerAdapter {
    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);
            String res = fins.getFileLocation();

            System.out.println("里面的是" +ei.toString() + "," + fi.getName() + ","+ (fins.isRead() ? "READ" : "WRITE") + "," + currentThread.getName() + "," + fins.getFileLocation());//输出锁中的所有信息
            System.out.println("class : " + executedInstruction.getClass() + "," + "bytecode:" + executedInstruction.getByteCode());

        }
    }
}
