import java.util.ArrayDeque;
import java.util.Deque;

public class ServiceState {
  private final Deque<Integer> window = new ArrayDeque<>();
  private int seq = 0;

  public synchronized int append(int v) {
    if (window.size() == 20) window.removeFirst();
    window.addLast(v);
    return ++seq;
  }

  public synchronized int min() {
    return window.stream().min(Integer::compare).orElse(9999);
  }

  public synchronized int seq() { return seq; }

  // snapshot format: seq|v1,v2,...
  public synchronized String snapshot() {
    StringBuilder sb = new StringBuilder();
    sb.append(seq).append("|");
    boolean first = true;
    for (int v : window) {
      if (!first) sb.append(",");
      sb.append(v);
      first = false;
    }
    return sb.toString();
  }

  public synchronized void load(String snap) {
    String[] parts = snap.split("\\|");
    seq = Integer.parseInt(parts[0]);
    window.clear();
    if (parts.length > 1 && !parts[1].isBlank()) {
      for (String s : parts[1].split(",")) window.addLast(Integer.parseInt(s));
    }
  }
}
