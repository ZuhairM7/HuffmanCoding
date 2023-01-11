/*  Student information for assignment:
 *
 *  On MY honor, Zuhair Merali, this programming assignment is MY own work
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

import java.util.ArrayList;

public class PriorityQueue<E extends Comparable<? super E>> {
    private ArrayList<E> queue;

    /**
     * Constructs an empty priority queue
     * pre : None
     * post: queue is empty
     */
    public PriorityQueue() {
        queue = new ArrayList<>();
    }

    /**
     * Adds a passed in val to the queue using binary search to place it in the correct position by
     * comparing to the current values in the queue
     * pre : Already met in calling function
     * post: val is inserted into the correct position in the queue
     */
    public void enqueue(E val) {
        queue.add(bsearch(queue, val), val);
    }

    /**
     * Gets the value at a certain pos in the queue
     * pre : pos >= 0
     * post: returns the value at pos.
     */
    public E get(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("Pos can't be less than 0");
        }
        return queue.get(pos);
    }

    /**
     * Removes the value at the front of the queue.
     * pre : Size of the queue must be greater than 0
     * post: returns the value removed from the queue
     */
    public E dequeue() {
        if (queue.size() == 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        return queue.remove(queue.size() - 1);
    }

    /**
     * Get the value at the front of the queue
     * pre : Size must be greater than 0
     * post: value at the front of the queue is return
     */
    public E peek() {
        if (queue.size() == 0) {
            throw new IllegalArgumentException("Size of queue must be greater than 0");
        }
        return queue.get(queue.size() - 1);
    }

    /**
     * Gets the size of the queue
     * pre : None
     * post: returns an int representing the size of the queue.
     */
    public int size() {
        return queue.size();
    }

    /**
     * A binary search that finds the target position of where to enqueue a new value into the
     * queue.
     * pre : Already met in calling function
     * post: returns the position of where the new value must be enqueued.
     */
    private int bsearch(ArrayList<E> data, E tgt) {
        //5 4 2 1
        int indexOfTgt = -1;
        int low = 0;
        int high = data.size() - 1;
        while (indexOfTgt == -1 && low <= high) {
            int mid = low + ((high - low) / 2);
            if (data.get(mid).compareTo(tgt) == 0) {
                indexOfTgt = mid;
            }
            else if (data.get(mid).compareTo(tgt) < 0) {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }
        if (indexOfTgt > -1) {
            while (indexOfTgt > 0 && data.get(indexOfTgt - 1).compareTo(tgt) == 0) {
                indexOfTgt--;
            }
            return indexOfTgt;
        }
        else {
            return low;
        }
    }
}
