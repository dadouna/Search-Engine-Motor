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
import java.util.NoSuchElementException;
import java.util.Collections;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /**
     * The index as a hashtable.
     */
    private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();


    /**
     * Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset) {

        PostingsList postlist = new PostingsList();
        if (!index.containsKey(token)) {
            postlist.add_off(docID, offset);
            //postlist.add(docID);
            index.put(token, postlist);
        } else {
            postlist = getPostings(token);
            index.get(token).add_off(docID, offset);
        }

    }


    /**
     * Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {

        return index.keySet().iterator();
    }


    /**
     * Returns the postings for a specific term, or null
     * if the term is not in the index.
     */
    public PostingsList getPostings(String token) {

        return index.get(token);
    }


    /*
    @param two postingslist holding docIDs. 
    returns a postingslist holding all docIDs that contains the terms equal query.
    */
    public PostingsList search(Query query, int queryType, int rankingType, int structureType) {

        String token = query.terms.getFirst();
        PostingsList answer = getPostings(token);   // the PostingsList to be returned.
        PostingsList tmp = new PostingsList();      // temporary Postingslist
        if (queryType == Index.INTERSECTION_QUERY) {
			for(int i = 1; i < query.terms.size(); i++){
				tmp = getPostings(query.terms.get(i));
				answer = intersect(answer.iterator(), tmp.iterator());
            }
        }else if (queryType == Index.PHRASE_QUERY) {
			for(int i = 1; i < query.terms.size(); i++){
				tmp = getPostings(query.terms.get(i));
				answer = phrase_intersect(answer.iterator(), tmp.iterator());
            }
        } else if (queryType == Index.RANKED_QUERY) {
			if(rankingType == Index.TF_IDF){
        		answer = cosineScore(query);
        	}
        } 
        return answer;
    }

    /*
        A search method to implement ranked retrieval. Compute the cosine similarity between 
        the tf-idf vector of the query and the tf-idf vectors of all matching documents.
        
        Then sort the documents according to their similarity score.
        tf  -  hur många gågr söktermerna forekommer i ett dokument
        idf -  i hur många dokument som de förekommer i.
        
        termer som förekommer i få dokument och många gånger i samma dokument rankas som högre dvs får högre score.

        ------------------------------------------------
    SCORE   tf_idf_dt   =   tf_dt * idf_dt / len_d
            idf_t       =   ln(N/df_dt)
        ------------------------------------------------
            tf_dt   =   #   of occurences of t in d (OBS positionslist.length i 
                            postingsentry säger hur många som förekommer i ett dokument)
            N       =   #   documents in the corpus
            df_t    =   #   documents in the corpus which contain t
            len_d   =   #   words in d 
        ------------------------------------------------
        Log-frequency weight of term t in document d
        if (tf_td > 0) then
            w_td = 1 + log_10 tf_td 
        else 0

        Score is 0 if no query term is present in the document. 
        → High weight for rare terms like ARACHNOCENTRIC
        → Very low weight for common terms like THE
	*/
    private PostingsList cosineScore(Query query){
        /* %%%%%%%%%%%%%%% Declarations %%%%%%%%%%%%%%%% */

        HashMap<Integer, PostingsEntry> relevant_docs = new HashMap<Integer, PostingsEntry>(); // will hold all docs with score.
        PostingsList answer = new PostingsList();   //  a list with all postingslists with postingsentries having a score.
        double tfidf = 0.0;                         //  tfidf = tf_dt * idf_dt / len_d for doc d and query term t
        double idft = 0.0;                          //  idft  = log(n/df_dt)    length-normalizer to normalize score of 
                                                    //                          document with respect to the query term. 
        double n = Index.docIDs.size();             //  n     = [ # documents in the corpus]
        double lend = 0.0;                          //  lend  = [# words in d]
        double tfdt = 0.0;                          //  tfdt  = [# occurrences of t in d]
        double dft = 0.0;                           //  dft   = [# documents in the corpus which contain t],
        
        /* %%%%%%%%%%%%%%% Algortihm %%%%%%%%%%%%%%%% */
        for(String term: query.terms){
            PostingsList postingslist = getPostings(term);
            Iterator<PostingsEntry> it = postingslist.iterator();
            try {
                PostingsEntry postingsentry = it.next();
                while (postingsentry != null) {
                    lend = Index.docLengths.get(Integer.toString(postingsentry.docID)); // len av antalet ord
                    tfdt = postingsentry.positionList.size();
                    dft = postingslist.size();
                    idft = Math.log(n/dft);
                    tfidf = tfdt*idft/lend;
                    
                    if(relevant_docs.containsKey(postingsentry.docID)){
                        PostingsEntry pe = relevant_docs.get(postingsentry.docID);
                        pe.score += tfidf;
                        relevant_docs.put(pe.docID, pe);
                    }else{
                        postingsentry.score = tfidf;
                        relevant_docs.put(postingsentry.docID, postingsentry);
                    }   
                    postingsentry = it.next();
                }
            }catch(NoSuchElementException e){
                // Iterator can cause some exceptions.
            }
        }
        // get all relevant docs from hashmap and sort them.
        answer.list.addAll(relevant_docs.values());
        Collections.sort(answer.list);
        return answer;
    }

    private PostingsList intersect(Iterator<PostingsEntry> itrp1, Iterator<PostingsEntry> itrp2) {

        PostingsList answer = new PostingsList();
        //Iterator<PostingsEntry> itrp1 = p1.iterator();
        //Iterator<PostingsEntry> itrp2 = p2.iterator();
        try {
            PostingsEntry elem1 = itrp1.next();
            PostingsEntry elem2 = itrp2.next();
            while (elem1 != null && elem2 != null) {
                if (elem1.docID == elem2.docID) {
                    answer.add(elem1.docID);
                    elem1 = itrp1.next();
                    elem2 = itrp2.next();
                } else if (elem1.docID < elem2.docID) {
                    elem1 = itrp1.next();
                } else {
                    elem2 = itrp2.next();
                }
            }
        } catch (NoSuchElementException e) {

        }
        return answer;
    }


    private PostingsList phrase_intersect(Iterator<PostingsEntry> itrp1, Iterator<PostingsEntry> itrp2) {

        PostingsList answer = new PostingsList();
        //Iterator<PostingsEntry> itrp1 = p1.iterator();
        //Iterator<PostingsEntry> itrp2 = p2.iterator();
   
        try {
            PostingsEntry doc1 = itrp1.next();
            PostingsEntry doc2 = itrp2.next();
            
            while (doc1 != null && doc2 != null) {
                if (doc1.docID == doc2.docID) {
                    Iterator<Integer> pp1_itr = doc1.list_iterator();
                    Iterator<Integer> pp2_itr = doc2.list_iterator();
                    PostingsEntry l = new PostingsEntry(doc1.docID);
                    boolean isAdded = false;
                    try {
                        
                        Integer pp1 = pp1_itr.next();
                        Integer pp2 = pp2_itr.next();
        
                        
                        while (pp1 != null && pp2 != null) {
                            if (pp1+1 == pp2) { // nästa ord ligger en position efter om de är lika
                                l.addOffs(pp2); // lägg till i en länkad lista pp2 var det först
                                isAdded = true;
                                //System.out.println("isadded in if " + isAdded + " " + pp1 + " " + pp2 );
                                pp1 = pp1_itr.next();
                                pp2 = pp2_itr.next();

                            } else if (pp1 < pp2) {
                                pp1 = pp1_itr.next();

                            } else {
                                pp2 = pp2_itr.next();
                            }

                        }
                        if (isAdded == true) {
                            answer.addEntry(l);
                        }
                        doc1 = itrp1.next();
                        doc2 = itrp2.next();

                    } catch (NoSuchElementException e) {

                        if(isAdded){
                            answer.addEntry(l);
                        }


                        doc1 = itrp1.next();
                        doc2 = itrp2.next();
                    }

                } else if (doc1.docID < doc2.docID) {
                    doc1 = itrp1.next();
                    

                } else {
                    doc2 = itrp2.next();
                   
                }
            }
        } catch (NoSuchElementException e) {
            //System.out.println(e);
        }
        return answer;
    }

    /**A ∩ B
     A ∩ B ∩ C
     D = A ∩ B
     E = D ∩ C
     return E
     */

    /**
     * No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
