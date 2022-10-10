package io.intino.builder;


import io.intino.builder.operations.MasterNodeCodeGenerationOperation;
import io.intino.magritte.builder.TaraCompilerRunner;
import io.intino.magritte.builder.compiler.operations.StashGenerationOperation;
import io.intino.magritte.builder.core.errorcollection.TaraException;
import io.intino.magritte.compiler.shared.TaraBuildConstants;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NessRunner {

	private static final Logger LOG = Logger.getGlobal();

	private NessRunner() {
	}

	public static void main(String[] args) {
		final boolean verbose = args.length != 2 || Boolean.parseBoolean(args[1]);
		if (verbose) System.out.println(TaraBuildConstants.PRESENTABLE_MESSAGE + "Starting compiling");
		try {
			File argsFile;
			if (checkArgumentsNumber(args) || (argsFile = checkConfigurationFile(args[0])) == null)
				throw new TaraException("Error finding args file");
			new TaraCompilerRunner(verbose, List.of(StashGenerationOperation.class, MasterNodeCodeGenerationOperation.class)).run(argsFile);
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
