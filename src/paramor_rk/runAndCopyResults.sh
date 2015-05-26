if [ $# -lt 3 ]; then
	echo "params: <config file> <lang> <file suffix>"
	exit
fi

CONF_FILE=$1
LANG=$2
FILE_SUFFIX=$3

python paramor.py $CONF_FILE
cp wordToClusters.txt "../resources/$LANG/wordToClusters-$FILE_SUFFIX.txt"
cp clusterToWords.txt "../resources/$LANG/clusterToWords-$FILE_SUFFIX.txt"
cp junk-clusters.txt ../eval/`echo $CONF_FILE | sed 's%.*/\([^/]*\)init$%\1clusters%'`

