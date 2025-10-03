import java.net.*;
import java.io.*;
import java.util.Random;

public class Primary {
  static final int SERVICE_PORT    = 7010; // clients connect here
  static final int CHECKPOINT_PORT = 8001; // send to backup

  static final ServiceState state = new ServiceState();

  // args: <backupHost>
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: Primary <backupHost>");
      return;
    }
    String backupHost = args[0];
    Random rng = new Random();

    try (ServerSocket ss = new ServerSocket(SERVICE_PORT)) {
      System.out.println("[Primary] serving on " + SERVICE_PORT);
      while (true) {
        try (Socket c = ss.accept()) {
          var in  = new BufferedReader(new InputStreamReader(c.getInputStream()));
          var out = new PrintWriter(c.getOutputStream(), true);
          String line = in.readLine();
          if (line == null) continue;

          if (line.startsWith("READING:")) {
            int v = Integer.parseInt(line.split(":")[1]);
            int seq = state.append(v);

            // checkpoint every op (simple)
            sendCheckpoint(backupHost, state.snapshot());

            out.println("ACK:" + seq);

            // random crash to simulate failure
            if (rng.nextDouble() < 0.0005) {
              System.out.println("[Primary] random crash now");
              System.exit(1);
            }
          } else if ("QUERY".equals(line)) {
            out.println("MIN:" + state.min() + ",SEQ:" + state.seq());
          } else if ("PING".equals(line)) {
            out.println("PONG");
          } else {
            out.println("ERR:unknown");
          }
        } catch (Exception ignore) {}
      }
    }
  }

  static void sendCheckpoint(String backupHost, String snapshot) {
    try (Socket s = new Socket(backupHost, CHECKPOINT_PORT)) {
      new PrintWriter(s.getOutputStream(), true).println(snapshot);
    } catch (Exception e) {
      // if backup is down, skip — simple demo
    }
  }
}
