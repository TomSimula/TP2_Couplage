package org.example.process;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.example.Config.Config;
import org.example.common.Parser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JdtParser {

	private ASTParser parser;
	public JdtParser(String projectPath){
		configureParser(projectPath);
	}

	private void configureParser(String projectPath){
		parser = ASTParser.newParser(AST.JLS4); // java +1.6
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.setBindingsRecovery(true);

		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);

		parser.setUnitName("");

		String[] sources = { projectPath };
		String[] classpath = { Config.jrePath };

		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
	}

	public CompilationUnit parse(File fileEntry) throws IOException {
		parser.setSource(FileUtils.readFileToString(fileEntry).toCharArray());
		return (CompilationUnit) parser.createAST(null); // create and parse
	}

}
