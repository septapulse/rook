package rook.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class CLI implements Runnable {
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Format: CLI <ws>");
			System.exit(1);
		}
		String ws = args[0];
		new CLI(ws, new BufferedReader(new InputStreamReader(System.in)), System.out).run();
	}

	private final BufferedReader clin;
	private final PrintStream clout;
	
	public CLI(String ws, BufferedReader clin, PrintStream clout) {
		this.clin = clin;
		this.clout = clout;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				clout.print("# ");
				String input = clin.readLine();
				if(input.trim().length() > 0) {
					String[] cmd = parse(input);
					switch(cmd[0]) {
					case "exit":
						return;
					case "help":
						clout.println(help());
						break;
					case "PROCESS":
						clout.println(process(Arrays.copyOfRange(cmd, 1, cmd.length)));
						break;
					case "PACKAGE":
						clout.println(pkg(Arrays.copyOfRange(cmd, 1, cmd.length)));
						break;
					case "CONFIG":
						clout.println("CONFIG commands are not supported quite yet");
						break;
					default:
						clout.println(cmd[0] + " is not a recognized command type");
						break;
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				clout.println("ERROR: Could not parse. Wrong number of arguments provided.");
			} catch(IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				clout.println(sw.toString());
				clout.println("Encountered IOException. Exiting...");
				break;
			} catch(Throwable t) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				clout.println(sw.toString());
			}
		}
	}

	private String[] parse(String input) {
		return input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}

	private String help() {
		return "commands:"
				+ "\n  PACKAGE LIST"
				+ "\n  PACKAGE GET <package_id>"
				+ "\n  PACKAGE ADD <package_id> <path_to_zip_file>"
				+ "\n  PACKAGE REMOVE <package_id>"
				+ "\n  PACKAGE REFRESH"
				+ "\n  PROCESS STATUS"
				+ "\n  PROCESS START <package_id> <service_id> <argument>[]"
				+ "\n  PROCESS STOP <process_id>"
				+ "\n  PROCESS STOP_FORCIBLY <process_id>"
				+ "\n  PROCESS CLEAN <process_id:optional>"
				+ "\n  PROCESS LOG <process_id>"
				+ "\n  CONFIG LIST <package_id:optional> <service_id:optional>"
				+ "\n  CONFIG GET <package_id> <service_id> <config_name>"
				+ "\n  CONFIG UPLOAD <package_id> <service_id> <config_name> <path_to_config_file>"
				+ "\n  CONFIG REMOVE <package_id> <service_id> <config_name>"
				+ "\n  help"
				+ "\n  exit";
	}
	
	private String process(String[] params) {
		switch(params[0]) {
		case "STATUS":
			return processStatus();
		case "START":
			return processStart(params[1], params[2], Arrays.copyOfRange(params, 2, params.length));
		case "STOP":
			return processStop(params[1]);
		case "STOP_FORCIBLY":
			return processStopForcibly(params[1]);
		case "CLEAN":
			return processClean(params.length > 1 ? params[1] : null);
		case "LOG":
			return processLog(params[1]);
		default:
			return params[0] + " is an unrecognized PROCESS command";
		}
	}

	private String processStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	private String processStart(String packageId, String serviceId, String[] params) {
		// TODO Auto-generated method stub
		return null;
	}

	private String processStop(String processId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String processStopForcibly(String processId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String processClean(String processId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String processLog(String processId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String pkg(String[] params) {
		switch(params[0]) {
		case "LIST":
			return pkgList();
		case "GET":
			return pkgGet(params[1]);
		case "ADD":
			return pkgAdd(params[1], params[2]);
		case "REMOVE":
			return pkgRemove(params[1]);
		case "REFRESH":
			return pkgRefresh();
		default:
			return params[0] + " is an unrecognized PACKAGE command";
		}
	}

	private String pkgList() {
		// TODO Auto-generated method stub
		return null;
	}

	private String pkgGet(String packageId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String pkgAdd(String packageId, String pathToZip) {
		// TODO Auto-generated method stub
		return null;
	}

	private String pkgRemove(String packageId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String pkgRefresh() {
		// TODO Auto-generated method stub
		return null;
	}
}
