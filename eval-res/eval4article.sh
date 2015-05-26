cd ../paramor_rk

#cz10 corpus

./runAndCopyResults.sh experiments/czech10.noseed.init cz10 noseed
cp autoSeed.txt autoSeed-cz10.txt

for SUFFIX in seed; do
	./runAndCopyResults.sh experiments/czech10.$SUFFIX.init cz10 $SUFFIX
done

#cz20 corpus

./runAndCopyResults.sh experiments/czech20.noseed.init cz20 noseed
cp autoSeed.txt autoSeed-cz20.txt

for SUFFIX in seed; do
	./runAndCopyResults.sh experiments/czech20.$SUFFIX.init cz20 $SUFFIX
done

#si corpus

./runAndCopyResults.sh experiments/slovene.noseed.init si noseed
cp autoSeed.txt autoSeed-si.txt

for SUFFIX in seed; do
	./runAndCopyResults.sh experiments/slovene.$SUFFIX.init si $SUFFIX
done

# de corpus

./runAndCopyResults.sh experiments/german.noseed.init de noseed
./runAndCopyResults.sh experiments/german.seed.init de seed

# cat corpus

./runAndCopyResults.sh experiments/catalan.noseed.init 'cat' noseed
./runAndCopyResults.sh experiments/catalan.seed.init 'cat' seed

cd ../eval
./clustering-all.sh ../clustering/experiments/*direct*
