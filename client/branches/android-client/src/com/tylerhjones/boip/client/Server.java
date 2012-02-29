/*
 * 
 * BarcodeOverIP Client (Android < v3.2) Version 0.3.1 Beta
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
	private String Name;
	private String Host;
	private String Pass; // NOTE: When this value is set into this class, it MUST and WILL always be hashed!
	private int Index; // For database lookups, probably never get used
	private int Port;
	
	// Default server class constructor
	public Server() {
		this.Name = "New Server";
		this.Host = Common.NET_HOST;
		this.Port = Common.NET_PORT;
		this.Pass = "";
		this.Index = 1;
	}
	
	// Detailed server class constructor
	public Server(String Name, String Host, String Pass, int Port, int Index) {
		this.Name = Name;
		this.Host = Host;
		this.Port = Port;
		this.Pass = Pass;
		this.Index = Index;
	}

	public String getName() {
		return this.Name;
	}
	
	public void setName(String Name) {
		this.Name = Name;
	}
	
	public int getPort() {
		return this.Port;
	}
	
	public void setPort(int Port) {
		this.Port = Port;
	}
	
	public int getIndex() {
		return this.Index;
	}
	
	public void setIndex(int Index) {
		this.Index = Index;
	}

	public String getHost() {
		return this.Host;
	}
	
	public void setHost(String Host) {
		this.Host = Host;
	}
	
	public String getPassword() {
		return this.Pass;
	}
	
	public void setPassword(String Pass) {
		this.Pass = Pass;
	}

}
