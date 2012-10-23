#!/usr/bin/python
##
## BarcodeOverIP-server (python) Version 0.2.x Beta
## Copyright (C) 2012  Tyler H. Jones (me@tylerjones.me)
## http://www.thebasementserver.com/phpimgdump
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

CONFIG_FILE="settings.conf"
APPNAME="BarcodeOverIP-server"

import time, shelve, os, sys, random, string, shutil

try:
	config = open(CONFIG_FILE).read().split("\n")
	conffile = CONFIG_FILE
except IOError:
	conffile = raw_input(APPNAME + " cannot find 'settings.conf'! It should be here in the root folder of the server application (settings.conf).\nPlease enter the full path to the settings file.\n(i.e. '/home/user/settings.conf' OR 'C:\Users\User\biop-server\settings.conf') >>> ")
	try:
		config = open(conffile).read().split("\n")
	except IOError:
		print "*** Cannot find/access the given settings file! ***"
		#LOGTHIS
		sys.exit()

conf = shelve.open(".config")
conf.clear()
conf.close()
	
def configure(rehash="NO"):
	config = open(conffile).read().split("\n")
	linenumber = 1
	defined = {}
	for line in config:
		if line == "":
			pass
		elif line[0] == "#":
			pass
		else:
			option = line.split()[0]

####################################################################
## Server Settings
####################################################################
			if option == "SystemName":
				if "SystemName" in defined:
					print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
					print "*** SystemName already defined, using earlier definition ***"
				else:
					try:
						tmp = line.split("=")[1].strip().split("##")[0].strip()
						if tmp == "":
							print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
							print "*** No arguments for option SystemName, using default 'BoIP_Target' ***"
							defined["SystemName"] = "BoIP_Target"
						else:
							defined["SystemName"] = tmp
					except IndexError:
						print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
						print "*** No arguments for option SystemName, using default 'BoIP_Target' ***"
						defined["SystemName"] = "BoIP_Target"
			elif option == "BindIP":
				if "BindIP" in defined:
					print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
					print "*** BindIP already defined, using earlier definition ***"
				else:
					try:
						tmp = line.split("=")[1].strip().split("##")[0].strip()
						if tmp == "":
							print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
							print "*** No arguments for option BindIP, using default '0.0.0.0' ***"
							defined["BindIP"] = "0.0.0.0"
						else:
							defined["BindIP"] = tmp
					except IndexError:
						print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
						print "*** No arguments for option BindIP, using default '0.0.0.0' ***"
						defined["BindIP"] = "0.0.0.0"
			elif option == "BindPort":
				if "BindPort" in defined:
					print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
					print "*** BindPort already defined, using earlier definition ***"
				else:
					try:
						tmp = line.split("=")[1].strip().split("##")[0].strip()
						if tmp == "":
							print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
							print "*** No arguments for option BindPort, using default '41788' ***"
							defined["BindPort"] = "41788"
						else:
							defined["BindPort"] = tmp
					except IndexError:
						print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
						print "*** No arguments for option BindPort, using default '41788' ***"
						defined["BindPort"] = "41788"

			elif option == "Password":
				if "Password" in defined:
					print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
					print "*** Password already defined, using earlier definition ***"
				else:
					try:
						tmp = line.split("=")[1].strip().split("##")[0].strip()
						if tmp == "":
							print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
							print "*** No arguments for option Password, using default (NONE) ***"
							defined["Password"] = ""
						else:
							defined["Password"] = tmp
					except IndexError:
						print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
						print "*** No arguments for option Password, using default (NONE) ***"
						defined["Password"] = ""

			elif option == "AppendReturn":
		        	if "AppendReturn" in defined:
		            		print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
		            		print "*** AppendReturn already defined, using earlier definition ***"
		        	else:
		            		try:
						AppendReturn = line.split("=")[1].strip().split("##")[0].strip()
						if AppendReturn.upper() != "TRUE" and AppendReturn.upper() != "FALSE":
		                    			print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
		                    			print "*** Invalid arguments for option AppendReturn, using default (FALSE) ***"
		                    			print "*** Choices: TRUE, FALSE ***"
				    			defined["AppendReturn"] = "FALSE"
						else:
		                    			defined["AppendReturn"] = AppendReturn
		           		except IndexError:
		                		print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
		                		print "*** No arguments for option AppendReturn, using default (FALSE) ***"
						defined["AppendReturn"] = "FALSE"

## End of if statement
			else:
				print "*** CONFIG PARSE ERROR ON LINE " + str(linenumber) + " ***"
				print "*** Unknown option: " + option + " ***"
				print "*** Ignoring ***"
		linenumber += 1
			
	if "SystemName" not in defined.keys():
		print "*** CONFIG PARSE ERROR ***"
		print "*** No SystemName defined ***"
		if rehash == "NO":
			print "*** FATAL: quitting ***"
		return "Error"
	if "BindIP" not in defined.keys():
		print "*** CONFIG PARSE ERROR ***"
		print "*** No BindIP defined ***"
		if rehash == "NO":
			print "*** FATAL: quitting ***"
		return "Error"
	if "BindPort" not in defined.keys():
		print "*** CONFIG PARSE ERROR ***"
		print "*** No BindPort defined ***"
		if rehash == "NO":
			print "*** FATAL: quitting ***"
		return "Error"
	if "Password" not in defined.keys():
		print "*** CONFIG PARSE ERROR ***"
		print "*** No Password defined ***"
		if rehash == "NO":
			print "*** FATAL: quitting ***"
		return "Error"
	if "AppendReturn" not in defined.keys():
		print "*** CONFIG PARSE ERROR ***"
		print "*** No AppendReturn defined ***"
		if rehash == "NO":
			print "*** FATAL: quitting ***"
		return "Error"

	return defined
