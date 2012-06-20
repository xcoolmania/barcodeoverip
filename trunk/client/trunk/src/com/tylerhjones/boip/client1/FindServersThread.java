/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.1
 * Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
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
 * Filename: DiscoveServersThread.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on June 21, 2012 at 12:22:43 PM
 * 
 * Description: The thread that searches the local network (WiFi) for 
 * available/running BoIP-Server applications
 */

package com.tylerhjones.boip.client1;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.util.Log;


public class FindServersThread extends Thread {
	
	private static final String TAG = "FindServersThread";
	private int Port = Common.DEFAULT_PORT;
	
	// Define sockets and Listeners
	private MulticastSocket socket;
	private DatagramSocket inSocket;
	private FindListener Listener;
	
	// Constructors
	public FindServersThread(FindListener l) {
		this.Listener = l;
	}
	
	public FindServersThread(int p, FindListener l) {
		this.Port = p;
		this.Listener = l;
	}
	
	public void run() {
		try {
			this.socket = new MulticastSocket(this.Port);
			this.socket.joinGroup(InetAddress.getByName(Common.MULTICAST_IPADDR));
			this.inSocket = new DatagramSocket(this.Port + 1);
			this.SendHostChallenge();
			this.WaitForResponse();
		}
		catch (IOException e) {
		}
		catch (InterruptedException e) {
		}
	}
	
	public void closeSocket() {
		this.socket.close();
		this.inSocket.close();
	}
	
	private void SendHostChallenge() throws IOException, InterruptedException {
		byte[] barray = Common.HOST_CHALLENGE.getBytes();
		DatagramPacket packet = new DatagramPacket(barray, barray.length);
		packet.setAddress(InetAddress.getByName(Common.MULTICAST_IPADDR));
		packet.setPort(this.Port);
		this.socket.send(packet);
		Thread.sleep(500);
	}
	
	private void WaitForResponse() throws IOException {
		byte[] barray = new byte[Common.BUFFER_LEN];
		DatagramPacket packet = new DatagramPacket(barray, barray.length);
		Log.d(TAG, "Going to wait for packet");
		while (true) {
			this.inSocket.receive(packet);
			this.handleReceivedPacket(packet);
		}
	}

	private void handleReceivedPacket(DatagramPacket packet) {
		String data = new String(packet.getData());
		Log.d(TAG, "Got packet! data:" + data);
		Log.d(TAG, "IP:" + packet.getAddress().getHostAddress());
		if (data.substring(0, Common.HOST_RESPONSE.length()).compareTo(Common.HOST_RESPONSE) == 0) {
			this.Listener.onAddressReceived(packet.getAddress().getHostAddress());
		}
	}

	
	public static interface FindListener {
		void onAddressReceived(String address);
	}
}
