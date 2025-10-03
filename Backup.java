import java.net.*;
import java.io.*;

public class Backup {
  static final int SERVICE_PORT    = 7010; // will bind after PROMOTE
  static final int CHECKPOINT_PORT = 8001; // Primary -> Backup
  static final int PROMOTE_PORT    = 9002; // Monitor -> Backup

  static final ServiceState state = new ServiceState();
  static volatile boolean promoted = false;

  public static void main(String[] args) throws Exception {
    // 1) receive checkpoints while in standby
    new Thread(() -> {
      try (ServerSocket ss = new ServerSocket(CHECKPOINT_PORT)) {
        System.out.println("[Backup] receiving checkpoints on " + CHECKPOINT_PORT);
        while (!promoted) {
          try (Socket s = ss.accept()) {
            var br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String snap = br.readLine();
            if (snap != null) state.load(snap);
          } catch (Exception ignore) {}
        }
      } catch (IOException e) { e.printStackTrace(); }
    }).start();

    // 2) wait for PROMOTE from monitor
    try (ServerSocket ss = new ServerSocket(PROMOTE_PORT)) {
      try (Socket s = ss.accept()) {
        var br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        if ("PROMOTE".equals(br.readLine())) {
          promoted = true;
          System.out.println("[Backup] PROMOTED → serving on " + SERVICE_PORT);
          serveClients();
        }
      }
    }
  }

  static void serveClients() {
    try (ServerSocket ss = new ServerSocket(SERVICE_PORT)) {
      while (true) {
        try (Socket c = ss.accept()) {
          var in  = new BufferedReader(new InputStreamReader(c.getInputStream()));
          var out = new PrintWriter(c.getOutputStream(), true);
          String line = in.readLine();
          if (line == null) continue;

          if (line.startsWith("READING:")) {
            int v = Integer.parseInt(line.split(":")[1]);
            int s = state.append(v);
            out.println("ACK:" + s);
          } else if ("QUERY".equals(line)) {
            out.println("MIN:" + state.min() + ",SEQ:" + state.seq());
          } else if ("PING".equals(line)) {
            out.println("PONG");
          } else {
            out.println("ERR:unknown");
          }
        } catch (Exception ignore) {}
      }
    } catch (IOException e) { e.printStackTrace(); }
  }
}
