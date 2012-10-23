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
##	  http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##

VERSION="0.2.1"
APPNAME="BarcodeOverIP-Server"
LOGFILE="boip.log"

import time, os, sys, random, string, logging, shutil

## Setup logger
log = logging.getLogger(APPNAME)
hdlr = logging.FileHandler(LOGFILE)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
log.addHandler(hdlr) 
log.setLevel(logging.INFO)

class logger:
	def __init__(self):
		return
	def setup(self, output, verbose):
		self.open = True
		self.verbose = verbose
		if output >= 0 or output <= 2:
			self.output = output
		else:
			self.output = 0;
		return True

	def info(self, title, desc):
		if title == "":
			out = desc
		else:
			out = title + " - " + desc
		if self.output == 0 or self.output == 1:
			print "-> " + out
		if self.output == 0 or self.output == 2:
			log.info(out)
		return True
		
	def warning(self, title, desc):
		if title == "":
			out = desc
		else:
			out = title + " - " + desc
		if self.output == 0 or self.output == 1:
			print "*** WARNING: " + out + " ***"
		if self.output == 0 or self.output == 2:
			log.warning(out)
		return True

	def error(self, title, desc):
		if title == "":
			out = desc
		else:
			out = title + " - " + desc
		if self.output == 0 or self.output == 1:
			print "\n*** ERROR: " + out + " ***\n"
		if self.output == 0 or self.output == 2:
			log.error(out)
		return True

	def exception(self, title, desc):
		if title == "":
			out = desc
		else:
			out = title + " - " + desc
		if self.output == 0 or self.output == 1:
			print "\nXXXX************************XXXX"
			print "*** EXCEPTION: " + out + " ***"
			print "XXXX************************XXXX\n"
		if self.output == 0 or self.output == 2:
			log.exception(out)
		return True

	def critical(self, title, desc):
		if title == "":
			out = desc
		else:
			out = title + " - " + desc
		if self.output == 0 or self.output == 1:
			print "\n!!!**************************!!!"
			print "*** CRITICAL: " + out + " ***"
			print "!!!**************************!!!\n"
		if self.output == 0 or self.output == 2:
			log.critical(out)
		return True
