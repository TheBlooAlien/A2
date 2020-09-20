JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

SRCDIR=src
BINDIR=bin
DOCDIR=doc
DATADIR=data

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES= Terrain.class Water.class FlowPanel.class FlowThread.class Flow.class

CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

clean:
	rm $(BINDIR)/*.class
	rm $(SRCDIR)/*.class
runsmall:
	@java -cp bin Flow "small_in.txt"
runmed:
	@java -cp bin Flow "medsample_in.txt"

runlarge:
	@java -cp bin Flow "largesample_in.txt"

git:
	git push origin master

gitadd:
	git add *
doc:
	javadoc -d $(DOCDIR) src/*.java
