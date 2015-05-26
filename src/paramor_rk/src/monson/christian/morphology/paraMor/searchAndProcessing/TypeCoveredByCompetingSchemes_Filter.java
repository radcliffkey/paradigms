/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.schemes.Scheme;

public class TypeCoveredByCompetingSchemes_Filter implements SearchStep {

	private static final String SEARCH_STEP_NAME = "Types Covered By Competing Schemes Filter";

	private static final long serialVersionUID = 1L;

	public static class Parameters extends SearchStepParameters {
		private static final long serialVersionUID = 1L;

		public Iterator<SearchStepParameterSetting> iterator() {
			
			List<SearchStepParameterSetting> oneEmptyParameterSetting = 
				new ArrayList<SearchStepParameterSetting>();
			
			oneEmptyParameterSetting.add(new ParameterSetting());
			
			return oneEmptyParameterSetting.iterator();
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return null;
		}

		@Override
		protected String getParametersString() {
			return "No Varying Parameters";
		}
		
		@Override
		protected String getParametersStringAsComment() {
			return "# No Varying Parameters";
		}

		
	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		// Some filters have specific parameters.  But TypeCoveredByCompetingSchemes_Filter
		// does not (yet).
		
		public ParameterSetting() {
			associatedSearchStep = TypeCoveredByCompetingSchemes_Filter.class;
		}

		@Override
		public String getStringForSpreadsheet() {
			return "";
		}
		
		@Override
		public String getFilenameUniqueifier() {
			return "";
		}

		@Override
		public int compareTo(SearchStepParameterSetting that) {
			if ( ! (that instanceof ParameterSetting)) {
				super.compareTo(that);
			}
			
			// All TypeCoveredByCompetingSchemes_Filter.ParamterSetting instances are identical
			return 0;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		protected String getParameterString() {
			return "No Varying Paramters";
		}
		
		
	}
	
	TypeCoveredByCompetingSchemes_Filter.ParameterSetting parameterSetting;
	
	Map<String, StringWithSetOfSearchPaths> coveredTypesWithSchemesByType = 
		new TreeMap<String, StringWithSetOfSearchPaths>();
	
	SearchPathList pathsToFilterAsAList;
	// To be able to easily remove specific searchPaths/Schemes I also keep the
	// selected schemes around in set form.  At the end of filtering the set version
	// and the list version (defined in a parent class) are reconciled.
	Set<SearchPath> pathsToFilterAsASet;
	
	public TypeCoveredByCompetingSchemes_Filter(
			TypeCoveredByCompetingSchemes_Filter.ParameterSetting parameterSetting, 
			SearchPathList pathsToFilter, 
			Set<StringWithSetOfSearchPaths> coveredTypesToSchemes) {
		
		pathsToFilterAsAList = pathsToFilter;
		pathsToFilterAsASet = new TreeSet<SearchPath>(pathsToFilter);

		this.parameterSetting = parameterSetting;
		
		for (StringWithSetOfSearchPaths coveredTypeWithSchemes : coveredTypesToSchemes) {
			this.coveredTypesWithSchemesByType.put(coveredTypeWithSchemes.getString(), 
												 coveredTypeWithSchemes);
		}
	}

	public SearchPathList performSearchStep() {
		
		// Since I will be actually removing schemes/paths as I filter them 
		// a simple iterator over the Set covertedTypesToSchemes may get confused.  
		// Since I will not actually REMOVE any entries from coveredTypesToSchemes 
		// (but just modify them) I can put coveredTypesToSchemes into an ArrayList.
		List<StringWithSetOfSearchPaths> coveredTypesWithSchemesAsAList = 
			new ArrayList<StringWithSetOfSearchPaths>(coveredTypesWithSchemesByType.values());
		
		for (StringWithSetOfSearchPaths stringWithSetOfSearchPaths : coveredTypesWithSchemesAsAList) {
			
			// The initial data structure
			Map<Context, Set<SearchPath>> searchPathsByStem = 
				stringWithSetOfSearchPaths.getSearchPathsByStem();
			
			// An intermediate data structure to aid filtering the competing schemes that cover a 
			// particular type.
			Map<Integer, Map<Context, Set<SearchPath>>> searchPathsByStemByLevel = 
				new TreeMap<Integer, Map<Context, Set<SearchPath>>>();

			for (Context context : searchPathsByStem.keySet()) {
				Set<SearchPath> searchPaths_fromByStem = searchPathsByStem.get(context);
				
				for (SearchPath searchPath : searchPaths_fromByStem) {
					Scheme terminalSchemeInPath = searchPath.getTerminalScheme();
					
					Integer level = terminalSchemeInPath.level();
					
					if ( ! searchPathsByStemByLevel.containsKey(level)) {
						searchPathsByStemByLevel.put(level, new TreeMap<Context, Set<SearchPath>>());
					}
					
					Map<Context, Set<SearchPath>> stemsToSearchPaths_fromByLevel = 
						searchPathsByStemByLevel.get(level);
					
					if ( ! stemsToSearchPaths_fromByLevel.containsKey(context)) {
						stemsToSearchPaths_fromByLevel.put(context, new TreeSet<SearchPath>());
					}
					
					Set<SearchPath> searchPaths_fromByStemByLevel = stemsToSearchPaths_fromByLevel.get(context);
					searchPaths_fromByStemByLevel.add(searchPath);
				}
			}
			
			// Now traverse the intermediate data structure looking for schemes that can be
			// filtered out.
			levelLoop:
			for (Integer level : searchPathsByStemByLevel.keySet()) {
				Map<Context, Set<SearchPath>> stemsToSearchPaths_fromByLevel = 
					searchPathsByStemByLevel.get(level);
				
				// This filter needs at least 2 suggested segmentations for a particular
				// word type, in order to be able to filter anything.
				int numberOfUniqueSuggestedSegmentations = stemsToSearchPaths_fromByLevel.keySet().size();
				if (numberOfUniqueSuggestedSegmentations <= 1) {
					continue;
				}
				
				// This filter will not apply if any of the segmentations have
				// more than one selected scheme voting for them.
				for (Set<SearchPath> paths_fromByStemByLevel : stemsToSearchPaths_fromByLevel.values()) {
					if (paths_fromByStemByLevel.size() > 1) {
						continue levelLoop;
					}
				}
				
				int numOfStemsInSchemeWithMostStems = -1;
				for (Set<SearchPath> paths_fromByStemByLevel : stemsToSearchPaths_fromByLevel.values()) {
					// We know there is exactly one search path in this set
					SearchPath searchPath = paths_fromByStemByLevel.iterator().next();
					Scheme terminalSchemeInPath = searchPath.getTerminalScheme();
					int numberOfStemsInScheme = terminalSchemeInPath.adherentSize();
					if (numberOfStemsInScheme > numOfStemsInSchemeWithMostStems) {
						numOfStemsInSchemeWithMostStems = numberOfStemsInScheme;
					}
				}
				
				// Actually filter/remove all the scheme(s)/path(s) and associated stems
				// that have fewer than numOfStemsInSchemeWithMostStems that are at this level.
				for (Set<SearchPath> paths_fromByStemByLevel : stemsToSearchPaths_fromByLevel.values()) {
					// We know there is exactly one search path in this set
					SearchPath searchPath = paths_fromByStemByLevel.iterator().next();
					Scheme terminalSchemeInPath = searchPath.getTerminalScheme();
					int numberOfStemsInScheme = terminalSchemeInPath.adherentSize();
					
					if (numberOfStemsInScheme < numOfStemsInSchemeWithMostStems) {
						
						System.err.println();
						System.err.println("Looking at the covered Type:");
						System.err.println();
						System.err.println(stringWithSetOfSearchPaths);
						System.err.println();
						System.err.println("Removing:");
						System.err.println();
						System.err.println(searchPath);
						System.err.println();
						System.err.println("Because the level " + level + " selected schemes that");
						System.err.println("  cover " + stringWithSetOfSearchPaths.getString());
						System.err.println("  are:");
						System.err.println();
						System.err.println(stemsToSearchPaths_fromByLevel);
						System.err.println();
						
						removeSearchPath(searchPath);
					}
				}
			}
		}
		
		// Reconcile the temporary set version of 'searchPaths' and the official in-order
		// list version.
		Iterator<SearchPath> iter = pathsToFilterAsAList.iterator();
		while (iter.hasNext()) {
			SearchPath searchPath = iter.next();
			
			if ( ! pathsToFilterAsASet.contains(searchPath)) {
				iter.remove();  // safely remove from the underlying list (or collection) while iterating
			}
		}
		
		return pathsToFilterAsAList;
	}

	private void removeSearchPath(SearchPath searchPath) {
		// The searchPath must be removed from the Set version of 'searchPaths' and 
		// from the 'coveredTypesWithSchemesByType' data structure.
		
		// First the easy one: 'pathsToFilterAsASet'
		pathsToFilterAsASet.remove(searchPath);
		
		// Now the hard one: 'coveredTypesWithSchemesByType' -- but most of the tricky stuff
		// has been pushed down to StringWithSetOfSearchPaths.remove().
		List<String> coveredTypes = searchPath.getTerminalScheme().getCoveredWordTypes();
		for (String coveredType : coveredTypes) {
			StringWithSetOfSearchPaths coveredTypeWithPaths = 
				coveredTypesWithSchemesByType.get(coveredType);
			coveredTypeWithPaths.remove(searchPath);
		}
	}

	public String getName() {
		return getNameStatic();
	}

	private static String getNameStatic() {
		return SEARCH_STEP_NAME;
	}

	@Override
	public String toString() {
		return parameterSetting.toString();
	}
}
