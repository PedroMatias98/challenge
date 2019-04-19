JARNAME=Challenge

all:
	(javac -cp libraries/*.jar -encoding UTF-8 `find . -name \*.java`)
	(jar -cmvf META-INF/MANIFEST.MF $(JARNAME).jar `find . -name \*.class -o -name \*.java` storedData appData)

clean:
	$(RM) $(JARNAME).jar `find . -name \*.class`
