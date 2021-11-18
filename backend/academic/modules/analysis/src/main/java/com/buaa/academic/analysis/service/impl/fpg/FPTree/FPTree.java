package com.buaa.academic.analysis.service.impl.fpg.FPTree;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class FPTree {
    private double supportNum;

    public FPTree() {}

    public FPTree(double support) {
        super();
        if(support >= 1)
            this.supportNum = support;
    }

    public LinkedList<FPTreeNode> buildHeaderTable(ArrayList<ArrayList<String>> transactions){
        LinkedList<FPTreeNode> headerTable = new LinkedList<>();
        HashMap<String, FPTreeNode> map = new HashMap<>();
        //使用HashMap保存项名称及结点，使用结点的原因为方便计数和排序
        for(ArrayList<String> items: transactions){
            for(String item: items){
                if(map.containsKey(item))
                    map.get(item).inc(1);
				else {
                    FPTreeNode node = new FPTreeNode(item, 1);
                    map.put(item, node);
                }
            }
        }
        for(String name: map.keySet()){
            FPTreeNode node = map.get(name);
            if(node.getSupport() >= supportNum)
            headerTable.add(node);
        }

        headerTable.sort(new FPTreeNodeSort());			//使用Collections的sort方法，需要对TreeNode类实现一个排序类TreeNodeSort
        return headerTable;
    }

    public FPTreeNode buildFPTree(ArrayList<ArrayList<String>> transactions, LinkedList<FPTreeNode> headerTable) {
        FPTreeNode root = new FPTreeNode("root", 0, null);				//树根结点，计数为0，无父结点

        for(ArrayList<String> items: transactions){
            FPTreeNode parent = root;
            for(String item: items){
                FPTreeNode itemNode = exist(item, parent);			//根据父结点判断当前结点是否存在，并获取正确的结点
                addSameNode(headerTable, itemNode);					//得到结点后，放入项头表中
                parent = itemNode;									//父结点动态变化
            }
        }
        return root;
    }

    public void addSameNode(LinkedList<FPTreeNode> headerTable, FPTreeNode itemNode) {
        for(FPTreeNode head: headerTable)
            if(head.getContent().equals(itemNode.getContent())) {
                while(head.hasNextSameNode()){
                    head = head.getNextSameNode();					//寻找对应的项的链表，找到其指向的最后一个相同结点
                    if(head == itemNode)							//若遍历链表时发现当前结点已存在链式关系，即无需添加，直接返回
                        return;
                }
                head.setNextSameNode(itemNode);
                return;
            }
    }

    public FPTreeNode exist(String item, FPTreeNode parent) {
        for(FPTreeNode child: parent.getSons())
            if(child.getContent().equals(item)){
                child.inc(1);
                return child;
            }

        FPTreeNode node = new FPTreeNode(item, 1, parent);				//注意新创建结点后为当前结点添加父结点
        parent.addSon(node);										//父结点的子结点列表中也要添加当前结点
        return node;
    }

    public ArrayList<String> getCPB(FPTreeNode node){
        ArrayList<String> transaction = new ArrayList<>();
        FPTreeNode parent = node;
        while(parent.hasFather()){
            if(parent.getFatherNode().getContent().equals("root"))
                break;
            parent = parent.getFatherNode();
            transaction.add(parent.getContent());
        }

        return transaction;
    }

    public ArrayList<ArrayList<String>> sortByFreqItem(ArrayList<ArrayList<String>> transactions,
                                                       LinkedList<FPTreeNode> itemSortByFreq) {
        //保存排序后的所有事务
        ArrayList<ArrayList<String>> sortedTransactions = new ArrayList<>();
        for(ArrayList<String> transaction: transactions){
            ArrayList<String> sortedItem = new ArrayList<>();
            int itemNum = transaction.size();
            for(FPTreeNode node: itemSortByFreq){						//对排序后的频繁一项集遍历
                if(transaction.contains(node.getContent())){			//若当前事务中存在该频繁一项集，则保存
                    sortedItem.add(node.getContent());
                    itemNum--;
                }
                if(itemNum == 0)									//用以计数避免无用的循环
                    break;
            }
            sortedTransactions.add(sortedItem);
        }
        return sortedTransactions;
    }

    public ArrayList<String> fp_growth(ArrayList<ArrayList<String>> transactions, String item){
        ArrayList<String> freqItems = new ArrayList<>();		//保存频繁项集
        LinkedList<FPTreeNode> itemSortByFreq = buildHeaderTable(transactions);
        transactions = sortByFreqItem(transactions, itemSortByFreq);
        FPTreeNode root = buildFPTree(transactions, itemSortByFreq);
        //对于当前项和事务，生成其对应的FP树和项头表
        if(itemSortByFreq.size() == 0 || root == null)
            return freqItems;

        if(item == null){
            for(FPTreeNode node: itemSortByFreq)
                freqItems.add(node.getContent() + ":" + node.getSupport());
        }
        else{
            for(int i = itemSortByFreq.size() - 1; i >= 0; i--){
                FPTreeNode node = itemSortByFreq.get(i);
                freqItems.add(node.getContent() + FPGMainClass.splitChar + item + ":" + node.getSupport());
            }
        }

        for(int i = itemSortByFreq.size() - 1; i >= 0; i--){
            ArrayList<ArrayList<String>> newTransactions = new ArrayList<>();
            FPTreeNode node = itemSortByFreq.get(i);
            String newItem = item == null ? node.getContent() : node.getContent() + FPGMainClass.splitChar + item;

            while(node.hasNextSameNode()){
                node = node.getNextSameNode();
                for(int j = 0; j < node.getSupport(); j++)
                    newTransactions.add(getCPB(node));
            }

            freqItems.addAll(fp_growth(newTransactions, newItem));
        }
        return freqItems;
    }
}
