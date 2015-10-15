import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;

public class Server {
	private NetworkMaster netMaster;
	private Thread netMasterThread;
	private Timer eventTicker;
	private EventHandler eventHandler;

	private Thread renderer;

	private static int NETWORK_MAX_CONNECTIONS = 16;
	private static int NETWORK_PORT = 7767;
	private static int SERVER_VERSION = 1;

	private World world;

	private ArrayList<Packet.Modification> modifications;

	enum ThreadNames {
		Bashful, Doc, Dopey, Grumpy, Happy, Sleepy, Sneezy
	}

	public static void main(String[] args) {
		new Server();
	}

	public Server() {
		netMaster = new NetworkMaster();
		modifications = new ArrayList<Packet.Modification>();
		world = new World(true, 100, 6, 64, 64, new Util());
		world.getUtil().Log("Initializing server...");

		renderer = new Thread(new Renderer());
		renderer.start();

		netMasterThread = new Thread(netMaster);
		netMasterThread.start();

		eventHandler = new EventHandler();
		eventTicker = new Timer(1, eventHandler);
		eventTicker.start();
	}

	public class EventHandler implements ActionListener{
		NetworkMaster netMaster;

		public EventHandler() {
			netMaster = Server.this.netMaster;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < netMaster.networkThreads.length; i++) {
				if (netMaster.networkThreads[i] != null) {
					if (!netMaster.networkThreads[i].isAlive()) {
						netMaster.networkThreads[i] = null;
						netMaster.networkSockets[i] = null;
					}
				}
			}
		}
	}

	public class NetworkMaster implements Runnable {
		ServerSocket serverSocket;

		Thread[] networkThreads;
		NetworkSocket[] networkSockets;
		Socket socket;

		public NetworkMaster() {
			networkThreads = new Thread[Server.NETWORK_MAX_CONNECTIONS];
			networkSockets = new NetworkSocket[Server.NETWORK_MAX_CONNECTIONS];
		}

		public void run() {
			try {
				try {
					serverSocket = new ServerSocket(NETWORK_PORT);
				}catch (Exception e) {

				}

				while (true) {
					world.getUtil().Log("Awaiting connections on Port " + serverSocket.getLocalPort() + "...");
					socket = serverSocket.accept();

					world.getUtil().Log("Connection requested from " + socket.getRemoteSocketAddress() + "... Delegating thread.");

					boolean success = false;
					for (int i = 0; i < networkThreads.length; i++) {
						boolean isClear = false;

						if (networkThreads[i] != null) {
							if (!networkSockets[i].GetActive()) {
								world.getUtil().Log("Allocating space for network user...");
								networkThreads[i].interrupt();
								networkThreads[i] = null;
								networkSockets[i] = null;
								isClear = true;

								System.gc();
							}
						}else {
							isClear = true;
						}

						if (isClear) {
							world.getUtil().Log("Allocating thread " + i + ". Preparing network setup...");
							Random rand = new Random();
							String randomName = ThreadNames.values()[rand.nextInt(ThreadNames.values().length)].toString() + rand.nextInt(9) + rand.nextInt(9) + rand.nextInt(9);
							networkThreads[i] = new Thread(networkSockets[i] = new NetworkSocket(socket, randomName));
							networkThreads[i].start();
							success = true;
							break;
						}
					}
					if (!success) {
						world.getUtil().Log("CRITICAL ERROR. I DON'T HAVE ANYWHERE TO PUT MY NEXT CLIENT.");
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (Exception e) {

				}
			}
		}

		public Socket GetCurrentSocket() {
			return socket;
		}
	}

	public class Renderer implements Runnable {
		private final static int MAX_FPS = 60;	
		private final static int MAX_FRAME_SKIPS = 5;	
		private final static int FRAME_PERIOD = 1000 / MAX_FPS;	

		@Override
		public void run() {
			long beginTime;
			long timeDiff;
			int sleepTime;
			int framesSkipped;

			sleepTime = 0;

			while (true) {
				try {
					beginTime = System.currentTimeMillis();
					framesSkipped = 0;
					world.update();
					timeDiff = System.currentTimeMillis() - beginTime;
					sleepTime = (int)(FRAME_PERIOD - timeDiff);

					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);	
						} catch (InterruptedException e) {}
					}

					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
						world.update();
						sleepTime += FRAME_PERIOD;	
						framesSkipped++;
					}
				} finally {
					Toolkit.getDefaultToolkit().sync();
				}
			}
		}
	}

	synchronized public ArrayList<Packet.Modification> getChanges() {
		ArrayList<Packet.Modification> m = modifications;
		modifications = new ArrayList<Packet.Modification>();
		return m;
	}

	public class NetworkSocket implements Runnable {
		private String name;
		private Socket server;
		private boolean active;
		private String remoteName;

		private String username;

		private String rank = "user";

		public NetworkSocket(Socket socket, String threadName) {
			active = true;

			name = threadName;
			server = socket;
		}

		public boolean GetActive() {
			return active;
		}

		@Override
		public void run() {
			DataInputStream in;
			DataOutputStream out;
			ObjectOutputStream  oos;
			ObjectInputStream ois;

			while(active) {
				try {
					oos = new ObjectOutputStream(new BufferedOutputStream(server.getOutputStream()));
					oos.flush();
					ois = new ObjectInputStream(new BufferedInputStream(server.getInputStream()));

					out = new DataOutputStream(server.getOutputStream());
					in = new DataInputStream(server.getInputStream());

					world.getUtil().Log("Activating " + name + " on port " + server.getLocalPort() + ".");
					world.getUtil().Log("[" + name + "] Authorizing " + server.getRemoteSocketAddress() + "...");

					String opening = in.readUTF();
					String[] entries = opening.split("%");

					String remoteVersion = entries[0];
					String remoteName = entries[1];
					String remoteIP = entries[2];

					if (remoteVersion.equals(String.valueOf(SERVER_VERSION))) {
						if (remoteIP.equals(server.getRemoteSocketAddress().toString())) {
							this.remoteName = remoteName;

							Random rand = new Random();

							String message = "CALLSIGN ";
							for (int i = 0; i < 128; i++) {
								message += rand.nextInt(10);
							}
							out.writeUTF(message);

							String expectedResponse = "" + message.charAt(57) + message.charAt(72) + message.charAt(15) + message.charAt(66) + message.charAt(49);
							String actualResponse = in.readUTF();

							if (actualResponse.equals(expectedResponse)) {
								out.writeUTF("$IDENTIFY");

								boolean verified = false;

								while (!verified) {	
									String identification = in.readUTF();
									if (identification.equals("$ABORT")) {
										break;
									}
									if (identification.startsWith("$IDENTIFY")) {
										username = identification.substring("$IDENTIFY ".length(), identification.length());
										out.writeUTF("$VALID");
										world.getUtil().Log("Sending world data...");
										verified = true;
									}
								}

								if (verified) {	
									boolean done = false;
									boolean firstTick = true;

									while (!done) {
										try {
											if (firstTick) {
												oos.writeObject(new Payload(world.getChunkData()));
												oos.flush();
												world.getUtil().Log("Welcome, " + username + "! Awaiting next connection...");
												firstTick = false;
												ois.readByte();
											}else {
												Packet outgoing = new Packet(world.isGenerated());
												outgoing.setChanges(getChanges());
												oos.writeObject(outgoing);
												oos.flush();
												Packet incoming = (Packet)ois.readObject();
												world.setGenerated(incoming.getState());
												world.applyChanges(incoming.getChanges());
											}
										}catch (Exception e) {
											world.getUtil().Log(username + " disconnected: Error.");
											break;
										}
									}
								}else {
									world.getUtil().Log("Remote Client failed to identify themselves. This is not malicious, they simply failed to log in.");
								}
							}else {
								out.writeUTF("$ERROR, HANDSHAKE");
								world.getUtil().Log("MISMATCH! Client failed to provide proper handshake! Closing the Server!");
							}
						}else {
							out.writeUTF("$ERROR, IP");
							world.getUtil().Log("MISMATCH! Client says their IP is " + remoteIP + " but we see " + server.getRemoteSocketAddress() + "! Closing the Server!");
						}
					}else {
						out.writeUTF("$ERROR, VERSION: " + SERVER_VERSION);
						world.getUtil().Log("MISMATCH! Client is version " + remoteVersion + " but we are version " + SERVER_VERSION + "! Closing the Server!");
					}
				}catch(SocketTimeoutException s) {
					System.out.println(username + " has timed out, and disconnected.");
					break;
				}catch(IOException e) {
					world.getUtil().Log(username + " has disconnected.");
					//e.printStackTrace();
					break;
				}

				try {
					oos.close();
					in.close();
					out.close();
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				active = false;
			}
		}

		public String[] GetParameters(String input) {
			String[] split = input.split(" ");
			String[] output = new String[split.length - 1];
			for (int i = 1; i < split.length; i++) {
				//We dont want the item at index 0, since its a $COMMAND
				output[i - 1] = split[i];
			}
			return output;
		}
	}
}
