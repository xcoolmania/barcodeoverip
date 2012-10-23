#!/usr/bin/python

##
## BarcodeOverIP-server (python) Version 0.2.x Beta
## Copyright (C) 2012  Tyler H. Jones (me@tylerjones.me)
## https://code.google.com/p/barcodeoverip/
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##        http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##

## Imports
import time, os, sys, string, socket, hashlib
import win32com.client, win32api
from SendKeys import SendKeys
#import threading, base64
import logger


#########################################################
## Constant variables



VERSION="0.2.1"
APPNAME="BarcodeOverIP-server-python_win32"
#APPNAME="BarcodeOverIP-server-python_all"


DSEP="||"
DLIM=";"
DDATA="_DATA:" #ex: "%_CMD:getimgfile=HY9cyL;getimginfo=HY9cyL;"
DHASH="_HASH:" #ex: "%_AUTH:userid||authkey_CMD:getimgfile=HY9cyL;"
THANKS = "THANKS\n" #sent from server to client when server has finished successfully
OK = "OK\n" #Server's response to CHECK command on success

## Startup message/server info
print "\n*******************************************************************************"
print "** " + APPNAME + " " + VERSION + " - BarcodeOverIP-server for Win32 "
print "** Website: https://code.google.com/p/barcodeoverip/"
print "** Written By: Tyler H. Jones, February 2012"
print "** Licensed Under Apache-2.0 License. (C) 2012 - Tyler H. Jones (tylerjones.me) "
print "*******************************************************************************"
#print "** Options: --verbose || -v : Be verbose with terminal/log messages"
#print "**          --config  || -c : Specify config file (Defualt: ./settings.conf)"
#print "*******************************************************************************\n"

## Setup logging
verbose = True
log = logger.logger()
log.setup(0, verbose)

###############################################################################################
## Variable Declarations
###############################################################################################

host = '192.168.1.5'
port = 41788
password = 'hello'
append_return = True

m = hashlib.new('sha1')
if(password.strip() != "" and password.upper().strip() != 'NONE'):
        m.update(password)
        server_hash = m.hexdigest()     
else:
        server_hash = "none"

print "\nPassword (SHA1 Hex Digest): " + server_hash + "\n" 

error_codes = {'ERR1':'Invalid data format and/or syntax!', 'ERR2':'No data was sent!', 'ERR3':'Invalid Command Sent!', 'ERR4':'Missing/Empty Command Argument(s) Recvd.', 'ERR5':'Invalid command syntax!', 'ERR6':'Invalid Auth Syntax!', 'ERR7':'Access Denied!', 'ERR8':'Server Timeout, Too Busy to Handle Request!', 'ERR9':'Unknown Data Transmission Error',
                'ERR10':'Auth required.', 'ERR11':'Invalid Password.', 'ERR12':'Not logged in.', 'ERR13':'Incorrect Username/Password!', 'ERR14':'Invalid Login Command Syntax.', 'ERR19':'Unknown Auth Error'
        }

        
shell = win32com.client.Dispatch("WScript.Shell")

def type_string(s):
    char_array = list(s);
    for char in char_array:
        shell.SendKeys(str(char), 0)
        
##
## Handle data received by the server
##
def handleConnection(cs):
        Authed = False
        data = str(cs.recv(4096)).strip()
        if not len(data) or data == "":
                log.warning("handleConnection", "No data sent!")
                cs.send("ERR2\n")
                return False
        #else:
                #if str(data).strip() != "": #TODO: Remove this when no longer debugging
                        #print("DEBUG: Received Data: " + str(data).strip())
                #data = str(data).strip()
        ###############################################
        ## Basic server commands
        if data.upper().startswith("CHECK") and data.upper().find(DSEP) > 0 and data.upper().endswith(";"):
                check_array = data.upper().split(DSEP)
                client_hash = check_array[1]
                client_hash  = client_hash[:-1] #Remove trailing semi-colon on the last parameter value
                #print "clint_hash: " + client_hash
                if server_hash.upper().strip() != "NONE":
                        if client_hash.upper().strip() == server_hash.upper().strip():
                                Authed = True
                                cs.send(OK)
                                log.info("BoIP Client Verification", "BoIP cilent has verified its server settings OK")
                        else:
                                Authed = False
                                log.info("Invalid Password", "Invalid password was sent by the client!")
                                cs.send("ERR11\n")
                                return False
                else:
                        cs.send(OK)
                        log.info("BoIP Client Verification", "BoIP cilent has verified its server settings OK") 
                return True     
        if data.upper() == "VERSION": # Get the server version and information
                log.info("Version Info Request", "A client is requesting the server's version information.")
                cs.send("\n*******************************************************************\nBarcodeOverIP-server 0.2.1 Beta \nPowered by Python 2.7.2 and pySendKeys\nThis server is for use with mobile device applications.\nYou must have the right client to use it!\nPlease visit: https://code.google.com/p/barcodeoverip/ for more\ninformation on available clients.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012\nGoogle Code Website: https://code.google.com/p/barcodeoverip/\n*******************************************************************\n\n")
                return True
        if data.upper().startswith("ERR") and data.upper().find(" ") >= 0:
                er = data.split(" ")
                if er[0].strip().upper() == "ERR" and er[1].strip().upper() != "" and len(er[1].strip().upper()) > 0:
                        errdesc = "\nError '" + er[1].strip().upper() + "' Meaning: " + error_codes[er[1].strip().upper()] + "\n"
                        cs.send(errdesc)
                        return True
        ###############################################
        ## Check data for errors
        if not data.endswith(DLIM):
                log.info("", "Invalid data format and/or syntax! - Does not end with '" + DLIM + "'")
                cs.send("ERR1\n")
                return False
        data = data[:-1]
        data_array = data.split(DSEP);
        if data.find(DSEP) == -1 or len(data_array) < 1:
                log.info("", "Invalid data format and/or syntax! - Cannot find any (or enough) data!")
                cs.send("ERR1\n")
                return False

        #####################################################
        ## Parse data while error checking
        if server_hash.lower() == data_array[0].lower() or server_hash.upper() != "NONE":
                Authed = True
                if(server_hash.upper() == "NONE"):
                        log.info("No Password Set", "No password is set in settings.conf therefore access is granted to anyone. Using a password is STRONGLY suggested!");
                else:
                        log.info("Password Accepted!", "Your password was correct! You have been granted authorization!");
        else:
                Authed = False
                log.info("Invalid Password", "Invalid password was sent by the client!")
                cs.send("ERR11\n")
                return False
        log.info("Finished Parsing Data", "Parsed data: '" + data_array[1] + "'")
        log.info("Sending Keyboard Emulation", "Sending keystrokes to system...")
        type_string(data_array[1].strip())
        if append_return:
                #type_keycode(36)
                shell.SendKeys("(ENTER)", 0)
        log.info("Sending Keyboard Emulation... DONE!", "Sending keystrokes to system... FINISHED!")
        log.info("Sending 'THANKS' To Client", "Sending a thank you to the client to inform of successful receipt.")
        cs.send(THANKS)
        return True


###############################################################################################
## Start the server
###############################################################################################




s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((host, port))
s.listen(5)
log.info("Server Socket Created successfully!", "Host/IP: " + host + " -- Port: " + str(port))

try:
        while 1:
                (clientsock, clientaddr) = s.accept()
                log.info("Incoming Connection!", "From: " + str(clientsock.getpeername()))
                #TODO: Use a fork/thread instead of a function call
                handleConnection(clientsock)
                clientsock.close()
except (KeyboardInterrupt, SystemExit): # Wait for a keyboard interupt
        log.info("Keyboard Interupt", "Received keyboard interrupt, quitting threads")
        #threadListener.stop() # Stop the thread
        sys.exit(0)
