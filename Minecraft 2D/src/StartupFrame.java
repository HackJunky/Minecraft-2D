import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JProgressBar;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import java.awt.Button;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


public class StartupFrame extends JFrame {
	private static final long serialVersionUID = -2794438895707041418L;

	String[] usernamePrefix = {"Red", "Orange", "Green", "Blue", "Yellow", "Purple"};
	String[] usernameSuffix = {"Cat", "Dog", "Wolf", "Rocket", "Sheep", "Goat"};
	private JTextField txtAlias;
	private JTextField txtServerIP;
	private JPasswordField txtServerPassword;
	private JTextField txtMaxPlayers;
	private JTextField txtChunkWidth;
	private JTextField txtChunkHeight;
	private JTextField txtWorldWidth;
	private JTextField txtWorldHeight;

	private JToggleButton btnAutoJoin;
	
	private JButton btnConnect;

	private boolean callback = false;

	private Util util;
	private JTextField textField;
	private JTextField txtPort;
	private JTextField txtPortServer;

	public static void main(String[] args) {
		new StartupFrame();
	}

	public StartupFrame() {
		util = new Util();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("data/MCFont.ttf")));
			util.Log("MCFont loaded to database of " + ge.getAllFonts().length + " font(s).");
		} catch (Exception e) {
			util.Log("Could not locate MCFont file in the 'data' directory!");
		}

		setResizable(false);
		setType(Type.POPUP);
		setTitle("Minecraft 2D Launcher");

		this.validate();
		this.repaint();

		this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 320, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 240);
		this.setSize(500, 280);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[][4px:n:4px][grow]"));

		JLabel lblWelcomeToMinecraft = new JLabel("Welcome to Minecraft 2D, configure the options below to begin!");
		getContentPane().add(lblWelcomeToMinecraft, "cell 0 0,alignx center");

		JPanel panel = new JPanel();
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(panel, "cell 0 2,grow");
		panel.setLayout(new MigLayout("", "[grow][][][]", "[][grow]"));

		JLabel lblEnterAnAlias = new JLabel("Enter an Alias:");
		panel.add(lblEnterAnAlias, "flowx,cell 0 0,aligny center");

		JToggleButton tglConnect = new JToggleButton("Connect to a Server");
		JToggleButton tglHost = new JToggleButton("Host a Server");
		tglHost.setSelected(true);

		panel.add(tglHost, "cell 2 0");
		panel.add(tglConnect, "cell 3 0");

		JPanel pnlConnect = new JPanel();
		pnlConnect.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPanel pnlHost = new JPanel();
		pnlHost.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		pnlHost.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][][][grow][]"));

		JLabel lblMaxPlayers = new JLabel("Max Players (1-64):");
		pnlHost.add(lblMaxPlayers, "cell 0 0,alignx trailing");

		txtMaxPlayers = new JTextField();
		txtMaxPlayers.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}
			public void removeUpdate(DocumentEvent e) {
				warn();
			}
			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				if (txtMaxPlayers.getText().length() == 0 || Integer.parseInt(txtMaxPlayers.getText()) < 1) {
					txtMaxPlayers.setBackground(Color.RED);
				}else {
					txtMaxPlayers.setBackground(Color.WHITE);
				}
			}
		});
		txtMaxPlayers.setText("16");
		pnlHost.add(txtMaxPlayers, "flowx,cell 1 0,growx");
		txtMaxPlayers.setColumns(10);

		btnAutoJoin = new JToggleButton("Auto-Join Server");
		pnlHost.add(btnAutoJoin, "cell 2 0 2 1,alignx right");

		JLabel lblWorldChunks = new JLabel("Chunk Dimensions (W x H):");
		pnlHost.add(lblWorldChunks, "cell 0 2,alignx trailing");

		txtChunkWidth = new JTextField();
		txtChunkWidth.setText("64");
		pnlHost.add(txtChunkWidth, "flowx,cell 1 2,growx");
		txtChunkWidth.setColumns(10);

		JLabel lblChunkHeight = new JLabel("World Chunk Size (W x H):");
		pnlHost.add(lblChunkHeight, "cell 0 3,alignx trailing");

		JLabel lblX = new JLabel("x");
		pnlHost.add(lblX, "cell 1 2");

		txtChunkHeight = new JTextField();
		txtChunkHeight.setText("64");
		pnlHost.add(txtChunkHeight, "cell 1 2,growx");
		txtChunkHeight.setColumns(10);

		txtWorldWidth = new JTextField();
		txtWorldWidth.setText("100");
		pnlHost.add(txtWorldWidth, "flowx,cell 1 3,growx");
		txtWorldWidth.setColumns(10);

		JLabel lblX_1 = new JLabel("x");
		pnlHost.add(lblX_1, "cell 1 3");

		txtWorldHeight = new JTextField();
		txtWorldHeight.setText("4");
		pnlHost.add(txtWorldHeight, "cell 1 3");
		txtWorldHeight.setColumns(10);
		
		JLabel lblPort = new JLabel("Port:");
		pnlHost.add(lblPort, "cell 0 4,alignx trailing");
		
		txtPortServer = new JTextField();
		txtPortServer.setText("7767");
		pnlHost.add(txtPortServer, "flowx,cell 1 4,growx");
		txtPortServer.setColumns(10);

		JToggleButton btnShowConsole = new JToggleButton("Show Server Console");
		pnlHost.add(btnShowConsole, "cell 1 6 2 1,alignx right");

		JButton btnLaunchServer = new JButton("Launch Server");
		btnLaunchServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Server(util, StartupFrame.this,
						Integer.parseInt(txtPortServer.getText()),
						Integer.parseInt(txtMaxPlayers.getText()),
						Integer.parseInt(txtChunkWidth.getText()),
						Integer.parseInt(txtChunkHeight.getText()),
						Integer.parseInt(txtWorldWidth.getText()),
						Integer.parseInt(txtWorldHeight.getText()));
				if (btnShowConsole.isSelected()) {
					new Console(util);
				}
				StartupFrame.this.setVisible(false);
			}
		});
		pnlHost.add(btnLaunchServer, "cell 3 6,growx");

		Component horizontalStrut = Box.createHorizontalStrut(128);
		pnlHost.add(horizontalStrut, "cell 1 0");

		txtAlias = new JTextField();
		txtAlias.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}
			public void removeUpdate(DocumentEvent e) {
				warn();
			}
			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				if (txtAlias.getText().length() < 5) {
					txtAlias.setBackground(Color.RED);
				}else {
					txtAlias.setBackground(Color.WHITE);
				}
			}
		});
		txtAlias.setText(usernamePrefix[new Random().nextInt(usernamePrefix.length)] + usernameSuffix[new Random().nextInt(usernamePrefix.length)] + new Random().nextInt(9999));

		panel.add(txtAlias, "cell 0 0 2 1,growx,aligny center");
		txtAlias.setColumns(10);
		pnlConnect.setLayout(new MigLayout("", "[][128px:n:128px,grow][grow]", "[][grow][][][]"));

		panel.add(pnlHost, "cell 0 1 4 1,grow");
		//panel.add(pnlConnect, "cell 0 1 4 1,grow");
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(96);
		pnlHost.add(horizontalStrut_3, "cell 1 4");
		

		JLabel lblGuiScale = new JLabel("GUI Scale (2 - 64):");
		pnlConnect.add(lblGuiScale, "cell 0 0,alignx trailing");

		textField = new JTextField();
		textField.setText("16");
		pnlConnect.add(textField, "flowx,cell 1 0,growx");
		textField.setColumns(10);

		JRadioButton rdbtnLoadInventoryFrom = new JRadioButton("Load Inventory from Server");
		rdbtnLoadInventoryFrom.setSelected(true);
		pnlConnect.add(rdbtnLoadInventoryFrom, "cell 2 0");

		JRadioButton rdbtnNewRadioButton = new JRadioButton("Spectate Only");
		pnlConnect.add(rdbtnNewRadioButton, "cell 2 1,alignx left,aligny top");
		
				JLabel lblServerIp = new JLabel("Server IP:");
				pnlConnect.add(lblServerIp, "cell 0 2,alignx trailing");
		
				txtServerIP = new JTextField();
				txtServerIP.setText("127.0.0.1");
				pnlConnect.add(txtServerIP, "cell 1 2,growx");
				txtServerIP.setColumns(10);
		
		JLabel lblServerPort = new JLabel("Server Port:");
		pnlConnect.add(lblServerPort, "flowx,cell 0 3,alignx trailing");
		
		txtPort = new JTextField();
		txtPort.setText("7767");
		pnlConnect.add(txtPort, "flowx,cell 1 3,growx");
		txtPort.setColumns(10);

		JLabel lblServerPassword = new JLabel("Server Password:");
		pnlConnect.add(lblServerPassword, "cell 0 4,alignx trailing");

		txtServerPassword = new JPasswordField();
		pnlConnect.add(txtServerPassword, "cell 1 4,growx");

		btnConnect = new JButton("Connect to Server");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnect.setEnabled(false);
				new Client(txtServerIP.getText(), Integer.parseInt(txtPort.getText()), util, txtAlias.getText(), StartupFrame.this);
			}
		});
		pnlConnect.add(btnConnect, "cell 2 4,alignx right");

		Component horizontalStrut_1 = Box.createHorizontalStrut(48);
		pnlConnect.add(horizontalStrut_1, "cell 1 0");
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(64);
		pnlConnect.add(horizontalStrut_2, "cell 1 3");

		tglHost.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					panel.remove(pnlConnect);
					panel.add(pnlHost, "cell 0 1 4 1,grow");
					tglConnect.setSelected(false);
				}else {
					tglConnect.setSelected(true);
				}
				StartupFrame.this.repaint();
				StartupFrame.this.validate();
			}
		});
		tglConnect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					panel.remove(pnlHost);
					panel.add(pnlConnect, "cell 0 1 4 1,grow");
					tglHost.setSelected(false);
				}else {
					tglHost.setSelected(true);
				}
				StartupFrame.this.repaint();
				StartupFrame.this.validate();
			}
		});

		this.setVisible(true);
	}

	public void OnServerCallback() {
		if (!callback) {
			if (btnAutoJoin.isSelected()) {
				new Client("127.0.0.1", Integer.parseInt(txtPortServer.getText()), util, txtAlias.getText(), this);
			}
			callback = true;
		}
	}
	
	public void OnClientCallback(String error) {
		this.setVisible(true);
		JOptionPane.showMessageDialog(this, error);
		btnConnect.setEnabled(true);
	}
}
