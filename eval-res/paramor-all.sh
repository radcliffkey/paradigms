cd ../paramor_rk

#cz10 corpus

./runAndCopyResults.sh experiments/czech10.noseed.init cz10 noseed
cp autoSeed.txt autoSeed-cz10.txt

for SUFFIX in seed seed.pref autoseed bothseed bothseed.pref autoasman; do
	./runAndCopyResults.sh experiments/czech10.$SUFFIX.init cz10 $SUFFIX
done

#cz20 corpus

./runAndCopyResults.sh experiments/czech20.noseed.init cz20 noseed
cp autoSeed.txt autoSeed-cz20.txt

for SUFFIX in seed seed.pref autoseed bothseed bothseed.pref autoasman; do
	./runAndCopyResults.sh experiments/czech20.$SUFFIX.init cz20 $SUFFIX
done


#si corpus

./runAndCopyResults.sh experiments/slovene.noseed.init si noseed
cp autoSeed.txt autoSeed-si.txt

for SUFFIX in seed autoseed bothseed autoasman; do
	./runAndCopyResults.sh experiments/slovene.$SUFFIX.init si $SUFFIX
done

# de corpus

for SUFFIX in noseed seed seed.pref; do
	./runAndCopyResults.sh experiments/german.$SUFFIX.init de $SUFFIX
done

#cat corpus

./runAndCopyResults.sh experiments/catalan.noseed.init 'cat' noseed
cp autoSeed.txt autoSeed-cat.txt

for SUFFIX in seed autoseed bothseed autoasman; do
	./runAndCopyResults.sh experiments/catalan.$SUFFIX.init 'cat' $SUFFIX
done

cd ../eval
