default:
	clear
	javac Server.java
	java Server
1:
	clear
	java clientOne
2:
	clear
	java clientOne 2
# JFLAGS = -g
# JC = javac
# JVM = java
# .SUFFIXES: .java .class
# .java.class: ; $(JC) $(JFLAGS) $*.java

# CLASSES = \
# 	test.java

# default: classes run

# classes: $(CLASSES:.java=.class)

# clean:; $(RM) *.class
# run: classes 
# 	$(JVM) $(MAIN)

# run2: test.class
# 	java test