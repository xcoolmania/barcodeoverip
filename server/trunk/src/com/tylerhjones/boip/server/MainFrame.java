package com.tylerhjones.boip.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class MainFrame extends JFrame {

	private JPanel contentPane;
    private String TAG = "MainFrame";

    private static final int DEFAULT_PORT = 41788;
    private static final String DEFAULT_IP = "0.0.0.0";
    private static final String OK = "OK";
    public static JarFile jar;
    public static String basePath = "";

    private ServerCore Server = new ServerCore();
    private Settings SET = new Settings();
    private TrayIcon SysTrayIcon;
    private Toolkit toolkit;
    private Thread serverThread = new Thread(Server);
    private MulticastSocketThread discoverable;
    private JTextField textField;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("New label");
		lblTitle.setBounds(6, 6, 438, 52);
		lblTitle.setText("<html>Enter the IP and Port given below into your BarcodeOverIP Client app to scan and send barcodes to this computer. It's that easy!</html>");
		contentPane.add(lblTitle);
		
		JLabel lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(16, 70, 76, 28);
		contentPane.add(lblIpAddress);
		
		textField = new JTextField();
		textField.setBounds(104, 70, 157, 28);
		contentPane.add(textField);
		textField.setColumns(10);
		setResizable(false);
	}
	
    public void init() {
        serverThread.start();

        discoverable = new MulticastSocketThread(SET.getPort());
        discoverable.start();
        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;

        // Move the window
        this.setLocation(x, y);

        txtPassword.setText(SET.getPass());
        chkAppendNL.setSelected(Boolean.valueOf(SET.getAppendNL()));
        chkAutoSet.setSelected(Boolean.valueOf(SET.getAutoSet()));

        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override 
            
            public void changedUpdate(DocumentEvent e) {
                warn();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

             public void warn() {
                SET.setPass(txtPassword.getText());
            }
        });

        if(SET.getAutoSet()) {
            String ip = this.FindSystemIP();
            if(ip.equals(DEFAULT_IP) || ip.startsWith("127") || ip.equals("")) {
                JOptionPane.showMessageDialog(this.getParent(), "The IP address of your system could not be auto-discovered. Check the network connection and try again or set the IP and port manually.", "Cannot Auto-discover Local IP Address", JOptionPane.WARNING_MESSAGE);
                chkAutoSet.setSelected(false);
                SET.setAutoSet(false);
                txtHost.setEditable(true);
                txtPort.setEditable(true);
                txtHost.setFocusable(true);
                txtHost.requestFocusInWindow();
                txtHost.selectAll();
                btnApplyIPPort.setEnabled(true);
            } else {
                chkAutoSet.setSelected(true);
                SET.setAutoSet(true);
                txtHost.setEditable(false);
                txtPort.setEditable(false);
                btnApplyIPPort.setEnabled(false);
                SET.setHost(ip);
                SET.setPort(DEFAULT_PORT);
                txtHost.setText(ip);
                txtPort.setText(String.valueOf(DEFAULT_PORT));
            }
        } else {
            txtHost.setText(SET.getHost());
            txtPort.setText(String.valueOf(SET.getPort()));
            txtHost.setEditable(true);
            txtPort.setEditable(true);
            txtHost.setFocusable(true);
            txtHost.requestFocusInWindow();
            txtHost.selectAll();
            btnApplyIPPort.setEnabled(true);
        }
        Server.activate();
    }

    public void setTrayIcon(TrayIcon ico) {
        this.SysTrayIcon = ico;
        this.SysTrayIcon.setToolTip("BarcodeOverIP " + SET.VERSION + " - Active\n(Right or Left click to show settings window)\nHost:Port - " + SET.getHost() + ":" + String.valueOf(SET.getPort()));
    }
    
    
    
    // Auto-discover IP for Java 6
    /*
     
    public static InetAddress getLocalHost_nix() throws UnknownHostException {
            InetAddress localHost = InetAddress.getLocalHost();
            if(!localHost.isLoopbackAddress()) return localHost;
            InetAddress[] addrs = getAllLocalUsingNetworkInterface_nix();
            for(int i=0; i<addrs.length; i++) {
                    //Check for "." to ensure IPv4
                    if(!addrs[i].isLoopbackAddress() && addrs[i].getHostAddress().contains(".")) return addrs[i];
            }
            return localHost;
    }

    public static InetAddress[] getAllLocal_nix() throws UnknownHostException {
            InetAddress[] iAddresses = InetAddress.getAllByName("127.0.0.1");
            if(iAddresses.length != 1) return iAddresses;
            if(!iAddresses[0].isLoopbackAddress()) return iAddresses;
            return getAllLocalUsingNetworkInterface_nix();
    }

    private static InetAddress[] getAllLocalUsingNetworkInterface_nix() throws UnknownHostException {
            ArrayList addresses = new ArrayList();
            Enumeration e = null;
            try {
                    e = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException ex) {
                    throw new UnknownHostException("127.0.0.1");
            }
            while(e.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface)e.nextElement();
                    for(Enumeration e2 = ni.getInetAddresses(); e2.hasMoreElements();) {
                            addresses.add(e2.nextElement());
                    }
            }
            InetAddress[] iAddresses = new InetAddress[addresses.size()];
            for(int i=0; i<iAddresses.length; i++) {
                    iAddresses[i] = (InetAddress)addresses.get(i);
            }
            return iAddresses;
    }
     */
    
    
    private String FindSystemIP() {
	String ip = DEFAULT_IP;
	try {
	    ip = getIPv4InetAddress().getHostAddress();
	} catch (SocketException se) {
	    System.out.println("SocketException when looking up local system IP address! -- MESSAGE: " + se.toString());
	} catch (UnknownHostException unk) {
	    System.out.println("UnknownHostException when looking up local system IP address! -- MESSAGE: " + unk.toString());
	}
	
	/*
	 * For Java 6
	 * 
	try {
            localAddr = InetAddress.getLocalHost();
            if (localAddr.isLoopbackAddress()) {
                localAddr = getLocalHost_nix();
            }
            ip = localAddr.getHostAddress();
        } catch (UnknownHostException ex) {
            Server.pln("Error finding local IP.");
            return NO;
        }
	 
	 */
	return ip;
    }

    private InetAddress getIPv4InetAddress() throws SocketException, UnknownHostException {
	    String os = System.getProperty("os.name").toLowerCase();
	    try {
	    if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {   
	        NetworkInterface ni = NetworkInterface.getByName("eth0");
	        Enumeration<InetAddress> ias = ni.getInetAddresses();
	        InetAddress iaddress;
	        do {
	            iaddress = ias.nextElement();
	        } while(!(iaddress instanceof Inet4Address));
	        return iaddress;
	    }
	    } catch (NullPointerException e) {
	    	return InetAddress.getLocalHost(); 
	    }
	    return InetAddress.getLocalHost();  
    }
 
    
    
    
    
    private String validateValues() {
        if(!chkAutoSet.isSelected()) {
            if(txtHost.getText().trim().length() < 1 || txtHost.getText().trim().equals("") || txtHost.getText().trim() == null) {
                return "Invalid or empty Host/IP Address!";
            }
            if(txtPort.getText().trim().length() < 1 || txtPort.getText().trim().equals("") || txtPort.getText().trim() == null) {
                txtPort.setText("41788");
            } else {
                boolean validport;
                try {
                    int i = Integer.parseInt(txtPort.getText());
                    if(i < 65535 && i > 1023) {
                        validport = true;
                    } else {
                        return "Given port is out of range. Must be: <65535 and >1023!";
                    }
                } catch(NumberFormatException nme) {
                    validport = false;
                }
                if(!validport) { return "Given port value is not a valid number!"; }
            }
        }
        if(txtPassword.getText().trim().toUpperCase().equals("CHECK") || txtPassword.getText().trim().toUpperCase().equals("VERSION")) { 
            return "Given password conflicts with the syntax of the client-server comm protocol. Please pick another password!"; }
        if(txtPassword.getText().trim().length() < 4 && !txtPassword.getText().trim().equals("")) { return "Given password is too short! Must be > 4 characters long!"; }
        if(txtPassword.getText().trim().length() > 32) { return "Given password is too long! Must be < 32 characters long!"; }
        return OK; //All is ok
    }

    private boolean ApplyIPPort() {
        String validres = validateValues();
        if(!validres.equals(OK)) {
            JOptionPane.showMessageDialog(this, validres, "Invalid Value!", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if(!chkAutoSet.isSelected()) {
            SET.setHost(txtHost.getText().trim());
            SET.setPort(Integer.valueOf(txtPort.getText().trim()));
        } else {
            txtPort.setText(String.valueOf(DEFAULT_PORT));
            txtHost.setText(this.FindSystemIP());
            SET.setHost(txtHost.getText().trim());
            SET.setPort(Integer.valueOf(txtPort.getText().trim()));
        }
        LogI(TAG, "Changes successfully saved!");

        txtHost.setText(SET.getHost());
        txtPort.setText(String.valueOf(SET.getPort()));

        int n = JOptionPane.showConfirmDialog(this, "The changes to the IP and/or Port settings will not take effect until BoIP-Server is restarted. Would you like to EXIT now?", "App Restart Required", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            Server.stopListener();
            dispose();
            System.exit(0);
        }
        
        return true;
    }

    public void LogD(String tag, String info) { Log(tag, info, 0); }
    public void LogI(String tag, String info) { Log(tag, info, 1); }
    public void LogW(String tag, String info) { Log(tag, info, 2); }
    public void LogE(String tag, String info) { Log(tag, info, 3); }
    public void LogF(String tag, String info) { Log(tag, info, 4); }
    public void Log(String tag, String info, int level) { //Levels: 0 = debug, 1 = info, 2 = warning, 3 = error, 4 = fatal
        String a = "";
        if(level == 2) { a = "WARN: "; }
        if(level == 3) { a = "*ERR*: "; }
        if(level == 4) { a = "**FATAL**: "; }
        System.out.println(TAG + a + "" + info);
    }

    public Image getImage(String sImage) {
        Image imgReturn = this.toolkit.createImage(this.getClass().getClassLoader().getResource(sImage));
        return imgReturn;
    }
}
