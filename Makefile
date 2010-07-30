SRC = src
CLS = classes

all: cleanup compile jar
	@@echo "compile complete."

cleanup:
	@@if test -d ${CLS}; \
		then rm -r ${CLS}; \
	fi
	@@if test -e checker.jar; \
		then rm checker.jar; \
	fi
	@@mkdir ${CLS}

compile:
	@@echo "compileing"
	@@javac ${SRC}/*.java -d ${CLS}

jar:
	@@echo "creating checker.jar"
	@@jar cfm checker.jar Manifest -C ${CLS} URLChecker
