cd ../clustering
for i in $@; do
    logName=`echo $i | sed 's%.*/\([^/]*\)properties$%\1log%'`
    echo "running $i"
    ./runEval.sh -v -g --threadCount 6 -f $i &> ../eval/$logName
    grep --colour=never 'recall =' ../eval/$logName
    echo
done
cd ../eval
