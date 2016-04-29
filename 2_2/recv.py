#import serial
import time
import numpy as np

import chipping as chip
from chipping import charsize, chipsize, id1, id2, chipping, boundaries, factor, sleeptime

def senderstub(sending, ids):
	ret = []
	for c in sending:
		asc = chip.startingcode(chip.strtobin(c))
		#asc = chip.strtobin(c)
		chsending = chip.code(asc, chipping, ids) # returns an array of int
		for b in chsending:
			ret.append(b) # change to the LED function
	ret.reverse()

	if ids == id2: # DESYNCING SENDER -- comment afterwards
		ret.append(-1)
		ret.append(-1)
	
	return ret

s1stream = senderstub("hello from sender 1", id1)
s2stream = senderstub("HELLO FROM SENDER 2", id2)

time.sleep(sleeptime)

s1started = False
s2started = False

recvreset = False

recved = []
s1bits = []
s2bits = []

print "Starting..."

while True:
	if len(recved) >= chip.chipsize: # keeps last read bit
		recved = []

	#line = ser.readline()
	#line = line.rstrip()
	line = s1stream.pop() + s2stream.pop() # SYNCHRONIZED READ - LINE TO BE MODIFIED
	print "Received: " + str(line)

	time.sleep(float(sleeptime)/10)
	recved.append(line)
	#print recved

	if len(recved) >= chipsize: # ready to decode a bit
		s1bits.append(chip.bitdecode(recved, chipping, id1))
		if not s1started:
			# decode and add to s1
			if s1bits == [0, 1]:
				s1started = True
				s1bits = []
				print "Sender 1 started."
			elif len(s1bits) == chip.chipsize: # resets, leaving last read bit
				s1bits = s1bits[-1:]
		elif len(s1bits) == charsize:
			print "Sender 1: " + chip.bintostr("0b" + ''.join(map(str, s1bits)))
			s1bits = []
			s1started = False
			time.sleep(sleeptime)

	if len(recved) >= chipsize: # ready to decode a bit
		s2bits.append(chip.bitdecode(recved, chipping, id2))
		if not s2started:
			# decode and add to s2
			if s2bits == [0, 1]:
				s2started = True
				s2bits = []
				print "Sender 2 started."
			elif len(s2bits) == chip.chipsize: # resets, leaving last read bit
				s2bits = s2bits[-1:]
		elif len(s2bits) == charsize:
			print "Sender 2: " + chip.bintostr("0b" + ''.join(map(str, s2bits)))
			s2bits = []
			s2started = False
			time.sleep(sleeptime)

# ser.close()
