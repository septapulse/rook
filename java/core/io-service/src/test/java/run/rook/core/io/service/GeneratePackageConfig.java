package run.rook.core.io.service;

import java.io.FileOutputStream;
import java.io.PrintStream;

import run.rook.test.tools.PackageConfigGenerator;

public class GeneratePackageConfig {

	public static void main(String[] args) throws Exception {
		PrintStream p = new PrintStream(new FileOutputStream("rook.cfg"));
		PackageConfigGenerator.generate("rook_core_io", "Core IO", "run.rook.core.io.service", p);
		p.close();
		System.out.println("Done");
	}
}
