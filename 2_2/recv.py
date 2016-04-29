#import serial
import time
import numpy as np

import chipping as chip
from chipping import charsize, chipsize, id1, id2, chipping, boundaries, factor, sleeptime

def senderstub(sending, ids):
    ret = []
    for c in sending:
        #asc = chip.startingcode(chip.strtobin(c))
        asc = chip.strtobin(c)
        chsending = chip.code(asc, chipping, ids) 
        for b in chsending:
            #print "Sender 1: " + str(b)
            #yield b
            ret.append(b)
    ret.reverse()
    return ret

#ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

values = []
value = 0

character = []
checked = False 

s1stream = senderstub("hello from sender 1", id1)
s2stream = senderstub("HELLO FROM SENDER 2", id2)

print "Starting..."

while True:
	#line = ser.readline()
	#line = line.rstrip()
	#lastline = line
	recved = []
	for i in range(charsize * chipsize):
		line = s1stream.pop() + s2stream.pop()
		#print "Received: " + str(line)
		recved.append(line)
		time.sleep(sleeptime)
	rec1 = chip.decode(recved, chipping, id1)
	rec2 = chip.decode(recved, chipping, id2)
	print "Sender 1: " + chip.bintostr("0b" + rec1)
	print "Sender 2: " + chip.bintostr("0b" + rec2)
	time.sleep(1)
	# while (int(time.time() * factor) % int(sleeptime * factor) < (sleeptime * factor) / 2):
	# 	checked = True
	# 	# several times per interval
	# 	line = ser.readline()
	# 	line = line.rstrip()
	# 	if abs(int(lastline) - int(line)) > 50:
	# 		break
	# 	#print line
	# 	try:
	# 		values.append(int(line))
	# 	except ValueError:
	# 		pass

	# if checked:
	# 	checked = False
	# 	print "In!"
	# 	# once per interval
	# 	if values:
	# 		nparray = np.asarray(values)
	# 		meanvalue = np.mean(nparray)
	# 		bitvalue = 0
	# 		if meanvalue < boundaries[0]:
	# 			bitvalue = 2
	# 		elif meanvalue < boundaries[1]:
	# 			bitvalue = 0
	# 		else:
	# 			bitvalue = -2
	# 		print meanvalue
	# 		print bitvalue
	# 		if len(character) < charsize:
	# 			character.append(bitvalue)
	# 		else:
	# 			print character
	# 			character = []
	# 	values = []

# ser.close()
