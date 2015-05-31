/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.BottomUpSearchableNetwork;
import monson.christian.morphology.paraMor.schemes.Scheme;

public abstract class BottomUpSearchAbstract extends NetworkSearchProcedure {

	protected BottomUpSearchableNetwork partialOrderNetwork;
	
	protected SearchPathList pathsToSelectedSchemes; 
	
	// If a node is in the list of covered nodes then we do not want to extend
	// another search path up to or through that node (because some previous 
	// search path grew up to cover that node.)
	TreeSet<SetOfMorphemes<Affix>> covered;
	TreeSet<SetOfMorphemes<Affix>> visited;
	
	// A list of level 1 nodes from which to begin a search path
	ArrayList<Scheme> candidateSeeds = null;

	
	// Bottom-up Search (for now at least) only allows dynamic dense networks.
	// The key here is the *dense* part.
	public BottomUpSearchAbstract(BottomUpSearchableNetwork partialOrderNetwork) {
		
		this.partialOrderNetwork = partialOrderNetwork;
		
		pathsToSelectedSchemes = new SearchPathList();
		covered = new TreeSet<SetOfMorphemes<Affix>>();	
		visited = new TreeSet<SetOfMorphemes<Affix>>();
	}
	

	public SearchPathList searchGreedy() {
		
		// current == null when we are at the bottom of the network
		Scheme current;
		
		SearchPath pathToSelectedScheme;
		
		boolean growNewSearchPath = true; 
		
		while (growNewSearchPath) {
			
			// 1) Pick a new seed at the bottom of the network
			//
			// pickNewSeed will return null when we do not wan't to grow any new seed
			//
			pathToSelectedScheme = new SearchPath();
			
			current = pickNewSearchSeed();
			if (current == null) {
				growNewSearchPath = false;
			} else {
				System.err.println();
				System.err.println("---------------------------------");
				System.err.println("Starting Search from the Level 1 Seed:");
				System.err.println(current.toPrettyString(30));
				System.err.println();
				System.err.println();
			}
			
			
			// 2) Grow the seed up until we decide to stop
			//
			// growSeed will return null when we should stop growing a seed
			//
			while (current != null) {
				// We want to add current to the covered list and to the
				// explored path exactly when current is not null--irregardless
				// of whether or not we are just starting this path or continuing
				// in onwards.
				addToCovered(current.getAffixes());
				pathToSelectedScheme.add(current);
				
				current = growSearchPath(current);
			}
			
			
			// 3) Add the grown seed to the set of Selected Nodes
			//
			// 'if (growNewSearchPath)', then we did grow a seed this time through the loop
			// So add the grown seed to the set of Selected Nodes
			if (growNewSearchPath) {
				
				Scheme selectedScheme = pathToSelectedScheme.getTerminalScheme();
				
				if (selectedScheme != null) {
					
					// Only accept schemes with at least 2 affixes in them (otherwise it
					// is hardly a paradigm)
					if (selectedScheme.level() >= 2) {
						System.err.println();
						System.err.println("------------------------------");
						System.err.println("Selected:");
						System.err.println(pathToSelectedScheme);  
						
						pathsToSelectedSchemes.add(pathToSelectedScheme);
					}
				}
			} else {
				
				System.err.println();
				System.err.println(pathsToSelectedSchemes.size() + " Schemes were selected during ");
				System.err.println("  this BottomUp Search.");
			}
		}
		
		return pathsToSelectedSchemes;
	}
	
	@Override
	public SearchPathList search() {
		
		Stack<SearchPath> partialPathsToSelectedSchemes = new Stack<SearchPath>();
		
		while (true) {
			
			// 1) Pick a new seed at the bottom of the network
			//
			// pickNewSeed will return null when we do not wan't to grow any new seed
			//
			SearchPath partialPathToSelectedScheme = new SearchPath();
			
			Scheme seed = pickNewSearchSeed();
			if (seed == null) {
				break;
			}
			
			System.err.println();
			System.err.println("---------------------------------");
			System.err.println("Starting Search from the Level 1 Seed:");
			System.err.println(seed.toPrettyString(30));
			System.err.println();
			System.err.println();
			
			partialPathToSelectedScheme.add(seed);
			partialPathsToSelectedSchemes.push(partialPathToSelectedScheme);
			
			
			// 2) Grow the seed up until we decide to stop
			//
			// growSeed will return null when we should stop growing a seed
			//
			while (partialPathsToSelectedSchemes.size() != 0) {
				
				partialPathToSelectedScheme = partialPathsToSelectedSchemes.pop();
				
				Scheme terminalScheme = partialPathToSelectedScheme.getTerminalScheme();
				addToCovered(terminalScheme.getAffixes());
				visited.add(terminalScheme.getAffixes());
				
				
				List<Scheme> qualifiedParentsSortedBestToWorst = null;/* = 
					getQualifiedParentsSortedBestToWorst(terminalScheme);
				*/
				
				// Step through in reverse order
				for (int indexIntoQualifiedParents = qualifiedParentsSortedBestToWorst.size()-1;
					 indexIntoQualifiedParents >= 0;
					 indexIntoQualifiedParents--) {
					
					Scheme qualifiedParent = 
						qualifiedParentsSortedBestToWorst.get(indexIntoQualifiedParents);
					
					SearchPath newPartialPathToSelectedScheme =
						new SearchPath(partialPathToSelectedScheme);
					newPartialPathToSelectedScheme.add(qualifiedParent);
					partialPathsToSelectedSchemes.push(newPartialPathToSelectedScheme);
				}
				
				
				// We want to add current to the covered list and to the
				// explored path exactly when current is not null--irregardless
				// of whether or not we are just starting this path or continuing
				// in onwards.
				//addToCovered(current.get_affixes());
				//pathToSelectedScheme.add(current);
				
				//current = growSearchPath(current);
			}
			
			
			// 3) Add the grown seed to the set of Selected Nodes
			//
			// 'if (growNewSearchPath)', then we did grow a seed this time through the loop
			// So add the grown seed to the set of Selected Nodes
			
			/*
			Scheme selectedScheme = pathToSelectedScheme.getTerminalScheme();
			
			if (selectedScheme != null) {
				
				// Only accept schemes with at least 2 affixes in them (otherwise it
				// is hardly a paradigm)
				if (selectedScheme.level() >= 2) {
					System.err.println();
					System.err.println("------------------------------");
					System.err.println("Selected:");
					System.err.println(pathToSelectedScheme);  
					
					pathsToSelectedSchemes.add(pathToSelectedScheme);
				}
			}
			*/
		}
		
		System.err.println();
		System.err.println(pathsToSelectedSchemes.size() + " Schemes were selected during ");
		System.err.println("  this BottomUp Search.");
		
		return pathsToSelectedSchemes;
	}


	/**
	 * @return a node from among the lowests in this.partialOrderNetwork
	 * 		   <code>null</code> if we do not want to pick any new seed
	 */
	protected abstract Scheme pickNewSearchSeed();

	/**
	 * Grow a seed up from current by one parent.  If we do grow a seed up to 
	 * a parent P, we *must* add P (and any descendents of P that are not in
	 * the set of covered schemes) to the set of covered schemes 
	 * 
	 * @param current The top of the current seed
	 * @return The "best" node that is a parent of current
	 * 		   <code>null</code> if we do not wan to grow this seed any further
	 */
	protected abstract Scheme growSearchPath(Scheme current);
	
	/**
	 * Adds the set of affixes and (to save time) all the individual affixes themselves
	 * to the list of covered Scheme names.
	 * 
	 * @param affixes
	 */
	protected void addToCovered(SetOfMorphemes<Affix> affixes) {

		covered.add(affixes);
		
		// For an on demand network even though the level 1 schemes are always
		// in the network, they are not necesarily descendents of 'node'--i.e. 
		// there may be no parent/child links from 'node' to any particular
		// level 1 descendent.  But for the purposes of SeedGrowingSearch we
		// exactly want to NOT search from level 1 schemes for which a covering
		// node has been grown--even if the built network doesn't yet know that
		// a node covers this particular level 1 scheme.
		for (Affix affix : affixes) {
			SetOfMorphemes<Affix> level1SchemeName = new SetOfMorphemes<Affix>(affix);
			covered.add(level1SchemeName);
		}
	}
	
	/**
	 * @return A TreeSet of PartialOrderNodes sorted by decreasing adherent size 
	 * 		   of the scheme at that node
	 */
	protected void getCandidateSeedsByAdherentSize() {
		
		// Return cached
		if (candidateSeeds != null) {
			return;
		}
		
		TreeSet<Scheme> candidateSeedsOrderedSet = 
			new TreeSet<Scheme>(new Scheme.ByDecreasingAdherentSize());
		
		candidateSeedsOrderedSet.addAll(partialOrderNetwork.getSmallestSchemesAboveLevel0());
		
		candidateSeeds = new ArrayList<Scheme>(candidateSeedsOrderedSet);
	}

}
