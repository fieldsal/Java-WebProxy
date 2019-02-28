import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest implements Runnable {
	public static final int PORT_NUM = 5002;
	private String HttpRequest;
	private Socket socket;
	private LinkedList<String> blacklisted;
	private LinkedList<String> cached;

	HttpRequest(Socket sock, LinkedList<String> blocked, LinkedList<String> cache) throws IOException {
		socket = sock;
		String url;
		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		this.blacklisted = blocked;
		this.cached = cache;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		while ((url = bufferedReader.readLine()) != null) {
			System.out.println(url);
			if (url.contains("GET /")) {
				System.out.println("Beep Blop Beep");
				System.out.println("Connection with: " + url + " initiated");
				url = parseGetRequest(url);
				if (blacklisted.contains(url.substring(8))) {
					System.out.println("Beep Blorp Beep");
					System.out.println("Computer says no");
					System.out.println("Url Blacklisted: " + url);
				} else if (cache.contains(url) == false) {
					this.HttpRequest = url;
					run();
					url = url.substring(8);
					String cachedUrl = url + ".html";
					cache.add(url);
					cache.add(cachedUrl);
				} else {
					System.out.println("Beep Blorp Beep");
					System.out.println("Loading from cache");
				}
			}
		}
		inputStream.close();
		outputStream.close();
	}

	private String parseGetRequest(String url) {
		String[] data = url.split(" ", 3);
		String returnVal = data[1].substring(1);
		return returnVal;
	}

	private String getFileName(String url) {
		String returnVal = url.substring(8);
		return returnVal;
	}

	public void cacheOut(File file) throws IOException {
		System.out.println();
		String getLine;
		System.out.println("Beep Blorp Beep");
		System.out.println(file.toString() + " coming to you from cache");
		FileInputStream fileInputStream = new FileInputStream(file);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String response = "HTTP/1.0 200 OK\nProxy-agent: ProxyServer/1.0\n\r\n";
		System.out.println(response);
		while ((getLine = bufferedReader.readLine()) != null) {
			bufferedWriter.write(getLine);
		}
		bufferedReader.close();
	}

	@Override
	public void run() {
		try {
			String getLine;
			File file;
			String fileName;
			URL url;
			try {
				url = new URL(HttpRequest);
				System.out.println("Beep Blorp Beep");
				System.out.println("You want to go here:" + url.toString());
				if (this.blacklisted.contains(url.toString()) == false) {
					if (this.cached.contains(url.toString()) == false) {
						fileName = (getFileName(HttpRequest)) + ".html";
						PrintWriter printWriter = new PrintWriter(fileName, "UTF-8");
						try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
							System.out.println("Beep Blorp Beep");
							System.out.println("Send it to cache");
							System.out.println("saved as:" + fileName);
							while ((getLine = bufferedReader.readLine()) != null) {
								printWriter.println(getLine);
							}
							file = new File(fileName);
							System.out.println("Beep Blorp Beep");
							System.out.println("cache out");
							cacheOut(file);
							printWriter.close();
						} catch (IOException e) {
							System.out.println("file writen, could not send to browser");
						}
					}
				}
			} catch (MalformedURLException e) {
				System.out.println("Beep Blorp Beep");
				System.out.println("BEEEEEEEEEEEEEP");
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}