/*  Student information for assignment:
 *
 *  On MY honor, Zuhair Merali, this programming assignment is My own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 Zuhair Merali
 *  UTEID: zsm386
 *  email address: zmerali@utexas.edu
 *  Grader name: Sai Tanuj Madisetty
 *
 *  Student 2
 *  UTEID:
 *  email address:
 *
 */

import java.util.HashMap;

public class HuffmanCodeTree {
    private TreeNode root;
    private int numLeafNodes;

    /**
     * Builds Huffman Tree from a passed in frequency table
     * pre : None
     * post: Tree is constructed with root
     */
    public TreeNode buildTree(int[] arr) {
        PriorityQueue<TreeNode> queue = new PriorityQueue<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 0) {
                queue.enqueue(new TreeNode(i, arr[i]));
            }
        }
        queue.enqueue(new TreeNode(IHuffConstants.ALPH_SIZE, 1));
        while (queue.size() >= 2) {
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            TreeNode parent = new TreeNode(left, -1, right);
            queue.enqueue(parent);
        }
        root = queue.peek();
        return queue.dequeue();
    }

    /**
     * Creates a mapping from the ASCI values in the file to the new compressed bits found by
     * traversing the huffman tree
     * pre : Root does not equal null
     * post: new HashMap is returned
     */
    public HashMap<Integer, String> ASCItoHuff(TreeNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root may not be null");
        }
        HashMap<Integer, String> table = new HashMap<>();
        huffTraverse(root, "", table);
        return table;
    }

    /**
     * Creates a mapping from the compressed bits back to the uncompressed ASCII values
     * pre : root can't be null
     * post: new HashMap is returned
     */
    public HashMap<String, Integer> HuffToASCI(TreeNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root can't be null");
        }
        HashMap<String, Integer> table = new HashMap<>();
        huffTraverseDecode(root, "", table);
        return table;
    }

    /**
     * huffTraverse is a helper method that works to build the new compressed bit output for every
     * word in a file
     * pre : Already met in calling method
     * post: new HashMap is filled.
     */
    private void huffTraverse(TreeNode node, String s, HashMap<Integer, String> table) {
        if (!node.isLeaf()) {
            huffTraverse(node.getLeft(), s + "0", table);
            huffTraverse(node.getRight(), s + "1", table);
        }
        else {
            table.put(node.getValue(), (s));
            numLeafNodes += 1;
        }
    }

    /**
     * huffTraverse is a helper method that works to rebuild the old uncompressed file bits from
     * the new compressed Bits.
     * pre : Already met in calling method
     * post: new HashMap is filled.
     */
    private void huffTraverseDecode(TreeNode node, String s, HashMap<String, Integer> table) {
        if (!node.isLeaf()) {
            huffTraverseDecode(node.getLeft(), s + "0", table);
            huffTraverseDecode(node.getRight(), s + "1", table);
        }
        else {
            table.put(s, node.getValue());
            numLeafNodes += 1;
        }
    }

    /**
     * Gets num of Leaf nodes in a Huffman tree.
     * pre : None
     * post: returns numLeafNodes of this newly built Huffman Tree
     */
    public int getNumLeafNodes() {
        return numLeafNodes;
    }

    /**
     * Calls size(root) to find the size of the tree
     * pre : None
     * post: returns size of tree
     */
    public int treeSize() {
        return size(root);
    }

    /**
     * Finds the size of a certain nodes subtree(or entire tree if root is passed in)
     * pre : None
     * post: returns size of node
     */
    public int size(TreeNode node) {
        if (node == null) {
            return 0;
        }
        else {
            return (size(node.getLeft()) + 1 + size(node.getRight()));
        }
    }

}
