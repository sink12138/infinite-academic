package com.buaa.academic.analysis.service.impl.fpg.FPTree;

import java.util.ArrayList;
import java.util.Comparator;

public class FPTreeNode {
    private final String content;
    private int support;
    private final FPTreeNode fatherNode;
    private final ArrayList<FPTreeNode> sons;
    private FPTreeNode nextSameNode;

    public FPTreeNode(String content, int support) {
        this.content = content;
        this.support = support;
        this.sons = new ArrayList<>();
        this.fatherNode = null;
        this.nextSameNode = null;
    }

    public FPTreeNode(String content, int support, FPTreeNode fatherNode) {
        this.content = content;
        this.support = support;
        this.fatherNode = fatherNode;
        this.sons = new ArrayList<>();
        this.nextSameNode = null;
    }

    public String getContent() {
        return content;
    }

    public int getSupport() {
        return support;
    }

    public FPTreeNode getFatherNode() {
        return fatherNode;
    }

    public ArrayList<FPTreeNode> getSons() {
        return sons;
    }

    public FPTreeNode getNextSameNode() {
        return nextSameNode;
    }

    public void setNextSameNode(FPTreeNode nextSameNode) {
        this.nextSameNode = nextSameNode;
    }

    public boolean hasFather() {
        return this.fatherNode != null;
    }

    public void addSon(FPTreeNode son) {
        this.sons.add(son);
    }

    public boolean hasNextSameNode(){
        return this.nextSameNode != null;
    }

    public void inc(int i) {
        this.support += i;
    }

    @Override
    public String toString() {
        return this.getContent() + ": " + this.getSupport();
    }
}

class FPTreeNodeSort implements Comparator<FPTreeNode> {
    @Override
    public int compare(FPTreeNode o1, FPTreeNode o2) {
        int result = Integer.compare(o2.getSupport(), o1.getSupport());
        if (result != 0)
            return result;
        return o1.getContent().compareTo(o2.getContent());
    }
}

