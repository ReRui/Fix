package fix.entity;
/**
 * 这是一个用来完成匹配相关变量的类
 */

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.HashSet;
import java.util.Set;

public class MatchVariable {
    private Set<String> matchSet = new HashSet<String>();//存放访问的变量用来匹配
    private int startLine;
    private int endLine;
    private int lockNum = 0;

    private ASTNode node = null;//代表父节点

    public Set<String> getMatchSet() {
        return matchSet;
    }
    public void setMatchSet(Set<String> matchSet) {
        this.matchSet = matchSet;
    }
    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }
    public ASTNode getNode() {
        return node;
    }
    public int getLockNum() {
        return lockNum;
    }
    public void setNode(ASTNode node) {
        this.node = node;
    }
    //往vector里面添加元素
    public void addMatchVector(String s){
        this.matchSet.add(s);
    }

    //判断macthSet是否有元素
    public boolean matchSetIsEmpty(){
        return this.matchSet.isEmpty();
    }

    //清空
    public void clear(){
        this.matchSet.clear();
        this.node = null;
    }

    //匹配
    public boolean equalTarget(Set<String> target){
        if(this.matchSet.size() != target.size())
            return false;

        return true;
    }

    public void update() {
        lockNum++;
    }



    //寻找两个ASTnode不同节点相同的父节点
    public void searchSame(ASTNode node){
        if(this.node.equals(node))
            return ;
        else{
            ASTNode saveNode = this.node;//用来操作的node
            ASTNode startNode = saveNode;
            ASTNode endNode = node;
            for(ASTNode iNode = saveNode; iNode != null; iNode = iNode.getParent()){
                for(ASTNode jNode = node;jNode != null;jNode = jNode.getParent()){
                    if(iNode.equals(jNode)){
                        this.startLine = startNode.getStartPosition();
                        this.endLine = endNode.getStartPosition() + endNode.getLength();
                        this.node = startNode;
                        return;
                    }
                    endNode = jNode;
                }
                startNode = iNode;
            }
        }
    }
}
