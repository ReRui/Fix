package fix.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jdt.core.dom.ASTNode;

public class MatchVariable {
    private Set<String> matchVector = new HashSet<String>();//存放访问的变量用来匹配
    private int startLine;
    private int endLine;
    private int lockNum = 0;

    private ASTNode node = null;//代表父节点

    public Set<String> getMatchVector() {
        return matchVector;
    }
    public void setMatchVector(Set<String> matchVector) {
        this.matchVector = matchVector;
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
        this.matchVector.add(s);
    }

    //判断macthVector是否有元素
    public boolean matchVectorIsEmpty(){
        return this.matchVector.isEmpty();
    }

    //清空
    public void clear(){
        this.matchVector.clear();
        this.node = null;
    }

    //匹配
    public boolean equalTarget(Set<String> target){
        if(this.matchVector.size() != target.size())
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
            ASTNode startNode = this.node;
            ASTNode endNode = node;
            for(ASTNode iNode = this.node; iNode != null; iNode = iNode.getParent()){
                for(ASTNode jNode = node;jNode != null;jNode = jNode.getParent()){
                    if(iNode.equals(jNode)){
                        this.startLine = startNode.getStartPosition();
                        this.endLine = endNode.getStartPosition() + endNode.getLength();
                        return;
                    }
                    endNode = jNode;
                }
                startNode = iNode;
            }

        }

    }
}
