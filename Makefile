JAVAC=javac
JAVA=java
.SUFFIXES: .java .class
SRCDIR=src
BINDIR=bin

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<
	
CLASSES=MeanFilter_Serial.class \
		MeanFilter_Parallel.class \
		MedianFilter_Serial.class \
		MedianFilter_Parallel.class

CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

run: $(CLASS_FILES)
	$(JAVA) -cp $(BINDIR) MeanFilter_Parallel

clean:
	@if exist $(BINDIR)\*.class ( \
		del $(BINDIR)\*.class \
	) else ( \
		echo No class files to remove. \
	)