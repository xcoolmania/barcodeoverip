#!/usr/bin/python

#########################################################
## Constant variables
VERSION="1.0"
APPNAME="BarcodeOverIP-server-client-testing"
DSEP="||"
DLIM=";"
DDATA="_DATA:" #ex: "%_CMD:getimgfile=HY9cyL;getimginfo=HY9cyL;"
DHASH="_HASH:" #ex: "%_AUTH:userid||authkey_CMD:getimgfile=HY9cyL;"
THANKS = "THANKS\n" #sent from server to client when server has finished successfully
OK = "OK\n" #Server's response to CHECK command on success

## Imports
import time, shelve, os, sys, string, socket, hashlib
#import threading, base64
import config, logger

if sys.platform == "win32":
        ### Windows ONLY
        import win32com.client
elif sys.platform == "linux2":
        ### Linux/X11 ONLY:
        import virtkey
elif sys.platform == "darmin":
        ### MacOSX Only, dunno what to do here...
        print "\n\n *** MacOS X support is still under development!\nSorry to dissapoint you! ***\n\n"
        sys.exit()
else:
        ### Other systems
        print "\n\n**************************************************"
        print "** You seem to be running Python on a system not supported"
        print "** by BarcodeOverIP-Server (Python). So far SoIP supports"
        print "** Linux, Unix, MacOS X and Windows"
        print "**************************************************\n\n"
        sys.exit()

##########
## WIN32/
##########
if sys.platform == "win32":
        def type_string(s):
                shell = win32com.client.Dispatch("WScript.Shell")
                char_array = list(s)
                for char in char_array:
                        shell.SendKeys(str(char), 0)
                                                
##########
## /WIN32
##########

##########
## LINUX/
##########
if sys.platform == "linux2":
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
                char_array = list(s)
                for char in char_array:
                        type_key(char)
                                                
##########
## /LINUX
##########

print "Start ------> "

# All platforms can see this                                            
type_string(str(sys.platform))

print "<-----------END"

sys.exit()
