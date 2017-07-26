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

/**
 * A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    /**
     * The postings list as a linked list. 
     */
    public LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>(); // OBS KANSKE MÅSTE GÖRA OM TILL PRIVAT DVS ORIGINAL


    /**
     * Number of postings in this list
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns the ith posting
     */
    public PostingsEntry get(int i) {
        return list.get(i);
    }

    //
    //  YOUR CODE HERE
    //

    public Iterator<PostingsEntry> iterator() {
        return list.iterator();
    }

    // offset står för ord position
    public void add(int id) {
        PostingsEntry entry = new PostingsEntry(id);
        if (list.size() == 0) {
            list.add(entry);
        } else if (list.getLast().docID != id) { // antar att de läggs in i ordning, detta för att undvika dubletter.
            list.add(entry);
        }
    }

    // Denna var fel
    public void add_off(int id, int offset) {
        add(id);
        list.getLast().addOffs(offset);
    }

    public void addEntry(PostingsEntry e) { // ett entry kmr va docid< >
        
            list.add(e);
        
    }

    private boolean contains(int iD) {
        for (PostingsEntry entry : list) {
            if (entry.docID == iD) {
                return true;
            }
        }
        return false;
    }
}


			   
