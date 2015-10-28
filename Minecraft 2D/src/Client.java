import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {
	private static final int CLIENT_VERSION = 1;
	private boolean authenticated = false;
	private Thread networkClient;
	private NetworkLayer instance;

	private static int SERVER_PORT = 7767;
	private static String SERVER_IP = "127.0.0.1";

	private World world;
	private Util util;
	private UI ui;
	private StartupFrame callback;

	private String password;
	
	private int BLOCK_WIDTH;
	private int BLOCK_HEIGHT;

	public Client(String IP, int port, Util u, String username, String password, StartupFrame callback) {
		SERVER_PORT = port;
		SERVER_IP = IP;
		util = u;

		this.password = password;

		this.callback = callback;

		authenticate(username);
	}

	public void authenticate(String username) {
		if (networkClient != null) {
			networkClient.interrupt();
		}
		instance = new NetworkLayer(username);
		networkClient = new Thread(instance);
		util.Log("Authenticating '" + username + "' with the remote server... (this process may hang)");
		networkClient.start();
	}

	public World getWorld() {
		return world;
	}

	public class NetworkLayer implements Runnable {
		private Socket client;
		private String username;

		private String rank = "user";

		boolean active = true;
		boolean validated = false;

		DataInputStream in;
		DataOutputStream out;

		int QUERY_ID = 0;

		public NetworkLayer(String user) {
			username = user.toLowerCase();
		}

		public String getUsername() {
			return username;
		}

		@Override
		public void run() {
			try {
				client = new Socket(SERVER_IP, SERVER_PORT);

				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream()));
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));

				out = new DataOutputStream(client.getOutputStream());
				in = new DataInputStream(client.getInputStream());

				util.Log("Client initializing on " + client.getLocalAddress() + "@" + client.getLocalPort() + ".");

				util.Log("Preparing to handshake the client at " + client.getRemoteSocketAddress() + ". Transmitting authorization protocol.");

				InetAddress addr;
				addr = InetAddress.getLocalHost();
				out.writeUTF(CLIENT_VERSION + "%" + addr.getHostName() + "%" + client.getLocalAddress() + ":" + client.getLocalPort() + "%");

				String callsign = in.readUTF();

				if (callsign.startsWith("$ERROR")) {
					util.Log("Server returned the network message " + callsign + ".");
					if (callsign.startsWith("$ERROR, VERSION: ")) {
						String remoteSource = callsign.substring("$ERROR, VERSION: ".length(), callsign.length());
					}
				}else {
					String response = "" + callsign.charAt(57) + callsign.charAt(72) + callsign.charAt(15) + callsign.charAt(66) + callsign.charAt(49);

					out.writeUTF(response);

					util.Log("Sending authorization code " + response + ".");

					String ident = in.readUTF();

					if (ident.equals("$IDENTIFY")) {
						String identification = "$IDENTIFY " + username;
						out.writeUTF(identification);
						out.writeUTF("$PASSWORD " + password);

						String ret = in.readUTF();
						if (ret.equals("$VALID")) {
							util.Log("Server sign on completed. Retreiving world data...");
							validated = true;
							authenticated = true;
							boolean firstTick = true;

							ui = new UI(Client.this);
							ui.getGame().setName(username);

							while (true) {
								try {
									if (firstTick) {
										Payload payload = (Payload)ois.readObject();
										world = new World(false, payload.getWorldWidth(), payload.getWorldHeight(), payload.getChunkWidth(), payload.getChunkHeight(), util);
										world.setChunkData(payload.getData());
										ui.getGame().BLOCK_WIDTH = payload.getBlockWidth();
										ui.getGame().BLOCK_HEIGHT = payload.getBlockHeight();
										ui.getGame().player = payload.getPlayer();
										ui.getGame().setViewTarget(ui.getGame().player);
										ui.getGame().initialize();
										firstTick = false;

										util.Log("Welcome to the server!");
										
										oos.writeByte(1);
										oos.flush();
									}
									
									Packet incoming = (Packet)ois.readObject();
									world.setGenerated(incoming.getState());
									world.setEntities(incoming.getEntities());
									world.applyChanges(incoming.getChanges());
									world.setLight(incoming.getWorldLight());
									world.setSkyColor(incoming.getSkyColor());

									Packet outgoing = new Packet(true);
									outgoing.setChanges(ui.getGame().getChanges());
									oos.writeObject(outgoing);
									oos.flush();
								}catch (Exception e) {
									util.Log("Disconnect: Error.");
									e.printStackTrace();
									callback.OnClientCallback("You have been disconnected from the server.");
									break;
								}
							}
						}else if (ret.equals("$INVALID")) {
							out.writeUTF("$ABORT");
						}
					}
				}

				if (!validated) {
					client.close();
					active = false;
				}
			}catch(SocketTimeoutException s) {
				util.Log("The socket has timed out and been reset.");
				callback.OnClientCallback("Connection Timed Out! Did the server go down?");
				active = false;
				s.printStackTrace();
			}catch(ConnectException c) {
				util.Log("Connection Refused.. is the server running?");
				callback.OnClientCallback("Connection Refused! Is the server running?");
				active = false;
			}catch(IOException e) {
				util.Log("The socket has been reset.");
				callback.OnClientCallback("Connection Reset! Did the server go down?");
				active = false;
				e.printStackTrace();
			}
		}
	}
}
