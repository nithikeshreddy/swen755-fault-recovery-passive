import java.net.*;
import java.io.*;

public class Monitor {
  static final int SERVICE_PORT  = 7010; // ping this on leader
  static final int REGISTRY_PORT = 9001; // clients ask "who is leader?"
  static final int PROMOTE_PORT  = 9002; // tell backup to become leader

  // args: <primaryHost> <backupHost>
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("Usage: Monitor <primaryHost> <backupHost>");
      return;
    }
    String primaryHost = args[0];
    String backupHost  = args[1];

    final String[] leaderHost = { primaryHost }; // start with primary as leader

    // 1) small registry for Client
    new Thread(() -> {
      try (ServerSocket ss = new ServerSocket(REGISTRY_PORT)) {
        while (true) try (Socket s = ss.accept()) {
          new PrintWriter(s.getOutputStream(), true)
              .println(leaderHost[0] + ":" + SERVICE_PORT);
        } catch (Exception ignore) {}
      } catch (IOException e) { e.printStackTrace(); }
    }).start();

    // 2) ping loop: every 500ms
    while (true) {
      Thread.sleep(500);
      if (!ping(leaderHost[0], SERVICE_PORT)) {
        System.out.println("[Monitor] Leader not responding → PROMOTE backup");
        try (Socket s = new Socket(backupHost, PROMOTE_PORT)) {
          new PrintWriter(s.getOutputStream(), true).println("PROMOTE");
          leaderHost[0] = backupHost; // update registry
        } catch (Exception e) {
          System.out.println("[Monitor] Could not contact backup for PROMOTE");
        }
      }
    }
  }

  static boolean ping(String host, int port) {
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(host, port), 200); // connect timeout
      var out = new PrintWriter(s.getOutputStream(), true);
      var in  = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out.println("PING");
      s.setSoTimeout(200);
      String line = in.readLine();
      return "PONG".equals(line);
    } catch (Exception e) {
      return false;
    }
  }
}
