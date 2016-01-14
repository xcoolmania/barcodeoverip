# BarcodeOverIP-Client 1.0.3`-r3` is now available! #
# Get it on Google Play! #

## Easy home-screen widget, supports 2D barcode/datamatrix code types with alphanumeric data support and it works on all current Android platforms! ##


---


## Looking For BarcodeOverIP-Server? Look No Further! ##
### Downloading and Installing BarcodeOverIP-Server ###
It is recommended that you read [WikiGetServerInstructions](WikiGetServerInstructions.md) before you do anything. Once you have a good understanding of how BarcodeOverIP-Server (and Client) works, use the links below to get the latest release and get it installed and working on your system.
**(NOTE: I have fixed these links!)**

  * **Latest Stable Release URL:** http://boip.tylerjones.me/server/latest
  * **Latest Stable Release Changes:** http://boip.tylerjones.me/server/changes

## **NOTE:** Only BoIP Client/Server v1.0.x will work with ClientServer v1.0.x and vice versa. If you download the new client release you MUST download the new server release too! ##


---


## About BarcodeOverIP (a.k.a. BoIP) ##
BarcodeOverIP is an open source application that enables any network-enabled Android device to serve as a wireless barcode scanner, sending the barcode values to any PC/MacOS/`*`nix computer that is running BarcodeOverIP-Server.
(See section below for a link to the download instructions wiki.)

## Project Objective and Usage ##
BarcodeOverIP is aimed at reducing the amount of time it takes to scan a barcode by cutting down on the number of actions the user must take to scan a barcode. For instance, the main application interface (window) is based around a scrolling list view of all the user-defined servers. To send a barcode to a server, with the user performing only a single action, simply tap the desired server then scan the barcode, and in an instant the barcode value will be input into the server. Press and hold a server in the list to display a menu allowing you to edit or remove that selected server. Press the menu key to display the 'Add Server', 'Donate', 'About', and 'Exit' options.

## Key Features ##
One key similarity shared by BarcodeOverIP and an everyday barcode scanner is the option to append a newline character (enter key) to the end of a barcode value when it is being input into the server. This saves time, eliminating the need to hit 'Enter' after each item is scanned. This option can be turned on or off at the server application. The barcode scanned can be activated from the home screen via a widget to make scanning even faster and more convenient.

## Security Features ##
As with all new server software there are probably a few security risks with using this software. But used primarily in a private WiFi network, there should be minimal problem. Naturally as I (or you) find bugs and holes, I will fix them. The data that is sent of the
network when scanning a barcode is encrypted, just in case.

Following is a list of these security features:
  1. By default when the server is started, it is ready to receive barcodes. However, if the user does not want to be interrupted by incoming barcodes, the activate/deactivate button on the server application allows the server user to control the input of barcodes to the server.
  1. If a user would like to restrict access to a server from other BarcodeOverIP Clients, a password can be set up at the server application and every client will need the password to send a barcode. By default a server is accessible by any user that knows, or can obtain, the IP/port of the server.
  1. Barcodes are encrypted before being sent over the network and decrypted once they reach the destination server.