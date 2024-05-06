package io.intino.ness.terminal.builder;

import io.intino.builder.BuildConstants;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalcRunner {

	private static final Logger LOG = Logger.getGlobal();

	private TerminalcRunner() {
	}

	public static void main(String[] args) {
		final boolean verbose = args.length != 2 || Boolean.parseBoolean(args[1]);
		if (verbose) System.out.println(BuildConstants.PRESENTABLE_MESSAGE + "Starting compiling");
		try {
			File argsFile;
			if (checkArgumentsNumber(args) || (argsFile = checkConfigurationFile(args[0])) == null)
				throw new IntinoException("Error finding args file");
			new TerminalCompilerRunner(verbose).run(argsFile);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage() == null ? e.getStackTrace()[0].toString() : e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static File checkConfigurationFile(String arg) {
		final File argsFile = new File(arg);
		if (!argsFile.exists()) {
			LOG.severe("%%mArguments file for Tara compiler not found/%m");
			return null;
		}
		return argsFile;
	}

	private static boolean checkArgumentsNumber(String[] args) {
		if (args.length < 1) {
			LOG.severe("%%mThere is no arguments for tara compiler/%m");
			return true;
		}
		return false;
	}
}
