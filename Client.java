import java.net.*;
import java.io.*;

public class Client {
  static final int REGISTRY_PORT = 9001;

  // args: <monitorHost>
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: Client <monitorHost>");
      return;
    }
    String monitor = args[0];

    String[] hp = askLeader(monitor).split(":");
    String host = hp[0]; int port = Integer.parseInt(hp[1]);

    while (true) {
      try {
        System.out.println(send(host, port, "READING:" + (50 + (int)(Math.random()*200))));
        if (Math.random() < 0.25) System.out.println(send(host, port, "QUERY"));
        Thread.sleep(100);
      } catch (Exception e) {
        Thread.sleep(300);
        hp = askLeader(monitor).split(":");
        host = hp[0]; port = Integer.parseInt(hp[1]);
      }
    }
  }

  static String askLeader(String monitorHost) throws Exception {
    try (Socket s = new Socket(monitorHost, REGISTRY_PORT)) {
      return new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
    }
  }

  static String send(String host, int port, String msg) throws Exception {
    try (Socket s = new Socket(host, port)) {
      var out = new PrintWriter(s.getOutputStream(), true);
      var in  = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out.println(msg);
      return in.readLine();
    }
  }
}
