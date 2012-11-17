#!/bin/sh
#( ./niwaka.sh start > /dev/null ) > & error.log &

cd `dirname ${0}`
if [ $# -eq 3 ]; then
#	java -cp twitter4j-core-2.2.1.jar:twitter4j-stream-2.2.1.jar:. niwaka $1 $2 $3
	java -cp Niwaka_BOT.jar niwaka $1 $2 $3
	exit 3
fi
if [ $# -eq 2 ]; then
#	java -cp twitter4j-core-2.2.1.jar:twitter4j-stream-2.2.1.jar:. niwaka $1 $2
	java -cp Niwaka_BOT.jar niwaka $1 $2
	exit 2
fi
if [ $# -eq 1 ]; then
#	java -cp twitter4j-core-2.2.1.jar:twitter4j-stream-2.2.1.jar:. niwaka $1
	java -cp Niwaka_BOT.jar niwaka $1
	exit 1
fi
#java -cp twitter4j-core-2.2.1.jar:twitter4j-stream-2.2.1.jar:. niwaka
java -cp Niwaka_BOT.jar niwaka
exit 0

