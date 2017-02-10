package run.rook.core.io.raspberrypi;

import java.io.FileOutputStream;
import java.io.PrintStream;

import run.rook.test.tools.PackageConfigGenerator;

public class GeneratePackageConfig {

	public static void main(String[] args) throws Exception {
		PrintStream p = new PrintStream(new FileOutputStream("rook.cfg"));
		PackageConfigGenerator.generate("rook_raspberry_pi", "Raspberry Pi", "run.rook.core.io.raspberrypi", p);
		p.close();
		System.out.println("Done");
	}
}
