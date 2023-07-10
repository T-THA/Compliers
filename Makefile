include Makefile.git

# export CLASSPATH=/usr/local/lib/antlr-*-complete.jar
DOMAINNAME = oj.compilers.cpl.icu
ANTLR = java -jar /usr/local/lib/antlr-*-complete.jar -listener -visitor -long-messages
JAVAC = javac -g
JAVA = java
CLASSPATH = $(shell echo `find /usr/local/lib -name "*.jar"` | sed  "s/\s\+/:/g")
# CLASSPATH = /usr/local/lib/*.jar
PFILE = $(shell find . -name "SysYParser.g4")
LFILE = $(shell find . -name "SysYLexer.g4")
JAVAFILE = $(shell find . -name "*.java")
ANTLRPATH = $(shell find /usr/local/lib -name "antlr-*-complete.jar")

# compile: antlr
# 	$(call git_commit,"make")
# 	mkdir -p classes
# 	$(JAVAC) -classpath $(ANTLRPATH) $(JAVAFILE) -d classes
compile: antlr
	$(call git_commit,"make")
	mkdir -p classes
	$(JAVAC) -classpath $(CLASSPATH) $(JAVAFILE) -d classes


# run: compile
# 	java -classpath ./classes:$(ANTLRPATH) Main $(FILEPATH)
run: compile
	java -classpath ./classes:$(CLASSPATH) Main $(FILEPATH) $(OUTPUTPATH)


antlr: $(LFILE) $(PFILE) 
	$(ANTLR) $(PFILE) $(LFILE)


# test: compile
# 	$(call git_commit, "test")
# 	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test1.sysy &
test: compile
	$(call git_commit, "test")
	java -classpath ./classes:$(CLASSPATH)  Main ./tests/test1.sysy  ./tests/moudle



clean:
	rm -f src/*.tokens
	rm -f src/*.interp
	rm -f src/SysYLexer.java src/SysYParser.java src/SysYParserBaseListener.java src/SysYParserBaseVisitor.java src/SysYParserListener.java src/SysYParserVisitor.java
	rm -rf classes


submit: clean
	git gc
	bash submit.sh

mysubmit: clean
	git add .
	git commit -m "try"
	bash submit.sh
	make antlr
	
eztest: compile
	$(call git_commit, "test")
	java -classpath ./classes:$(ANTLRPATH) Main ./tests/test1.sysy

.PHONY: compile antlr test run clean submit


