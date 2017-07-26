/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

package ir;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;


public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score;
    public LinkedList<Integer> positionList;

    //
    //  YOUR CODE HERE
    //
    public PostingsEntry(int id) {
        docID = id;
        positionList = new LinkedList<Integer>();

    }

    /**
     * PostingsEntries are compared by their score (only relevant
     * in ranked retrieval).
     * <p>
     * The comparison is defined so that entries will be put in
     * descending order.
     */
    public int compareTo(PostingsEntry other) {
        return Double.compare(other.score, score);
    }

    public Iterator<Integer> list_iterator() {
        return positionList.iterator();
    }
    // since it's sorted
    public void addOffs(int offset) {

        positionList.add(offset);
    }
  
}

    
