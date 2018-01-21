package fix;

import fix.analyzefile.CheckWhetherLocked;

public class Test {

    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        checkWhetherLocked.check("1");
        /*Vector<String> v = new Vector<String>();
        v.add("a");
        v.remove("a");
        for (Object o : v)
            System.out.println(o);*/
    }
}
