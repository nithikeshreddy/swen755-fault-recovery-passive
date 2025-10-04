# SWEN-755: Fault Recovery with Passive Redundancy

This project shows how to use **Fault Detection** and **Fault Recovery** with **Passive Redundancy**.  
The system has a Primary server, a Backup server, a Monitor, and a Client. If the Primary fails, the Monitor promotes the Backup to become the new leader. The Client then keeps working without losing much data.

## How to Run
Open four terminals and run these commands:
1.java Backup
2.java Primary 127.0.0.1
3.java Monitor 127.0.0.1 127.0.0.1
4.java Client 127.0.0.1

## Ports Used
1.7010 → Service (leader listens here)
2.8001 → Checkpoints (Primary → Backup)
3.9001 → Registry (Client → Monitor)
4.9002 → Promotion (Monitor → Backup)

## Steps for the demo
1.Start all four processes as above.
2.Client prints ACK:<number> and sometimes MIN:<value>,SEQ:<number>.
3.Kill the Primary (Ctrl+C) or wait for a random crash.
4.Monitor shows: Leader not responding → PROMOTE backup
5.Backup shows: PROMOTED → serving on 7010.
6.Client continues to send and receive responses with little interruption.

## Libraries Used
This project uses only standard Java libraries included in the JDK:
1.java.net.* — networking (Socket, ServerSocket, InetSocketAddress)
2.java.io.* — input/output (BufferedReader, InputStreamReader, PrintWriter)
3.java.util.* — data structures and utilities (Deque, ArrayDeque, Random)


## How to Build

Compile all Java files:
```bash
javac *.java


