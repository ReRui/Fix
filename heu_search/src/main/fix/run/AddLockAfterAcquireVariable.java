package fix.run;

import fix.analyzefile.AcquireVariableInSameLock;
import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.ExamplesIO;
import fix.io.InsertCode;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class AddLockAfterAcquireVariable {
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;
    static Set<String> variableVector = new HashSet<String>();
    static String className = "";//类的名字，以后用来比较用
    //chanage file content to buffer array
    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while((line = br.readLine()) != null) {
                // append the content and the lost new line.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length()-1, contents, 0);

            assert(contents.length > 0);
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return contents;
    }

    public static void main(String[] args){

        //获取相关变量
        AcquireVariableInSameLock acquireVariableInSameLock = new AcquireVariableInSameLock();
        Vector<String> v = acquireVariableInSameLock.getOneLockfieldVector();
        for (String s : v)
            variableVector.add(s);

        //单例
        ExamplesIO examplesIO = ExamplesIO.getInstance();
        //将项目从examples复制到exportExamples，并且修改当前dirPath路径
        dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples", dirPath);
        //对目录下的每个文件，都执行一次lock
        File file = new File(dirPath);
        File[] fileArr = file.listFiles();
        for(File f : fileArr){
            String listFile = f.getPath();
            lock(listFile);
        }


    }

    public static void lock(String filePath) {
        MatchVariable matchVariable = new MatchVariable();

        InsertCode.insert(3, "import java.util.concurrent.locks.ReentrantLock;" + '\n', filePath);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        cu.accept(new ASTVisitor() {

            Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰

            public boolean visit(TypeDeclaration  node){
                className = node.getName().toString();
                return true;
            }

            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
//                    System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));

                    boolean flag = false;
                    for(String s : variableVector){
                        if(s.equals(node.getIdentifier()))
                            flag = true;
                    }
                    if(flag){

                        if(matchVariable.matchSetIsEmpty()){
                            matchVariable.addMatchVector(node.getIdentifier());
                            matchVariable.setNode(node.getParent());
                        }
                        else{
                            //如果有数据则需要往里面添加数据，对于相同的变量，后面的一个变量应该覆盖前面的，在次使用改变父节点的方法
                            if(matchVariable.getMatchSet().contains(node.getIdentifier())){
                                matchVariable.setNode(node.getParent());
                            }else{
                                matchVariable.addMatchVector(node.getIdentifier());
//                                System.out.println("test" + node.getParent());
                                matchVariable.searchSame(node.getParent());
                            }
                        }
                        if(matchVariable.equalTarget(variableVector)){
//                           System.out.println("匹配成功");
//                           System.out.println("开始" + cu.getLineNumber(matchVariable.getStartLine()));//下一行
//                           System.out.println("结束" + cu.getLineNumber(matchVariable.getEndLine() + 1));

                            //不是成员变量且不是在构造函数里
                            if(!(matchVariable.getNode().getParent() instanceof TypeDeclaration) && !(isConstruct(matchVariable.getNode().getParent()))){
                                //加锁
                                InsertCode.insert(cu.getLineNumber(matchVariable.getStartLine()), "ReentrantLock lock" + matchVariable.getLockNum() + " = new ReentrantLock(true);lock" + matchVariable.getLockNum() + ".lock();"
                                        + " synchronized (lock" + matchVariable.getLockNum() +"){ ", filePath);
                                InsertCode.insert(cu.getLineNumber(matchVariable.getEndLine() + 1), " }", filePath);
                                //更新锁
                                matchVariable.update();
                            }
                            //清空
                            matchVariable.clear();
                        }
                    }
                    flag= false;
                }

                return true;
            }


        });
    }

    private static boolean isConstruct(ASTNode parent) {

        /**
         * 检查结点的所有父节点，看看有没有一个是构造函数
         * 判断节点类型是函数，节点名字与类名相同，则是构造函数
         */
        while(!(parent instanceof TypeDeclaration)){
            if((parent instanceof MethodDeclaration) && (((MethodDeclaration) parent).getName().toString().equals(className))) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

}
