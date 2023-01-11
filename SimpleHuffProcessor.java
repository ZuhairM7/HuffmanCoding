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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int storeCountsBits;
    private int storeTreesBits;
    private TreeNode root;
    private int headerFormat;
    private HashMap<Integer, String> table;
    private int[] freq;
    private int numLeafNodes;
    private int preProcessReturn;
    private static final int magicAndHeaderBitSize = 64;
    private static final int StoreCountsBitSize = 8192;
    private static final int treeHeaderBitSize = 9;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     *                     header to use, standard count format, standard tree format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        this.headerFormat = headerFormat;
        freq = new int[ALPH_SIZE];
        BitInputStream bitInputStream = new BitInputStream(in);
        int originalBits = updateFrequencies(bitInputStream, freq, 0);
        bitInputStream.close();
        int bitsInCompression = 0;
        HuffmanCodeTree tree = new HuffmanCodeTree();
        root = tree.buildTree(freq);
        table = tree.ASCItoHuff(root);
        numLeafNodes = tree.getNumLeafNodes();
        for (Integer AsciiVal : table.keySet()) {
            if (AsciiVal != ALPH_SIZE) {
                bitsInCompression += freq[AsciiVal] * table.get(AsciiVal).length();
            }
        }
        bitsInCompression += table.get(ALPH_SIZE).length();
        if (headerFormat == STORE_COUNTS) {
            storeCountsBits = magicAndHeaderBitSize + StoreCountsBitSize + bitsInCompression;
            return (preProcessReturn = (originalBits - storeCountsBits));
        }
        else if (headerFormat == STORE_TREE) {
            storeTreesBits = magicAndHeaderBitSize + (magicAndHeaderBitSize / 2) +
                    (tree.getNumLeafNodes() * (treeHeaderBitSize + 1)) + (tree.treeSize() -
                    tree.getNumLeafNodes()) + bitsInCompression;
            return (preProcessReturn = (originalBits - storeTreesBits));
        }
        else {
            throw new IOException("Incorrect Header Format");
        }
    }

    /**
     * updates Frequency table for storeCounts
     * pre : Already met in calling function
     * post: returns the numOfBits in the original file and updates the freq table
     *
     * @throws IOException if an error occurs while reading from the input file.
     */
    private int updateFrequencies(BitInputStream inputStream, int[] freq, int origBits)
            throws IOException {
        int readBits;
        while ((readBits = inputStream.readBits(BITS_PER_WORD)) != -1) {
            freq[readBits] += 1;
            origBits += BITS_PER_WORD;
        }
        return origBits;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *              If this is false do not create the output file if it is larger than the input
     *              file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (!force && preProcessReturn < 0) {
            myViewer.showError(("compress is not implemented as it's bigger than orig file"));
            return -1;
        }
        else {
            BitOutputStream outputStream = new BitOutputStream(out);
            outputStream.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            if (headerFormat == STORE_COUNTS) {
                outputStream.writeBits(BITS_PER_INT, STORE_COUNTS);
                for (int i = 0; i < freq.length; i++) {
                    outputStream.writeBits(BITS_PER_INT, freq[i]);
                }
            }
            else if (headerFormat == STORE_TREE) {
                outputStream.writeBits(BITS_PER_INT, STORE_TREE);
                HuffmanCodeTree huffmanCodeTree = new HuffmanCodeTree();
                outputStream.writeBits(BITS_PER_INT, huffmanCodeTree.size(root) + numLeafNodes
                        * treeHeaderBitSize);
                preOrderStoreTree(root, outputStream);
            }
            BitInputStream inputStream = new BitInputStream(in);
            writeCompressedBits(inputStream, table, outputStream);
            if (headerFormat == STORE_COUNTS) {
                return storeCountsBits;
            }
            else {
                return storeTreesBits;
            }
        }
    }

    /**
     * Writes out the compressedBits to the outputStream and closes the stream
     * pre : Already met in calling function
     * post: outputStream is updated with compressed values in place of original bits
     *
     * @throws IOException if an error occurs while reading from the input file.
     */
    private void writeCompressedBits(BitInputStream inputStream, HashMap<Integer, String> table,
                                     BitOutputStream outputStream) throws IOException {
        int readbits;
        while ((readbits = inputStream.readBits(BITS_PER_WORD)) != -1) {
            String s = table.get(readbits);
            for (int i = 0; i < s.length(); i++) {
                outputStream.writeBits(1, Integer.parseInt(s.substring(i, i + 1)));
            }
        }
        String s = table.get(ALPH_SIZE);
        for (int i = 0; i < s.length(); i++) {
            outputStream.writeBits(1, Integer.parseInt(s.substring(i, i + 1)));
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     * Writes out the header data for StoreTrees
     * pre : Already met in calling function
     * post: updates the outputstream with the new header data
     */
    private void preOrderStoreTree(TreeNode root, BitOutputStream outputStream) {

        if (root == null) {
            return;
        }
        if (!root.isLeaf()) {
            outputStream.writeBits(1, 0);
        }
        else {
            outputStream.writeBits(1, 1);
            int ASCI = root.getValue();
            if (ASCI != ALPH_SIZE) {
                outputStream.writeBits(treeHeaderBitSize, ASCI);
            }
            else {
                outputStream.writeBits(1, 1);
                outputStream.writeBits(BITS_PER_WORD, 0);
            }
        }
        preOrderStoreTree(root.getLeft(), outputStream);
        preOrderStoreTree(root.getRight(), outputStream);
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream bitInputStream = new BitInputStream(in);
        int readbits;
        readbits = bitInputStream.readBits(BITS_PER_INT);
        if (readbits != MAGIC_NUMBER) {
            bitInputStream.close();
            throw new IOException("Error reading compressed file: No Magic value");
        }
        readbits = bitInputStream.readBits(BITS_PER_INT);
        if (readbits != STORE_COUNTS && readbits != STORE_TREE) {
            bitInputStream.close();
            throw new IOException("Error reading compressed file: No Header");
        }
        HuffmanCodeTree tree = new HuffmanCodeTree();
        BitOutputStream outputStream = new BitOutputStream(out);
        TreeNode root = new TreeNode(0, 0);
        if (readbits == STORE_COUNTS) {
            int[] freq = new int[ALPH_SIZE];
            for (int i = 0; i < ALPH_SIZE; i++) {
                freq[i] = bitInputStream.readBits(BITS_PER_INT);
            }
            root = tree.buildTree(freq);
        }
        else {
            bitInputStream.readBits(BITS_PER_INT);
            root = buildStoreTree(bitInputStream, false);
        }
        return writeUncompressedBits(root, tree, bitInputStream, outputStream);
    }

    /**
     * Remakes the map of compressed to uncompressed Bits and uses the map to write out the bits
     * of the original uncompressed file to the outputStream
     * pre : Already met in calling function
     * post: returns the number of bits
     *
     * @throws IOException if an error occurs while reading from the input file.
     */
    private int writeUncompressedBits(TreeNode root, HuffmanCodeTree tree,
                                      BitInputStream bitInputStream,
                                      BitOutputStream outputStream) throws IOException {
        int readbits;
        int bitCount = 0;
        HashMap<String, Integer> table = tree.HuffToASCI(root);
        String s = "";
        boolean done = false;
        while ((readbits = bitInputStream.readBits(1)) != -1 && !done) {
            s += readbits;
            if (table.containsKey(s)) {
                if (table.get(s) == ALPH_SIZE) {
                    done = true;
                }
                else {
                    outputStream.writeBits(BITS_PER_WORD, table.get(s));
                    bitCount += BITS_PER_WORD;
                    s = "";
                }
            }
        }
        if (!done) {
            throw new IOException("Error reading compressedFile- Doesn't contain psuedo value");
        }
        outputStream.close();
        outputStream.flush();
        bitInputStream.close();
        return bitCount;
    }

    /**
     * Rebuilds the Store Tree using compressed header
     * pre : Already met in calling function
     * post: returns the root of the Tree
     *
     * @throws IOException if an error occurs while reading from the input file.
     */
    private TreeNode buildStoreTree(BitInputStream inputStream,
                                    boolean done) throws IOException {
        while (!done) {
            int readBits = inputStream.readBits(1);
            if (readBits == 0) {
                return new TreeNode(buildStoreTree(inputStream, done), -1,
                        buildStoreTree(inputStream, done));
            }
            else if (readBits == 1) {
                int ASCI = inputStream.readBits(treeHeaderBitSize);
                if (ASCI == ALPH_SIZE) {
                    done = true;
                }
                else {
                    return new TreeNode(ASCI, -1);
                }
            }
            else {
                throw new IOException("Ran out of bits when forming huffamn tree");
            }
        }
        return new TreeNode(ALPH_SIZE, -1);
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
