#!/bin/bash

java -Xmx1g -cp ./bin:trove.jar cz.klic.eval.EvalClustering $@
