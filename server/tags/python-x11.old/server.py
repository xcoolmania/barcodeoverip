#!/usr/bin/python
##
## BarcodeOverIP-server (python) Version 0.1.x Alpha
## Copyright (C) 2012  Tyler H. Jones (me@tylerjones.me)
## http://www.thebasementserver.com/boip
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##	  http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##

###########################################################################
## Server-Client Commands and Command formatting
## 
## Delimiters:
##	% = start of data 
##	; = end of cmd
## Format:
##	 %_AUTH:[USERID]||[AUTHKEY]_CMD:[NAME]:=[ARG];[NAME]:=[ARG];
## BEGIN |  Needed if reqd by cmd  || Cmd#1 name=val || Cmd #2	   | END
##
## 
## Commands:
##  - gimgfile(name) - get the image filename given just the image link name
##  - gimginfo(image) - get all the image info in one string
##  - gimgdata(image) - get the image file data for viewing the image on the client
##  - cimgpass(image, pass) - check the image password NOTE: when pass="",
##   	the function will simply check if the image is password protected, if
##      not protected it will return true as if the password check passed OK.
##      Otherwise, a password will be required to view the image.
##  - uploadimg(imgdata, filename, expires, pass, title, userid) - upload an 
##    	image to the server from the client
##

#########################################################
## Constant variables

VERSION="0.1.1"
APPNAME="BarcodeOverIP-server"

DSEP="||"
DLIM=";"
DDATA="_DATA:" #ex: "%_CMD:getimgfile=HY9cyL;getimginfo=HY9cyL;"
DHASH="_HASH:" #ex: "%_AUTH:userid||authkey_CMD:getimgfile=HY9cyL;"
THANKS = "THANKS" #sent from server to client when server has finished successfully
OK = "\nOK" #Server's response to CHECK command on success


## Imports
import time, shelve, os, sys, string, threading, socket, shutil, hashlib
import config, logger, virtkey

## Startup message/server info
print "\n*******************************************************************************"
print "** " + APPNAME + " " + VERSION + " - BarcodeOverIP server for taget systems"
print "** http://tbsf.me/boip - Written By: Tyler H. Jones, February 2012"
print "** Licensed Under Apache-2.0 License. (C) 2012 - Tyler H. Jones (tylerjones.me) "
print "*******************************************************************************"
print "** Options: --verbose || -v : Be verbose with terminal/log messages"
print "**          --config || -c : Specify config file (Defualt: ./settings.conf)"
print "*******************************************************************************\n"

## Setup logging
verbose = True
log = logger.logger()
log.setup(0, verbose)

#Initialize the virtkey class

## Validate config file
config_response = config.configure()
if config_response != "Error":
	log.info("", "Config file found... Loaded OK!")	
	conf = shelve.open(".config")
	for i,v in config_response.iteritems():
		conf[i] = v
	conf.close()
else:
	sys.exit()

## Make the config dictionary
config = shelve.open(".config")

###############################################################################################
###############################################################################################
## Variable Declarations
###############################################################################################

host = config["BindIP"]
port = int(config["BindPort"])

if(config["Password"].strip() != ""):
	phash = sha.new(config["Password"])
else:
	phash = "NONE"

data_chars = {';':'_%10', '=':'_%12', ':':'_%14', '&':'_%16', '+':'_%17', '@':'_%18', '#':'_%19', ' ':'_%20', ',':'_%21', '\\':'_%22', '(':'_%24', ')':'_%25', '?':'_%30'}

error_codes = {'ERR1':'Invalid data format and/or syntax!', 'ERR2':'No data was sent!', 'ERR3':'Invalid Command Sent!', 'ERR4':'Missing/Empty Command Argument(s) Recvd.', 'ERR5':'Invalid command syntax!', 'ERR6':'Invalid Auth Syntax!', 'ERR7':'Access Denied!', 'ERR8':'Server Timeout, Too Busy to Handle Request!', 'ERR9':'Unknown Data Transmission Error',
		'ERR10':'Auth required.', 'ERR11':'Invalid Password.', 'ERR12':'Not logged in.', 'ERR13':'Incorrect Username/Password!', 'ERR14':'Invalid Login Command Syntax.', 'ERR19':'Unknown Auth Error',
		'ERR20':'Image Not Found!', 'ERR21':'Image Too Large!', 'ERR22':'Invalid Image Type!', 'ERR23':'Not an Image File!', 'ERR24':'Image Upload Failed!'
	}

###############################################################################################
###############################################################################################
## BEGIN - Functions
###############################################################################################

def type_key(key, mask1="", mask2=""):
	v = virtkey.virtkey()
	if mask1 != "" and mask1 in mask_names: 
		v.lock_mod(masks[mask1])
	if mask2 != "" and mask2 in mask_names: 
		v.lock_mod(masks[mask2])

	v.press_unicode(ord(key))
	v.release_unicode(ord(key)) #Release must be called IMMEDIATLEY after 'press'!

	if mask1 != "" and mask1 in mask_names:
		v.unlock_mod(masks[mask1])
	if mask2 != "" and mask2 in mask_names:
		v.unlock_mod(masks[mask2])

def type_keycode(code, mask1="", mask2=""):
	v = virtkey.virtkey()
	if mask1 != "" and mask1 in mask_names: 
		v.lock_mod(masks[mask1])
	if mask2 != "" and mask2 in mask_names: 
		v.lock_mod(masks[mask2])

	v.press_keycode(code)
	v.release_keycode(code) #Release must be called IMMEDIATLEY after 'press'!

	if mask1 != "" and mask1 in mask_names:
		v.unlock_mod(masks[mask1])
	if mask2 != "" and mask2 in mask_names:
		v.unlock_mod(masks[mask2])

def type_string(s):
	char_array = list(s);
	for char in char_array:
		type_key(char);

	
##
## Handle data received by the server
##
def handleConnection(cs):
	Authed = False
	data = cs.recv(4096)
	if not len(data) or str(data).strip() == "":
		log.warning("handleConnection", "No data sent!")
		cs.send("ERR2" + "\n")
		return False
	else:
		if str(data).strip() != "": #TODO: Remove this when nolonger debugging
			print("DEBUG: Received Data: " + str(data).strip())
		data = str(data).strip()
	###############################################
	## Basic server commands
		## CHECK||[PASS_SHA1_HASH] - Check if client has access to the server
		if data.upper().startswith("CHECK") and data.upper().find(DSEP) > 0 and data.upper().endswith(";"):
			check_array = data.upper().split(DSEP)
			chash = check_array[1]
			chash  = chash[:-1] #Remove trailing semi-colon on the last parameter value
			if phash.upper() != "NONE":
				if chash.upper().strip() == phash.upper().strip():
					cs.send(OK)
					log.info("BoIP Client Verification", "BoIP cilent has verified its server settings OK")
				else:
					Authed = False
					log.info("Invalid Password", "Invalid password was sent by the client!")
					cs.send("ERR11" + "\n")
					return False
			else:
				cs.send(OK)
				log.info("BoIP Client Verification", "BoIP cilent has verified its server settings OK")	
					
		if data.upper() == "VERSION": # Get the server version and information
			log.info("Version Info Request", "A client is requesting the server's version information.")
			cs.send("\n***********************************************************\nBarcodeOverIP-server 0.1.1 Alpha\nPowered by Python 3.2 and MySQL\nThis server is for use with remote/mobile applications.\nPlease visit http://tbsf.me/boip for all other uses.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012 - http://tbsf.me/boip\n***********************************************************\n\n")
			return True
		if data.upper().startswith("ERROR") and data.upper().find(" ") >= 0:
			er = data.split(" ")
			if er[0].strip().upper() == "ERROR" and er[1].strip().upper() != "" and len(er[1].strip().upper()) > 0:
				errdesc = "\nError '" + er[1].strip().upper() + "' Meaning: " + error_codes[er[1].strip().upper()] + "\n"
				cs.send(errdesc)
				return True

	###############################################
	## Check data for errors
		if not data.endswith(DLIM):
			log.info("", "Invalid data format and/or syntax! - Does not end with '" + DLIM + "'")
			cs.send("ERR1" + "\n")
			return False
		data = data[:-1]
		arydata = data.split(DSEP);
		if data.find(DSEP) == -1 or len(arydata) < 1:
			log.info("", "Invalid data format and/or syntax! - Cannot find any (or enough) data!")
			cs.send("ERR1" + "\n")
			return False

	#####################################################
	## Parse data while error checking
		if phash.upper() != "NONE":  ## _AUTH: ############################
			print "Parsing Auth Data: " + arydata[0] + "\n"				
			if len(authdata) > 0 and data.startswith(DHASH):
				if phash.lower() == authdata.lower():
					Authed = True
				else:
					Authed = False
					log.info("Invalid Password", "Invalid password was sent by the client!")
					cs.send("ERR11" + "\n")
					return False
			else:
				log.info("Invalid Auth Syntax!", "Invalid auth data was recvd, looks like the auth data isnt at the start of the data string.")
				cs.send("ERR6" + "\n")
				return False
			Authed = True
		log.info("Finished Parsing Data", "Parsed data: '" + arydata[1] + "'")
		log.info("Sending Keyboard Emulation", "Sending keystrokes to system...")
		type_string(arydata[1].strip())
		if config["AppendReturn"]:
			type_keycode(36)
		log.info("Sending Keyboard Emulatio - DONE", "Sending keystrokes to system...FINISHED!")
		log.info("Sending 'THANKS' To Client", "Sending a thank you to the client to inform of successful receipt.")
		cs.send(THANKS)
		return True
		
#####################################################
## Formatting functions

def fFromD(val): #Format from data after receipt and before runnning commands/auth
	return val

def fToD(val):	 #Format to data string for xmit
	return val

###############################################################################################
###############################################################################################
## Start the server
###############################################################################################

#threadListener = Listener() # Define the listener thread
conf = shelve.open(".config")

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((host, port))
s.listen(5)
log.info("Server Socket Created successfully!", "Host/IP: " + host + " -- Port: " + str(port))

try:
	while True:
		clientsock, clientaddr = s.accept()
		log.info("Incoming Connection!", "From: " + str(clientsock.getpeername()))
		#TODO: Use a fork/thread instead of a function call
		handleConnection(clientsock)
		clientsock.close()
except (KeyboardInterrupt, SystemExit): # Wait for a keyboard interupt
	log.info("Keyboard Interupt", "Received keyboard interrupt, quitting threads")
	#threadListener.stop() # Stop the thread
	sys.exit(0)
