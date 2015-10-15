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


public class Client {
	private static final int CLIENT_VERSION = 1;
	private boolean authenticated = false;
	private Thread networkClient;
	private NetworkLayer instance;

	private static int SERVER_PORT = 7767;
	private static String SERVER_IP = "";

	World world;

	public Client(World w) {
		world = w;
	}

	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a");

	void Log(String message) {
		Date date = new Date();
		String sender = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
		String time = timeFormatter.format(date);

		String log = "[" + sender + "@" + time +"]: " + message;

		System.out.println(log);
	}

	private static final int CLIENT_CODE_STACK_INDEX;
	static {
		int i = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			i++;
			if (ste.getClassName().equals(Client.class.getName())) {
				break;
			}
		}
		CLIENT_CODE_STACK_INDEX = i;
	}


	public void authenticate(String username) {
		if (networkClient != null) {
			networkClient.interrupt();
		}
		instance = new NetworkLayer(username);
		networkClient = new Thread(instance);
		Log("Authenticating '" + username + "' with the remote server... (this process may hang)");
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
				client = new Socket("127.0.0.1", 1337);

				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

				out = new DataOutputStream(client.getOutputStream());
				in = new DataInputStream(client.getInputStream());

				Log("Client initializing on " + client.getLocalAddress() + "@" + client.getLocalPort() + ".");

				Log("Preparing to handshake the client at " + client.getRemoteSocketAddress() + ". Transmitting authorization protocol.");

				InetAddress addr;
				addr = InetAddress.getLocalHost();
				out.writeUTF(CLIENT_VERSION + "%" + addr.getHostName() + "%" + client.getLocalAddress() + ":" + client.getLocalPort() + "%");

				String callsign = in.readUTF();

				if (callsign.startsWith("$ERROR")) {
					Log("Server returned the network message " + callsign + ".");
					if (callsign.startsWith("$ERROR, VERSION: ")) {
						String remoteSource = callsign.substring("$ERROR, VERSION: ".length(), callsign.length());
					}
				}else {
					String response = "" + callsign.charAt(57) + callsign.charAt(72) + callsign.charAt(15) + callsign.charAt(66) + callsign.charAt(49);

					out.writeUTF(response);

					Log("Sending authorization code " + response + ".");

					String ident = in.readUTF();

					if (ident.equals("$IDENTIFY")) {
						String identification = "$IDENTIFY " + username;
						out.writeUTF(identification);

						String ret = in.readUTF();
						if (ret.equals("$VALID")) {
							Log("Server says we're good to go. Awaiting serialization index list...");
							validated = true;
							authenticated = true;

							while (true) {
								try {
									world.setChunkData((Chunk[][])ois.readObject());
									oos.writeObject(world.getChunkData());
								}catch (Exception e) {
									world.getUtil().Log("Forcing premature termination of the thread.");
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
				Log("The socket has timed out and been reset.");
				active = false;
				s.printStackTrace();
			}catch(ConnectException c) {
				Log("Connection Refused.. is the server running?");
				active = false;
			}catch(IOException e) {
				Log("The socket has been reset.");
				active = false;
				e.printStackTrace();
			}
		}
	}
}
