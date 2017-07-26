/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        //
        //  YOUR CODE HERE
        //
        if (!index.containsKey(token)) {
            index.put(token, new PostingsList());
        }
        index.get(token).add(docID, offset);
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (queryType == Index.INTERSECTION_QUERY) {
            PostingsList answer = getPostings(query.terms.get(0));
            for (int i = 1; i < query.terms.size(); i++ ) {
                PostingsList temp = getPostings(query.terms.get(i));
                answer = intersect(answer.getIterator(), temp.getIterator());
            }
            return answer;
        } else if (queryType == Index.PHRASE_QUERY) {
            PostingsList answer = getPostings(query.terms.get(0));
            for (int i = 1; i < query.terms.size(); i++ ) {
                PostingsList temp = getPostings(query.terms.get(i));
                answer = phraseIntersect(answer.getIterator(), temp.getIterator());
            }
            return answer;
        } else if (queryType == Index.RANKED_QUERY) {
            PostingsList answer = null;
            if (rankingType == Index.TF_IDF) {
                answer = cosineScore(query);
            }
            return answer;
        }
        String token = query.terms.getFirst();
        return getPostings(token);
    }

    /**
     *  Performs an intersection between two iterators.
     */
    private PostingsList intersect(PeekingIterator<PostingsEntry> p1, PeekingIterator<PostingsEntry> p2) {
        PostingsList answer = new PostingsList();
        while (p1.hasNext() && p2.hasNext()) {
            PostingsEntry a = p1.peek();
            PostingsEntry b = p2.peek();
            if (a.docID == b.docID) {
                answer.add(a.docID);
                p1.next();
                p2.next();
            } else if (a.docID < b.docID) {
                p1.next();
            } else {
                p2.next();
            }
        }

        return answer;
    }


    private PostingsList phraseIntersect(PeekingIterator<PostingsEntry> p1, PeekingIterator<PostingsEntry> p2) {
        PostingsList answer = new PostingsList();
        while (p1.hasNext() && p2.hasNext()) {
            PostingsEntry a = p1.peek();
            PostingsEntry b = p2.peek();
            if (a.docID == b.docID) {
                PostingsEntry temp = findIntersects(a.getIterator(), b.getIterator());
                if (temp != null) {
                  temp.docID = a.docID;
                  answer.add(temp);
                }
                p1.next();
                p2.next();
            } else if (a.docID < b.docID) {
                p1.next();
            } else {
                p2.next();
            }
        }

        return answer;
    }

    private PostingsEntry findIntersects(PeekingIterator<Integer> p1, PeekingIterator<Integer> p2) {
        PostingsEntry answer = new PostingsEntry();
        boolean hasAdded = false;
        while (p1.hasNext() && p2.hasNext()) {
            Integer a = p1.peek();
            Integer b = p2.peek();
            if (a+1 == b) {
                answer.addPos(b);
                hasAdded = true;
                p1.next();
                p2.next();
            } else if (a < b) {
                p1.next();
            } else {
                p2.next();
            }
        }

        return hasAdded ? answer: null;
    }

    private PostingsList cosineScore(Query q) {

        HashMap<String, Integer> qtf = new HashMap<String, Integer>();
        for(String term: q.terms) {
            if (qtf.containsKey(term)) {
                qtf.put(term, qtf.get(term)+1);
            } else {
                qtf.put(term, 1);
            }
        }

        HashMap<Integer, PostingsEntry> docs = new HashMap<Integer, PostingsEntry>();
        // TODO cleanup a bit...
        for (String term: q.terms) {
            //double wtq = 1 + Math.log10(qtf.get(term));
            PostingsList pl = getPostings(term);
            Iterator<PostingsEntry> it = pl.getIterator();
            double idf = Math.log(Index.docIDs.size()/pl.getList().size());
            while (it.hasNext()) {
                PostingsEntry pe = it.next();
                //double wtd = 1 + Math.log10(pe.getSize());
                double tf_idf = pe.getSize()*idf/Index.docLengths.get(Integer.toString(pe.docID));
                if (docs.containsKey(pe.docID)) {
                    PostingsEntry temp = docs.get(pe.docID);
                    //temp.score += wtd*wtq;
                    temp.score += tf_idf;
                    docs.put(pe.docID, temp);
                } else {
                    pe.score = tf_idf;
                    docs.put(pe.docID, pe);
                }
            }
        }

        PostingsList answer = new PostingsList();
        answer.getList().addAll(docs.values());
        Collections.sort(answer.getList());
        return answer;

    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}