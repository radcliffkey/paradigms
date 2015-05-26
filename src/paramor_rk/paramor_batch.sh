#!/bin/bash
./paramor.sh -if $1 <<END
sc
rc
cn
cn
sp
bus
s
y

c
SetCTCC
20
c
y

MBTFLFC
f
y

MBTFRFC
f
y

wcr
y

seg
segcor
ws
3
segmented
y
1

r
r
q
y
END