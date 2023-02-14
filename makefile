default: clean
	clear
	javac Server.java
	java Server
compile:
	clear
	javac Server.java
	javac client.java

serverD:
	clear
	java Server 1

removeClassFiles:
	rm *.class || true
clean: removeClassFiles
	clear
	javac Server.java
	javac client.java
1:
	clear
	java client
2:
	clear
	java client 2
1d:
	clear
	java client 1 1
2d:
	clear
	java client 2 1