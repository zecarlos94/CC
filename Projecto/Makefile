all: clean exceptions musicmanager users server thread user client pdu

# build for when clean fails.
build: exceptions musicmanager users server thread client

exceptions: src/Exceptions/*.java
	cd src/ && cd Exceptions/  && javac *.java

musicmanager: src/MusicManager.java
	cd src/ && javac MusicManager.java

server: src/Server.java
	cd src/ && javac Server.java

thread: src/UserThread.java
	cd src/ && javac UserThread.java

users: src/Users.java
	cd src/ && javac Users.java

client: src/Client.java
	cd src/ && javac Client.java

pdu: src/PDU.java
		cd src/ && javac PDU.java

fast: 
	javac src/Exceptions/*.java src/*.java
# check for todos in the source code.
todos:
	grep -r -n "TODO" src/

# check for fixes needed in the source code.
fixme:
	grep -r -n "FIXME" src/

clean:
	rm -f src/*.class
