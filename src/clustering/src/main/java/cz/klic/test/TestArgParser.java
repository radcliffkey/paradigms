package cz.klic.test;

import java.util.Arrays;

import cz.klic.argparse.ArgParser;

public class TestArgParser {
	public static void main(String[] args) {
		ArgParser parser = new ArgParser();
		try {
			parser.addFlag("verbose", "be verbose","-v", "--verbose");
			parser.addOption("blacount", Double.class, "number of blas", "-c");
			parser.addOption("somename", String.class, "name of sth", "-n", "--name");

			System.out.println(parser.getUsage(TestArgParser.class.getName()));
			
			parser.parseArgs("-n", "kokot", "-v", "-c", "-10.56", "-56.9", "cla");

			String[] names = new String[] { "verbose", "blacount", "somename" };

			for (String name : names) {
				System.out.println(name);
				System.out.println(parser.<String>getOptionVal(name));
				System.out.println(parser.getOptionVal(name).getClass());
			}

			boolean verbose = parser.getOptionVal("verbose");
			System.out.println(verbose);

			String[] pargs = parser.getPosArgs();
			System.out.println("positional arguments:");
			System.out.println(Arrays.toString(pargs));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
