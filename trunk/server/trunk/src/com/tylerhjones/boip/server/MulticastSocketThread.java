/*
 *
 *  BarcodeOverIP-Server (Java) Version 1.1.x
 *  Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
 *  http://boip.tylerjones.me
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Filename: MulticastSocketThread.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on June 22, 2012 9:50:26 AM
 *
 *  Description: This class interacts via threads and sockets with the
 *   BoIP-Client app to discover and list all available BoIP-Servers on the
 *   network.
 *
 */

package com.tylerhjones.boip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MulticastSocketThread extends Thread {

        private static int BUFFER_LEN = 1024;
	public static String MULTICAST_IP = "231.0.2.46";
        private static final String HOST_CHALLENGE = "BoIP:NarwhalBaconTime";
        private static final String HOST_RESPONSE = "BoIP:Midnight";

        //private Settings SET = new Settings();

	private int Port = 8714;
	private MulticastSocket socket;

	public MulticastSocketThread() {
                  //this.Port = SET.getPort() + 131;
	}

	public MulticastSocketThread(int port) {
		//this.Port = port + 131;
	}

	public MulticastSocketThread(Runnable target) {
		super(target);
                //this.Port = SET.getPort() + 131;
                
	}

	public MulticastSocketThread(String name) {
		super(name);
                //this.Port = SET.getPort() + 131;
		
	}

	public MulticastSocketThread(ThreadGroup group, Runnable target) {
		super(group, target);
                //this.Port = SET.getPort() + 131;
		
	}

	public MulticastSocketThread(ThreadGroup group, String name) {
		super(group, name);
                //this.Port = SET.getPort() + 131;
		
	}

	public MulticastSocketThread(Runnable target, String name) {
		super(target, name);
                //this.Port = SET.getPort() + 131;
		
	}

	public MulticastSocketThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
                //this.Port = SET.getPort() + 131;
		
	}

	public MulticastSocketThread(ThreadGroup group, Runnable target, String name,
			long stackSize) {
		super(group, target, name, stackSize);
                //this.Port = SET.getPort() + 131;
		
	}

	//

    @Override
	public void run() {
		try {
			byte[] b = new byte[BUFFER_LEN];
			DatagramPacket packet = new DatagramPacket(b, b.length);
			this.socket = new MulticastSocket(this.Port);
			this.socket.joinGroup(InetAddress.getByName(MULTICAST_IP));
                        System.out.println("MST -- run(): Listen IP: " + MULTICAST_IP);
                        System.out.println("MST -- run(): Listen Port: " + String.valueOf(this.Port));
                        while (true) {
				this.socket.receive(packet);
				this.PacketHandler(packet);
			}
		} catch (IOException e) {

		} catch (InterruptedException e) {

		}
	}

	private void PacketHandler(DatagramPacket packet) throws IOException, InterruptedException {
		String data = new String(packet.getData());
		System.out.println("MST -- PacketHandler(packet): Got data packet: " + data);
		if (data.substring(0, HOST_CHALLENGE.length()).equals(HOST_CHALLENGE)) {
			System.out.println("MST -- Client Request challenge OK!");
			// we'll send a response!
			byte[] b = HOST_RESPONSE.getBytes();
			DatagramPacket p = new DatagramPacket(b, b.length);
			p.setAddress(packet.getAddress());
			p.setPort(this.Port+1);
			// wait half a second just in case.
			Thread.sleep(500);
			//datagramSocket outSocket = new DatagramSocket();
			//outSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			//outSocket.send(p);
		}
	}

}
