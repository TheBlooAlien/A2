JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

SRCDIR=src
BINDIR=bin
DOCDIR=doc

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES= Basin.class FindBasin.class FindAllBasins.class FindAllBasinsSeq.class

CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

clean:
	rm $(BINDIR)/*.class
	rm $(SRCDIR)/*.class
run:
	@java -cp bin FindAllBasins large_in.txt large_out.txt
	
git:
	git push origin master

doc:
	javadoc -d $(DOCDIR) src/*.java
