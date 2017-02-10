package run.rook.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.gson.Gson;

import run.rook.cli.message.pkg.PackageInfo;
import run.rook.cli.message.pkg.PackageMessageType;
import run.rook.cli.message.pkg.PackageRequest;
import run.rook.cli.message.pkg.PackageResponse;
import run.rook.cli.message.pkg.ServiceInfo;
import run.rook.cli.message.process.ProcessInfo;
import run.rook.cli.message.process.ProcessMessageType;
import run.rook.cli.message.process.ProcessRequest;
import run.rook.cli.message.process.ProcessResponse;
import run.rook.cli.message.ui.UiInfo;
import run.rook.cli.message.ui.UiMessageType;
import run.rook.cli.message.ui.UiRequest;
import run.rook.cli.message.ui.UiResponse;

public class CLI implements Runnable {

	private static final int CONNECT_TIMEOUT_SECONDS = 3;
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.out.println("Loop Mode Format:  CLI <url>");
			System.out.println("Single Cmd Format: CLI <url> <command>");
			System.exit(1);
		}
		String ws = args[0];
		CLI cli = new CLI(ws, new BufferedReader(new InputStreamReader(System.in)), System.out);
		if(args.length == 1) {
			if("help".equalsIgnoreCase(args[0])) {
				// display help
				System.out.println(cli.help());
			} else {
				// loop mode
				cli.run();
			}
		} else {
			// single command
			StringBuilder input = new StringBuilder();
			for(int i = 1; i < args.length; i++) {
				input.append(args[i]).append(" ");
			}
			cli.process(input.toString().trim());
		}
		System.exit(0);
	}

	private final String url;
	private final BufferedReader clin;
	private final PrintStream clout;
	private final Gson gson = new Gson();
	
	public CLI(String url, BufferedReader clin, PrintStream clout) {
		this.url = url;
		this.clin = clin;
		this.clout = clout;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				clout.print("# ");
				String input = clin.readLine();
				process(input);
			} catch(ArrayIndexOutOfBoundsException e) {
				clout.println("ERROR: Could not parse. Wrong number of arguments provided.");
			} catch(Throwable t) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				clout.println(sw.toString());
			}
		}
	}

	public void process(String input) throws IOException {
		if(input.trim().length() > 0) {
			String[] cmd = parse(input);
			switch(cmd[0]) {
			case "exit":
				return;
			case "help":
				clout.println(help());
				break;
			case "process":
				clout.println(process(Arrays.copyOfRange(cmd, 1, cmd.length)));
				break;
			case "package":
				clout.println(pkg(Arrays.copyOfRange(cmd, 1, cmd.length)));
				break;
			case "config":
				clout.println("CONFIG commands are not supported quite yet");
				break;
			case "ui":
				clout.println(ui(Arrays.copyOfRange(cmd, 1, cmd.length)));
				break;
			default:
				clout.println(cmd[0] + " is not a recognized command type");
				break;
			}
		}
	}

	private String[] parse(String input) {
		return input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}

	private String help() {
		return "commands:"
				+ "\n  package list"
				+ "\n  package get <package_id>"
				+ "\n  package add <path_to_zip_file>"
				+ "\n  package remove <package_id>"
				+ "\n  package refresh"
				+ "\n  process list"
				+ "\n  process start <package_id> <service_id> <argument>[]"
				+ "\n  process stop <process_id>"
				+ "\n  process stop_forcibly <process_id>"
				+ "\n  process clean <process_id:optional>"
				+ "\n  process log <process_id>"
				+ "\n  config list <package_id:optional> <service_id:optional>"
				+ "\n  config get <package_id> <service_id> <config_name>"
				+ "\n  config upload <package_id> <service_id> <config_name> <path_to_config_file>"
				+ "\n  config remove <package_id> <service_id> <config_name>"
				+ "\n  ui list"
				+ "\n  ui get <ui_id>"
				+ "\n  ui add <path_to_zip_file>"
				+ "\n  ui remove <ui_id>"
				+ "\n  ui refresh"
				+ "\n  help"
				+ "\n  exit";
	}
	
	private String process(String[] params) throws IOException {
		switch(params[0]) {
		case "list":
			return processList();
		case "start":
			return processStart(params[1], params[2], Arrays.copyOfRange(params, 2, params.length));
		case "stop":
			return processStop(params[1]);
		case "stop_forcibly":
			return processStopForcibly(params[1]);
		case "clean":
			return processClean(params.length > 1 ? params[1] : null);
		case "log":
			return processLog(params[1]);
		default:
			return params[0] + " is an unrecognized process command";
		}
	}

	private String processList() throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.LIST));
		if(resp.getResult().getSuccess()) {
			StringBuilder sb = new StringBuilder();
			for(ProcessInfo p : resp.getProcesses()) {
				sb.append(parseProcessInfo(p)).append("\n");
			}
			return sb.toString();
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String processStart(String packageId, String serviceId, String[] params) throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.START)
				.setPackage(packageId)
				.setService(serviceId)
				.setArguments(params));
		if(resp.getResult().getSuccess()) {
			return parseProcessInfo(resp.getProcess());
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String processStop(String processId) throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.STOP)
				.setId(processId));
		if(resp.getResult().getSuccess()) {
			return "Stop Executed";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String processStopForcibly(String processId) throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.STOP_FORCIBLY)
				.setId(processId));
		if(resp.getResult().getSuccess()) {
			return "Stop Forcibly Executed";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String processClean(String processId) throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.CLEAN)
				.setId(processId));
		if(resp.getResult().getSuccess()) {
			return "Clean Executed";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String processLog(String processId) throws IOException {
		ProcessResponse resp = send(new ProcessRequest()
				.setType(ProcessMessageType.LOG)
				.setId(processId));
		if(resp.getResult().getSuccess()) {
			return resp.getLog();
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String parseProcessInfo(ProcessInfo p) {
		StringBuilder sb = new StringBuilder();
		sb.append(p.getId())
				.append("    ").append(p.getPackageName())
				.append("    ").append(p.getServiceName());
		return sb.toString();
	}


	private String pkg(String[] params) throws IOException {
		switch(params[0]) {
		case "list":
			return pkgList();
		case "get":
			return pkgGet(params[1]);
		case "add":
			return pkgAdd(params[1]);
		case "remove":
			return pkgRemove(params[1]);
		case "refresh":
			return pkgRefresh();
		default:
			return params[0] + " is an unrecognized package command";
		}
	}

	private String pkgList() throws IOException {
		PackageResponse resp = send(new PackageRequest()
				.setType(PackageMessageType.LIST));
		if(resp.getResult().getSuccess()) {
			StringBuilder sb = new StringBuilder();
			for(PackageInfo p : resp.getPackages()) {
				sb.append(parsePackageInfo(p)).append("\n");
			}
			return sb.toString();
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String pkgGet(String packageId) throws IOException {
		PackageResponse resp = send(new PackageRequest()
				.setType(PackageMessageType.GET)
				.setId(packageId));
		if(resp.getResult().getSuccess()) {
			return parsePackageInfo(resp.getPackage());
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String pkgAdd(String pathToZip) throws IOException {
		byte[] dataBytes = readFile(pathToZip);
		String data = Base64.getEncoder().encodeToString(dataBytes);
		PackageResponse resp = send(new PackageRequest()
				.setType(PackageMessageType.ADD)
				.setData(data));
		if(resp.getResult().getSuccess()) {
			return "Upload Successful";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private byte[] readFile(String pathToZip) throws IOException {
		// FIXME
		return new byte[0];
	}

	private String pkgRemove(String packageId) throws IOException {
		PackageResponse resp = send(new PackageRequest()
				.setType(PackageMessageType.REMOVE)
				.setId(packageId));
		if(resp.getResult().getSuccess()) {
			return "Removed " + packageId;
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String pkgRefresh() throws IOException {
		PackageResponse resp = send(new PackageRequest()
				.setType(PackageMessageType.REFRESH));
		if(resp.getResult().getSuccess()) {
			return "I'm Feeling Refreshed";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String parsePackageInfo(PackageInfo p) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(p.getId()).append("] ").append(p.getName()).append("\n");
		for(ServiceInfo s : p.getServices().values()) {
			sb.append("  ").append(s.getId()).append(": ").append(s.getName()).append("\n");
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	

	private String ui(String[] params) throws IOException {
		switch(params[0]) {
		case "list":
			return uiList();
		case "get":
			return uiGet(params[1]);
		case "add":
			return uiAdd(params[1]);
		case "remove":
			return uiRemove(params[1]);
		case "refresh":
			return uiRefresh();
		default:
			return params[0] + " is an unrecognized ui command";
		}
	}

	private String uiList() throws IOException {
		UiResponse resp = send(new UiRequest()
				.setType(UiMessageType.LIST));
		if(resp.getResult().getSuccess()) {
			StringBuilder sb = new StringBuilder();
			for(UiInfo p : resp.getUIs()) {
				sb.append(parseUiInfo(p)).append("\n");
			}
			return sb.toString();
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String uiGet(String packageId) throws IOException {
		UiResponse resp = send(new UiRequest()
				.setType(UiMessageType.GET)
				.setId(packageId));
		if(resp.getResult().getSuccess()) {
			return parseUiInfo(resp.getUI());
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String uiAdd(String pathToZip) throws IOException {
		byte[] dataBytes = readFile(pathToZip);
		String data = Base64.getEncoder().encodeToString(dataBytes);
		UiResponse resp = send(new UiRequest()
				.setType(UiMessageType.ADD)
				.setData(data));
		if(resp.getResult().getSuccess()) {
			return "Upload Successful";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}

	private String uiRemove(String packageId) throws IOException {
		UiResponse resp = send(new UiRequest()
				.setType(UiMessageType.REMOVE)
				.setId(packageId));
		if(resp.getResult().getSuccess()) {
			return "Removed " + packageId;
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String uiRefresh() throws IOException {
		UiResponse resp = send(new UiRequest()
				.setType(UiMessageType.REFRESH));
		if(resp.getResult().getSuccess()) {
			return "I'm Feeling Refreshed";
		} else {
			return "ERROR: " + resp.getResult().getError();
		}
	}
	
	private String parseUiInfo(UiInfo u) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(u.getId()).append("] ").append(u.getName()).append("\n");
		return sb.toString();
	}
	
	private ProcessResponse send(ProcessRequest request) throws IOException {
		String json = send("PROCESS", gson.toJson(request));
		return gson.fromJson(json, ProcessResponse.class);
	}
	
	private PackageResponse send(PackageRequest request) throws IOException {
		String json = send("PACKAGE", gson.toJson(request));
		return gson.fromJson(json, PackageResponse.class);
	}
	
	private UiResponse send(UiRequest request) throws IOException {
		String json = send("UI", gson.toJson(request));
		return gson.fromJson(json, UiResponse.class);
	}
	
	private String send(String protocol, String json) throws IOException {
		try {
			RequestResponseWebSocket socket = new RequestResponseWebSocket(json);
			WebSocketClient client = new WebSocketClient();
	        client.start();
	        URI url = new URI(this.url);
	        ClientUpgradeRequest request = new ClientUpgradeRequest();
	        request.setSubProtocols(protocol);
	        Future<Session> connectFuture = client.connect(socket,url,request);
	        Session session = connectFuture.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	        if(session == null) {
	        	throw new IOException("Could not connect to " + url);
	        }
	        return socket.getResponse().get();
		} catch(IOException e) {
			throw e;
		} catch(Throwable t) {
			throw new IOException("Could not send " + json, t);
		}
	}
	
}
