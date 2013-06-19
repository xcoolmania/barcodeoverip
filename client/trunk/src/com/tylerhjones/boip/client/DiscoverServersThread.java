/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.1.0
 * Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
 * http://boip.tylerjones.me/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Filename: DiscoverServersThread.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on June 21, 2012 at 12:22:43 PM
 * 
 * Description: The thread that searches the local network (WiFi) for 
 * available/running BoIP-Server applications
 */

package com.tylerhjones.boip.client;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.util.Log;


public class DiscoverServersThread extends Thread {
	
	private static final String TAG = "DiscoverServersThread";
	private int Port = 41788 + 32;
	
	// Define sockets and Listeners
	private MulticastSocket socket;
	private DatagramSocket inSocket;
	private FindListener listener;
	
	// Constructors
	public DiscoverServersThread(FindListener listener) {
		this.listener = listener;
	}
	
	public DiscoverServersThread(int p, FindListener listener) {
		li("DiscoverServersThread -- Constructor w/ port. this.Port = ", String.valueOf(p));
		this.Port = p + 131;
		this.listener = listener;
	}
	
	public void run() {
		li("run() -- Start MulticastSocketThread.");
		try {
			this.socket = new MulticastSocket(this.Port);
			this.socket.joinGroup(InetAddress.getByName(Common.MULTICAST_IP));
			this.inSocket = new DatagramSocket(this.Port + 1);
			this.SendHostChallenge();
			this.WaitForResponse();
		}
		catch (IOException e) {
			Log.e(TAG, "run() IOException: " + e.getMessage().toString());
		}
		catch (InterruptedException e) {
			Log.e(TAG, "run() InterruptedException: " + e.getMessage().toString());
		}
	}
	
	public void closeSocket() {
		li("closeSocket() -- Closing sockets.");
		this.socket.close();
		this.inSocket.close();
	}
	
	private void SendHostChallenge() throws IOException, InterruptedException {
		li("SendHostChallenge() -- Send the challenge string to all hosts.");
		byte[] barray = Common.HOST_CHALLENGE.getBytes();
		DatagramPacket packet = new DatagramPacket(barray, barray.length);
		packet.setAddress(InetAddress.getByName(Common.MULTICAST_IP));
		packet.setPort(this.Port);
		this.socket.send(packet);
		li("SendHostChallenge() -- Sent multicast packet containing: ", packet.toString());
		Thread.sleep(500);
	}
	
	private void WaitForResponse() throws IOException {
		li("WaitForResponse() -- Waiting for a host to respond to our challenge string.");
		byte[] barray = new byte[Common.BUFFER_LEN];
		DatagramPacket packet = new DatagramPacket(barray, barray.length);
		while (true) {
			this.inSocket.receive(packet);
			this.handleReceivedPacket(packet);
		}
	}

	private void handleReceivedPacket(DatagramPacket packet) {
		String data = new String(packet.getData());
		li("handleReceivedPacket() -- Got packet data: ", packet.toString());
		li("Server IP: " + packet.getAddress().getHostAddress());
		li("Server Port [ Real | Shown ]: " + packet.getPort() + " | " + String.valueOf(this.Port - 131));
		if (data.substring(0, Common.HOST_RESPONSE.length()).compareTo(Common.HOST_RESPONSE) == 0) {
			li("handleReceivedPacket() -- Server's response OK! Add to list");
			this.listener.onAddressReceived(packet.getAddress().getHostAddress());
		}
	}

	
	public static interface FindListener {
		void onAddressReceived(String address);
	}
	
	public void li(String msg) { // Info message
		Log.v(TAG, msg);
	}
	
	public void li(String msg, String val) { // Info message with one string value passed
		Log.v(TAG, msg + val);
	}
	
	public void lw(String msg) { // Warning message
		Log.w(TAG, msg);
	}
	
	public void lw(String msg, String val) { // Warning message with one string value passed
		Log.w(TAG, msg + val);
	}
}
