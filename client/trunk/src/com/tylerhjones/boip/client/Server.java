/*
 * 
 * BarcodeOverIP (Android < v3.2) Version 0.9.3
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
 * Filename: Server.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 28, 2012 at 5:14:11 PM
 * 
 * Description: Storage class for server settings
 */


package com.tylerhjones.boip.client;



public class Server {
	
	// Private class property variables
	private String Name = Common.DEFAULT_NAME;
	private String Host = Common.DEFAULT_HOST;
	private String Pass = Common.DEFAULT_PASS; // NOTE: When this value is set into this class, it MUST and WILL always be hashed!
	private int Port = Common.DEFAULT_PORT;
	private int Index = 0; // For database lookups, will probably never get used

	// Default server class constructor
	public Server() {
		this.Name = Common.DEFAULT_NAME;
		this.Host = Common.DEFAULT_HOST;
		this.Port = Common.DEFAULT_PORT;
		this.Pass = Common.DEFAULT_PASS;
		this.Index = 0;
	}
	
	// Detailed server class constructor
	public Server(String Name, String Host, String Pass, int Port, int Index) {
		this.Name = Name;
		this.Host = Host;
		this.Port = Port;
		this.Pass = Pass;
		this.Index = Index;
	}

	// Detailed server class constructor
	public Server(String Name, String Host, String Pass, int Port) {
		this.Name = Name;
		this.Host = Host;
		this.Port = Port;
		this.Pass = Pass;
		this.Index = 0;
	}

	/** Server name properties ************************************ */
	public String getName() {
		return this.Name;
	}
	
	public void setName(String Name) {
		this.Name = Name;
	}
	
	/** Server port properties ************************************ */
	public int getPort() {
		return this.Port;
	}
	
	public void setPort(int Port) {
		try {
			Common.isValidPort(String.valueOf(Port));
		}
		catch (Exception e) {
			this.Port = Common.DEFAULT_PORT;
			return;
		}
		this.Port = Port;
	}
	
	/** Server index properties ************************************ */
	public int getIndex() {
		return this.Index;
	}
	
	public void setIndex(int Index) {
		this.Index = Index;
	}

	/** Server host properties ************************************ */
	public String getHost() {
		return this.Host;
	}
	
	public void setHost(String Host) {
		this.Host = Host;
	}
	
	/** Server password properties ************************************ */
	public String getPassword() {
		return this.Pass;
	}
	
	public void setPassword(String Pass) {
		if (Pass.trim().equals("") || Pass == null) {
			this.Pass = "none";
		} else {
			this.Pass = Pass;
		}
	}

	public String getPass() { // Just a copy of setPassword, this makes for easier coding when I always want to type 'getPass'
		return this.getPassword();
	}
	
	public void setPass(String Pass) { // Just a copy of setPassword, this makes for easier coding when I always want to type 'getPass'
		this.setPassword(Pass);
	}

	public String getPassHash() { // Get the SHA1 hash of the server password
		try {
			if (this.Pass.trim().equals("none") || this.Pass.trim().equals("")) {
				return Common.DEFAULT_PASS;
			} else {
				return Common.SHA1(this.Pass);
			}
		}
		catch (Exception e) {
			return Common.DEFAULT_PASS;
		}
	}
}
