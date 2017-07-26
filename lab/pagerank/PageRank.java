/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 	18:16
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }

    /**
     *   An innerclass for creating orders with timestamp.
     *
     */
    private class DocNameByPageRank {
        private String docName; // = System.currentTimeMillis();
        private double pageRank;

        DocNameByPageRank(String docName, double pageRank) {
            this.pageRank = pageRank;
            this.docName = docName;
        }

        double getPageRank() {
            return pageRank;
        }

        String getDocName() {
            return docName;
        }
    }

    /**
     * Priority for pages is defined as highest rankvalue
     * 
     */
    private static final Comparator<DocNameByPageRank> RANKCOMPARATOR = new PageRankComparator();

    private static final class PageRankComparator implements Comparator<DocNameByPageRank> {
        public int compare(DocNameByPageRank o1, DocNameByPageRank o2) {
            double compareValue1 = o1.getPageRank(); 
            double compareValue2 = o2.getPageRank(); 
            return Double.compare(compareValue2, compareValue1);
        
        }


    }

	/* --------------------------------------------- */
    
    /*
     *   Computes the pagerank of each document.
     *      a hashtable holding hashtables.
     */
    void computePagerank( int numberOfDocs ) {
	//
	//   YOUR CODE HERE
	//
    	double[] init = new double[numberOfDocs]; // current position.
    	double[] next = new double[numberOfDocs]; // init * A  (A är vår transistion matrix) då får jag next state
    	// probability matrix size = n * n 
    	int loopCounter = 0;
    	//init[0] = 1.0;
    	for(int i = 0; i < numberOfDocs; i++){ // fill in with initial guesses
    		init[i] = 1.0/(double)numberOfDocs;
    	}
    	
    	double bored = BORED; //some optimization
    	double boredPerDoc = bored/(double)numberOfDocs; // Prob jumping to a random link.

    	while(loopCounter <  MAX_NUMBER_OF_ITERATIONS){ 
    		for(int i = 0; i < numberOfDocs; i++){ //loop over the rows of the transition matrix
 				
                next[i] = 0.0; 
    			for(int j = 0; j < numberOfDocs; j++){ //loop over the columns of the transition matrix
                
    				double g = boredPerDoc; // The default value used if no link exists.
                    Hashtable<Integer, Boolean> currentLink = link.get(j); // get hashtable
    				
                    if (currentLink == null) {  // there is no links to i from j
    					g = 1.0d/numberOfDocs;

					} else 	if(currentLink.get(i) != null){   // If there is a link to i from j
		    			g = boredPerDoc + ((1.0d-bored)/(double)out[j]);		            	
					}
		            
    				next[i] += init[j]*g;   // update next state.
    			}
    		}
    		
    		double diff = 0.0;
	    	for(int i = 0; i < numberOfDocs; i++){
	    		diff += Math.abs(init[i]-next[i]);
	    	} 
	    	System.out.println("Iter: " + loopCounter + ", Error: " + diff);
	    	if(diff < EPSILON){
	    		System.out.println("DONE, nr of iterations: " + loopCounter);
	    		break;
	    	}
	    	//skriv över för att du vill stega framåt
	    	for(int i = 0; i < numberOfDocs; i++){
	    		init[i] = next[i];
	    	}
	    	//System.out.println(difference);
	    	loopCounter++;
    	}
        sortAndPrint(next);
    }

    /*
        A method that connects docname to docid

    */
    private void sortAndPrint(double[] init){
    	List <DocNameByPageRank> result = new ArrayList<DocNameByPageRank>();
    	for(int i = 0; i < init.length; i++){
    		DocNameByPageRank pageAndRank = new DocNameByPageRank(docName[i], init[i]);
    		result.add(pageAndRank);
    	}
    	//Collections.sort(result, RANKCOMPARATOR);
    	result.sort(RANKCOMPARATOR);

    	
        for (int i = 0; i < 30; i++) {
            System.out.printf("%s: %.5f\n", result.get(i).getDocName() , result.get(i).getPageRank());
        }

    }
    
/* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
