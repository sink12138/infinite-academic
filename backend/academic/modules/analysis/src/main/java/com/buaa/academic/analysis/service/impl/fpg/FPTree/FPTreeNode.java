package com.buaa.academic.analysis.service.impl.fpg.FPTree;

import java.util.ArrayList;

public class FPTreeNode implements Comparable<FPTreeNode> {
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

    @Override
    public int compareTo(FPTreeNode another) {
        int result = Integer.compare(another.getSupport(), this.getSupport());
        if (result != 0)
            return result;
        return this.getContent().compareTo(another.getContent());
    }
}
