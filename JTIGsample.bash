#input file containing texts to parse
filename="test.txt"
#lexicon
lexicon="english-conll-5000.xml"
success=0
failure=0
total=0
while read -r line
do 
	#Parsing each sentence with time constraint
	timeout 3s java -jar JTIG-0.0.3-jar-with-dependencies.jar -lexicon "$lexicon" -input_sentence "$line"

done < "$filename"
RUNDIR="/cygdrive/c/Users/sampath/Desktop/JTIG-master/bin/data/runs"
cd $RUNDIR
RUNS=$(ls -1 $RUNDIR)
#cd $RUNDIR;rm -rf;cd ..
for A in $(ls -1)
do
	X=$(ls -1 $A | wc -l)
	if [[ $X == 1 ]]; then
		failure=$(expr $failure + 1)
	else
		success=$(expr $success + 1)
	fi
	total=$(expr $total + 1)
done
echo "Success = $success"  #total number of sentences parsed
echo "failure = $failure"  #total number of sentences not parsed
echo "Total = $total"       
