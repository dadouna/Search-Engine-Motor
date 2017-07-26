/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    

public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
// results contain the ranked list from the current search
// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	double gamma = 0.0; // används ej, de orelevanta dokumenterna. städa upp queryn. Vad innehller de orelevanta dokumenterna för termer
// dess termer tar vi bort från den relebanta.)

	double alfa = 0.8; // hur mycket litar på det gamla sökning innan feedback
	double beta = 0.6; // hur mycket hänsyn du ska ta till nya sökningen efter feedback 


	/* 

	Algorithm: 
	The Rocchio algorithm is based on a method of relevance 
	feedback found in information retrieval systems
	The algorithm is based on the assumption that most users 
	have a general conception of which documents should be denoted 
	as relevant or non-relevant.[1] Therefore, the user's search 
	query is revised to include an arbitrary percentage of relevant 
	and non-relevant documents as a means of increasing the search 
	engine's recall, and possibly the precision as well. The number 
	of relevant and non-relevant documents allowed to enter a query 
	is dictated by the weights of the a, b.
	the associated weights (a, b) are responsible for shaping the 
	modified vector in a direction closer, or farther away, from the 
	original query, related documents, and non-related documents.



	Limitations of rocchio:
		Therefore the two queries of "Burma" and "Myanmar" will 
		appear much farther apart in the vector space model, 
		though they both contain similar origins
	Why not use non-relevant documents:
		Though there are benefits to ranking documents as not-relevant, 
		a relevant document ranking will result in more precise documents 
		being made available to the user. 
	


	Roccio algoritmen: 
		alfa = Original query weight 
		beta = related document weight
		
		higher alpha means that we give more focus to the original query terms
		higher beta means that we give more focus to the new search feedback. i.e new terms.

		Tar en org query
		Tar den gamla gånnger en viss vikt 
		vikt är alfa + beta (entill vektor) plus att den kollar på original vektor, q0 
			högre alfa mer fokus på original queryn 
			högra beta skiftar till mer fokus på de nya termerna. 
		dvs utökas med ord/termer från dokument vektorerna som var relevanta. 

	Vanligtvis har man väldigt häg alfa och beta lite under för att det är standard. man tycker att den 
	första queryn är väldigt bra.

	
	*/

	PostingsList related_doc_vector = new PostingsList();
	HashMap<String, Double> qm = new HashMap<String, Double>(); // spara termer och weights.
	// normalize weights
	for(Double w : weights){
		w = w/weights.size();
	}

	Iterator<PostingsEntry> it = results.iterator();
	for(int i = 0; i < docIsRelevant.length; i++){
		if(docIsRelevant[i] == true){
			related_doc_vector.list.add(results.get(i));
		} 
	}

	// apply Rocchio algorithm.
	for(int i = 0; i < terms.size(); i++){
		double new_weigt = weights.get(i) * alfa;
		qm.put(terms.get(i), new_weigt);
	}

	for(int i = 0; i < related_doc_vector.size(); i++){
		Iterator<PostingsEntry> ite = related_doc_vector.iterator();
		while(ite.hasNext()){
			PostingsEntry postingsentry = ite.next();
			int docID = postingsentry.docID;
			String filename = indexer.index.docIDs.get(Integer.toString(docID)); // hämtar filnamnet
			String path = "/Users/monadadoun/Desktop/ir/lab/davisWiki/" + filename; 
			File file = new File(path);
			HashSet<String> hs = new HashSet<>();
			// lägger  till alla ord som finns till mitt hashet
			try (Reader reader = new InputStreamReader( new FileInputStream(file), StandardCharsets.UTF_8 )){
				Tokenizer tok = new Tokenizer( reader, true, false, true, indexer.patterns_file );
				while (tok.hasMoreTokens()) {
					hs.add(tok.nextToken());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Iterator<String> iter = hs.iterator();
			while(iter.hasNext()){
				String s = iter.next();
				if(qm.containsKey(s)){
					Double tmp = qm.get(s);
					qm.put(s, tmp  + beta * postingsentry.score);
				} else {
					qm.put(s, beta * postingsentry.score);
				}
			}
		}
	}
	LinkedList<String> term = new LinkedList<>();
	LinkedList<Double> weight = new LinkedList<>();
	List<Map.Entry<String,Double>> answer = new ArrayList<Map.Entry<String,Double>>(qm.entrySet());
	answer.sort((e1,e2) -> e2.getValue().compareTo(e1.getValue())); // sorterar efter värde så att orden med högst score ligger först
	for (int i = 0; i < answer.size(); i++) {
		term.add(answer.get(i).getKey());
		weight.add(answer.get(i).getValue()/answer.size());
	}

	terms = term;
	weights = weight;
	}
	 /*
	  class Map.Entry<String, Double> {

		String Key;
	  Double Value;
	  
	  public String getKey() {
	  	return key;
	  }
	  
	  public Double getValue() {
	    return Value;
	  }
	}
	*/

    
}

    
