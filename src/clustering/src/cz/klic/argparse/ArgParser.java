package cz.klic.argparse;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cz.klic.util.StringUtil;

/**
 * Class for parsing command line options loosely inspired by
 * Python's optparse module
 * 
 * Terminology: 
 * option - a named argument, a key-value pair. for example -n 10
 * positional argument - an unnamed argument, just a value
 * 
 * The parser expects options first, followed by the positional arguments
 * The option names must start with '-'.
 * 
 * The parser is able to automatically convert the option values
 * to any type, given the type has a constructor with exactly one
 * string parameter.
 *
 */
public class ArgParser {
	
	public static class Option<T> {
		
		private T value;
		
		private Class<T> type;
		
		/**
		 * names of the option as used in the command line e.g., "-v" or
		 * "--verbose"
		 */
		private Set<String> shellNames;

		/**
		 * If an option is a flag, it does not require a value to be set in the
		 * command line. It's presence among the arguments sets its value to
		 * true.
		 */
		private boolean flag = false;
		
		/**
		 * Description of the option. Used to generate help.
		 */
		private String desc;
		
		public Option(Class<T > type, Set<String> shellNames, String desc) {
			this.type = type;
			this.shellNames = shellNames;
			this.desc = desc;
		}
		
		public Option(Class<T > type, Set<String> shellNames) {
			this(type, shellNames, "");
		}
		
		public Option(Class<T > type, String[] shellNames, String desc) {
			this(type, new HashSet<String>(shellNames.length), desc);
			for (String name : shellNames) {
				this.shellNames.add(name);
			}
		}
		
		public Option(Class<T > type, String[] shellNames) {
			this(type, shellNames, "");
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public void setValueAsString(String val) throws Exception {
			Constructor<T> constructor = this.type.getConstructor(String.class);
			this.value = constructor.newInstance(val);
		}
		
		public Class<T> getType() {
			return type;
		}

		public Set<String> getShellNames() {
			return shellNames;
		}

		public boolean isFlag() {
			return flag;
		}

		public void setIsFlag(boolean flag) {
			this.flag = flag;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}
	}
	
	private Map<String, Option<?>> opts;
	
	//positinal ('unnamed') arguments
	private String[] args;

	public ArgParser() {
		opts = new HashMap<String, ArgParser.Option<?>>();
	}

	public <T> void addOption(String name, Class<T> type, String desc, String ... shellNames) throws Exception {
		this.addOption(name, type, false, desc, shellNames);
	}
	
	public <T> void addFlag(String name, String desc, String... shellNames) throws Exception {
		this.addOption(name, Boolean.class, true, desc, shellNames);
		this.getOption(name).setValue(false);
	}
	
	/**
	 * 
	 * @param name
	 *            name by which the option is identified. This name is used when
	 *            retrieving the option's value by getOptionVal(name).
	 * @param type
	 * @param isFlag
	 * @param desc
	 *            description used in the usage message
	 * @param shellNames
	 *            names to look for in the shell arguments
	 * @throws Exception
	 */
	protected <T> void addOption(String name, Class<T> type, boolean isFlag, String desc, String... shellNames) throws Exception {
		
		if (shellNames.length == 0) {
			throw new Exception("At least one shell name has to be assigned to option " + name);
		}
		
		for (String shName : shellNames) {
			if (!shName.startsWith("-")) {
				throw new Exception("Shell names must start with '-'");
			}
		}
		
		try {
			type.getConstructor(String.class);
		} catch (NoSuchMethodException e) {
			throw new Exception("Type " + type + "has to have a constructor from String", e);
		}
				
		Option<T> opt = new Option<T>(type, shellNames, desc);
		opt.setIsFlag(isFlag);
		this.opts.put(name, opt);
	}

	public void parseArgs(String ... args) throws Exception {
		int idx = 0;
		while (idx < args.length) {
			String shName = args[idx];
			if (!shName.startsWith("-")) {
				this.args = Arrays.copyOfRange(args, idx, args.length);
				return;
			}
			
			boolean notFound = true;
			for (Option<?> option: this.opts.values()) {
				if (option.shellNames.contains(shName)) {
					if (option.isFlag()) {
						option.setValueAsString("true");
					} else {
						++idx;
						if (idx == args.length) {
							throw new Exception("value for option " + shName + " is missing");
						}
						String strVal = args[idx];
						option.setValueAsString(strVal);
					}
					notFound = false;
					break;
				}
			}
			
			//the option starts with - but is not found
			if (notFound) {
				//is it a negative numerical argument?
				try {
					Float.parseFloat(shName);
					//we shall consider it a positional argument
					this.args = Arrays.copyOfRange(args, idx, args.length);
					return;
				} catch (NumberFormatException e) {
					// It's not a number, throw an exception
					throw new UnknownOptionException(shName, idx);
				}
			}
			
			++idx;
		}
	}
	
	/**
	 * Reads a Properties object, reading the options by names (not the "shell"
	 * names) and converting to given types.
	 * 
	 * @param props
	 * @param override
	 *            override previously read values, for example from command line
	 * @throws Exception
	 */
	public void readProperties(Properties props, boolean override) throws Exception {
		for (Object key : props.keySet()) {
			String propName = (String)key;
			if (this.opts.containsKey(propName)) {
				//the value is already set override flag is false
				if (!override && this.opts.get(propName).getValue() != null) {
					continue;
				}
				String value = props.getProperty(propName);
				this.opts.get(propName).setValueAsString(value);
			}
		}
	}
	
	public void readProperties(Properties props) throws Exception {
		readProperties(props, true);
	}

	public Map<String, Option<?>> getOptions() {
		return opts;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Option<T> getOption(String name) {
		return (Option<T>) this.opts.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOptionVal(String name) {
		return (T) this.opts.get(name).getValue();
	}
	
	/**
	 * @return positional arguments
	 */
	public String[] getPosArgs() {
		return args;
	}
	
	/**
	 * @param programName name of the program to show the message for
	 * @return a help message describing the options
	 */
	public String getUsage(String programName) {
		StringBuilder res = new StringBuilder();
		
		res.append("Usage: ").append(programName).append(" [OPTION]...");
		res.append("\nOptions:\n");
		
		for (String optName : this.opts.keySet()) {
			String description = this.opts.get(optName).getDesc();
			Set<String> shNameSet = this.opts.get(optName).getShellNames();			
			ArrayList<String> shNames = new ArrayList<String>(shNameSet);
			//Sort the shell names by length to show the short version first
			Collections.sort(shNames, new Comparator<String>() {

				@Override
				public int compare(String n1, String n2) {
					// return Integer.compare(n1.length(), n2.length()); 
					// is Java 7 only
					return Integer.valueOf(n1.length()).compareTo(
							Integer.valueOf(n2.length()));
				}
			});
			
			res.append('\t').append(StringUtil.join(", ", shNames));
			res.append('\t').append(description).append('\n');
		}
		
		return res.toString();
	}

}
