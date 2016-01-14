# Introduction #

This document explains the different methods of downloading and installing BarcodeOverIP-Server onto a target system.

### Things to Keep In Mind ###
Currently the only release of BarcodeOverIP-Server supported by BoIP-Client versions v0.9.0 and higher (any client installed via Google Play is higher than v0.9.4) is the Java-based GUI application. This is likely to remain the only major server release as it works on any OS/arch running a modern JRE (Java Runtime Environment).


# Download Methods #

BarcodeOverIP-Server binaries (.jar) can be downloaded by using one of the two(or three) following methods:
  1. **Project Website:** The current project website is this Google Code page where you can download all releases, get the source code and find release documentation (use the header links above to navigate). BarcodeOverIP will always reside here on Google Code but may have a dedicated site someday. The following URLs will always link to the current project website where you can get downloads, documentation and sources:
    * **Project Site:** http://boip.tylerjones.me  -OR-  http://b.thj.me
    * **Google Code:** http://boip.tylerjones.me/project  -OR-  http://b.thj.me/project
  1. **Static URL:** Using the static download URL allows the **most recent/stable release**  to be downloaded directly via a browser using the same, never changing, URL. Following is a list of all static URLs for downloading and viewing basic release info:
    * **Server Download (.jar):** http://boip.tylerjones.me/server/latest
    * **Current Version Info:** http://boip.tylerjones.me/server/version
    * **Changelog:** http://boip.tylerjones.me/server/changes
  1. **Build From Source:** Use subversion to checkout the source code from Googe Code using the command:
```
# Non-members may check out a read-only working copy anonymously over HTTP.
svn checkout http://barcodeoverip.googlecode.com/svn/trunk/ barcodeoverip-read-only
```

# Application Details and Specifics #

#### System and Software Requirements: ####
  * Modern JRE (i.e. Sun-Java6 or OpenJRE 6)
  * Confiure target server's firewall to allow incoming connections from port 41788. Don't forget about the Window Firewall, 2/3 of all network communication problems experienced during testing of BoIP we caused by improper client and/or target server network configurations.
  * Working wired/wireless network connection that isn't necessarily connected to the internet (BoIP doesn't need internet access, just local connectivity.


# Tips and Tricks #

  * **ProTip #1**: Anywhere the domain text 'boip.tylerjones.me' is used in a URL it can be substituted with 'b.thj.me' instead.
  * **ProTip #2**: Sometimes shining a flashlight on hard to read barcodes gives the camera more contrast and thus reads the barcode much quicker.
  * **ProTip #3**: If you are behind a NAT (router) setup port forwarding to forward port 41788 to the target computer inside your local network so that now barcodes can be scanned in over the internet rather than just the LAN/WAN. **Be sure to use a password if sending barcodes into and/or from the local network!**