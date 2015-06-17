#!/bin/bash

java -Xmx1g -cp ./target:trove.jar cz.klic.clustering.Clustering $@
