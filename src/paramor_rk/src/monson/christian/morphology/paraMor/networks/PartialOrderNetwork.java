/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;

import java.io.Serializable;
import java.util.Map;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;

public abstract class PartialOrderNetwork implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum MorphemicAnalysis { 
		SUFFIX, 
		PREFIX,
		SUFFIX_PREFIX, 
		SLOT};
	
	public static class Identifier implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private Class<? extends PartialOrderNetwork> theNetworkClass;
		private Corpus corpus;
		private MorphemicAnalysis morphemicAnalysis;
		private boolean allowEmptyStems = false;
		
	
		public 
		Identifier(
				Class<? extends PartialOrderNetwork> partialOrderNetworkClass, 
				Corpus corpus, 
				MorphemicAnalysis morphemicAnalysis,
				boolean allowEmptyStems) {
			
			this.theNetworkClass = partialOrderNetworkClass;
			this.corpus = corpus;
			this.morphemicAnalysis = morphemicAnalysis;
			this.allowEmptyStems = allowEmptyStems;
		}
		
		public Corpus getCorpus() {
			return corpus;
		}

		public MorphemicAnalysis getMorphemicAnalysis() {
			return morphemicAnalysis;
		}

		public Class<? extends PartialOrderNetwork> getTheNetworkClass() {
			return theNetworkClass;
		}
		
		public void setTheNetworkClass(Class<VirtualPartialOrderNetwork> networkClass) {
			this.theNetworkClass = networkClass;
		}
		
		public void setMorphemicAnalysis(MorphemicAnalysis morphemicAnalysis) {
			this.morphemicAnalysis = morphemicAnalysis;
		}
		
		public boolean getAllowEmptyStems() {
			return allowEmptyStems;
		}

		public void setAllowEmptyStems(boolean allowEmptyStems) {
			this.allowEmptyStems = allowEmptyStems;
		}

		@Override
		public boolean equals(Object o) {
			if ( ! (o instanceof PartialOrderNetwork.Identifier)) {
				return false;	
			}
			PartialOrderNetwork.Identifier that = (PartialOrderNetwork.Identifier)o;
			
			if ( ! this.theNetworkClass.getCanonicalName().equals(
					that.theNetworkClass.getCanonicalName())) {
				
				return false;
			}
						
			// The corpora have to be identical.  This likely means identical down to pointer
			// identity.
			if ( ! this.corpus.equals(that.corpus)) {
				return false;
			}
			
			// Compare the MorphemicAnalyses of 'this' and 'that'
			if ( ! this.morphemicAnalysis.equals(that.morphemicAnalysis)) {
				return false;
			}
			
			if (this.allowEmptyStems != that.allowEmptyStems) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public String toString() {
			String toReturn = "";
			//                    -------------> vvvv <-- extra space lines up output with 'corpus' output
			toReturn += "Network Type:               " + theNetworkClass.getSimpleName();
			toReturn += String.format("%n");
			toReturn += "Morphemic Analysis:         " + morphemicAnalysis;
			toReturn += String.format("%n");
			toReturn += "Allow Empty Stems:          " + allowEmptyStems;
			toReturn += String.format("%n");
			toReturn += corpus;
			
			return toReturn;
		}

		public String toStringAsComment() {
			String toReturn = "";
			//                    -------------> vvvv <-- extra space lines up output with 'corpus' output
			toReturn += "# Network Type:               " + theNetworkClass.getSimpleName();
			toReturn += String.format("%n");
			toReturn += "# Morphemic Analysis:         " + morphemicAnalysis;
			toReturn += String.format("%n");
			toReturn += "# Allow Empty Stems:          " + allowEmptyStems;
			toReturn += String.format("%n");
			toReturn += corpus.toStringAsComment();
			
			return toReturn;
		}


	}

	protected Identifier identifier;
	
	public abstract Map<Affix, Level1Scheme> getLevel1SchemesByAffix();
	
	public static 
	PartialOrderNetwork factory(
								Class<? extends PartialOrderNetwork> partialOrderNetworkClassToCreate,
								Corpus corpus,
								MorphemicAnalysis morphemicAnalysis,
								boolean allowEmptyStems) {
		
		PartialOrderNetwork.Identifier networkIdentifier = 
			new Identifier(
					partialOrderNetworkClassToCreate, 
					corpus, 
					morphemicAnalysis,
					allowEmptyStems);
		
		return PartialOrderNetwork.factory(networkIdentifier);
	}

	public static PartialOrderNetwork factory(Identifier networkIdentifier) {
		PartialOrderNetwork networkToReturn = null;
		
		Class<? extends PartialOrderNetwork> networkClassToCreate = 
			networkIdentifier.getTheNetworkClass();
		
		if (networkClassToCreate == VirtualPartialOrderNetwork.class) { 
			
			networkToReturn = new VirtualPartialOrderNetwork(networkIdentifier);
		
		} else {
			System.err.println();
			System.err.println("ERROR: I do not know how to build a " + 
							   networkClassToCreate.getCanonicalName());
			System.err.println();
		}
		
		return networkToReturn;	}
	
	
	public PartialOrderNetwork.Identifier getIdentifier() {
		return identifier;
	}


	public String toString() {
		return getIdentifier().toString();
	}



}
