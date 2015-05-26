#!/usr/bin/python

from subprocess import *
import sys

paramorCmds = """sc
settl
3

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

dpc
seg
segfile
setcs
f

settl
3

rc
ws
3
segmented
y
1

r
r
q
y
"""

if __name__ == '__main__':
	
	if len(sys.argv) < 2:
		sys.exit('usage: paramor.py <paramor init file>')

	args = ['java', '-cp', './bin:./trove.jar', '-Xmx1g',
			'monson.christian.morphology.paraMor.ParaMor', '-if', sys.argv[1]]
		
	paramor = Popen(args, stdin = PIPE, stdout = sys.stdout, stderr = STDOUT)

	paramor.communicate(paramorCmds)
