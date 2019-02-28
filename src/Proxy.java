import java.io.*;
import java.net.*;
import java.util.*;

public class Proxy extends Thread {
	public static final int PORT_NUM = 5002;
	private ServerSocket socket;
	private InetSocketAddress address;
	private volatile boolean running = true;
	private LinkedList<String> blacklisted;
	private LinkedList<String> cached;

	public Proxy() throws IOException {
		this.cached = new LinkedList<String>();
		this.blacklisted = new LinkedList<String>();
		this.socket = new ServerSocket();
		this.address = new InetSocketAddress("127.0.0.1", PORT_NUM);
		this.socket.bind(address);
		System.out.println(socket + ":CONNECTED");
		start();
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Beep Blorp Beep");
		System.out.println("Starting your proxy");
		Proxy proxy = new Proxy();
		proxy.listen();
	}

	public void listen() throws IOException {
		while (running) {
			Socket sock = socket.accept();
			Thread thread = new Thread(new HttpRequest(sock, this.blacklisted, this.cached), null);
			thread.start();
		}
	}

	public void run() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Beep Blorp Beep");
		System.out.println("Type 'blacklist' to see your blacklisted web-addresses'cache' to see your cached web-addresses, or type a web-address to block.");
		System.out.println("(www.example.com)");
		System.out.println("Type 'close' to shut it down");
		System.out.println("Beep Blorp Beep");
		while (running) {
			String input = scanner.nextLine();
			if (input.equals("close")) {
				System.out.println("...");
				System.out.println("systems shutting down");
				running = false;
			} else {
				while ((input.contentEquals("close")) == false) {
					if (input.equals("cache")) {
						System.out.println("...");
						System.out.println("cache:" + cached);
						input = scanner.nextLine();
					} else if (input.equals("blacklist")) {
						System.out.println("...");
						System.out.println("blacklist:" + blacklisted);
						input = scanner.nextLine();
					} else if (input.startsWith("www.")) {
						if (isBlacklisted(input) == false) {
							blacklisted.add(input);
							System.out.println("Blacklisted:" + input);
							input = scanner.nextLine();
						}
					}
				}
			}
		}
		scanner.close();
	}

	public boolean isBlacklisted(String url) {
		if (blacklisted.contains(url)) {
			return true;
		}
		return false;
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}