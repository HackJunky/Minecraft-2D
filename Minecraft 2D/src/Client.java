import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;


public class Client {
	private static final int CLIENT_VERSION = 1;
	private boolean authenticated = false;
	private Thread networkClient;
	private NetworkLayer instance;

	private static int SERVER_PORT = 7767;
	private static String SERVER_IP = "127.0.0.1";

	private World world;
	private Game game;

	public Client(World w, Game g) {
		world = w;
		game = g;

		String inputValue = null;
		while (inputValue == null || inputValue.length() < 4) {
			inputValue = JOptionPane.showInputDialog("Please enter a username: ");
		}
		authenticate(inputValue);
	}


	public void authenticate(String username) {
		if (networkClient != null) {
			networkClient.interrupt();
		}
		instance = new NetworkLayer(username);
		networkClient = new Thread(instance);
		world.getUtil().Log("Authenticating '" + username + "' with the remote server... (this process may hang)");
		networkClient.start();
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

				world.getUtil().Log("Client initializing on " + client.getLocalAddress() + "@" + client.getLocalPort() + ".");

				world.getUtil().Log("Preparing to handshake the client at " + client.getRemoteSocketAddress() + ". Transmitting authorization protocol.");

				InetAddress addr;
				addr = InetAddress.getLocalHost();
				out.writeUTF(CLIENT_VERSION + "%" + addr.getHostName() + "%" + client.getLocalAddress() + ":" + client.getLocalPort() + "%");

				String callsign = in.readUTF();

				if (callsign.startsWith("$ERROR")) {
					world.getUtil().Log("Server returned the network message " + callsign + ".");
					if (callsign.startsWith("$ERROR, VERSION: ")) {
						String remoteSource = callsign.substring("$ERROR, VERSION: ".length(), callsign.length());
					}
				}else {
					String response = "" + callsign.charAt(57) + callsign.charAt(72) + callsign.charAt(15) + callsign.charAt(66) + callsign.charAt(49);

					out.writeUTF(response);

					world.getUtil().Log("Sending authorization code " + response + ".");

					String ident = in.readUTF();

					if (ident.equals("$IDENTIFY")) {
						String identification = "$IDENTIFY " + username;
						out.writeUTF(identification);

						String ret = in.readUTF();
						if (ret.equals("$VALID")) {
							world.getUtil().Log("Server sign on completed. Retreiving world data...");
							validated = true;
							authenticated = true;
							boolean firstTick = true;

							while (true) {
								try {
									if (firstTick) {
										world.setChunkData(((Payload)ois.readObject()).getData());
										world.getUtil().Log("Welcome to the server!");
										game.initialize();
										firstTick = false;
										oos.writeByte(0);
									}else {
										Packet incoming = (Packet)ois.readObject();
										world.setGenerated(incoming.getState());
										world.setEntities(incoming.getEntities());
										world.applyChanges(incoming.getChanges());
										Packet outgoing = new Packet(incoming.getState());
										outgoing.setChanges(game.getChanges());
										oos.writeObject(outgoing);
										oos.flush();
									}
								}catch (Exception e) {
									world.getUtil().Log("Disconnect: Error.");
									e.printStackTrace();
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
				world.getUtil().Log("The socket has timed out and been reset.");
				active = false;
				s.printStackTrace();
			}catch(ConnectException c) {
				world.getUtil().Log("Connection Refused.. is the server running?");
				active = false;
			}catch(IOException e) {
				world.getUtil().Log("The socket has been reset.");
				active = false;
				e.printStackTrace();
			}
		}
	}

}
