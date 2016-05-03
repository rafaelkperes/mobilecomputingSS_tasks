import RPi.GPIO as GPIO
import time
import binascii as ba

def strtobin(strg):
	strbin = bin(int(ba.hexlify(strg), 16))[2:]
	if len(strbin) == 6:
                strbin = "0"+strbin
	return strbin

def send(strg, setupcode, sleeptime, factor):
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(setupcode, GPIO.OUT)
	
 	while True:
		for c in strg:
                        while not int(time.time() * factor) % int(16 * sleeptime * factor) == 0:
                                pass
			#starting code 01
			GPIO.output(setupcode, GPIO.HIGH)
			time.sleep(sleeptime)
			GPIO.output(setupcode, GPIO.LOW)
			time.sleep(sleeptime)
			asc = strtobin(c)
			for b in asc:
				if b == '1':
					GPIO.output(setupcode, GPIO.LOW)
					#print "1"
					time.sleep(sleeptime)
					GPIO.output(setupcode, GPIO.HIGH)
					#print "0"
					time.sleep(sleeptime)
				elif b == '0':
					GPIO.output(setupcode, GPIO.HIGH)
					#print "0"
					time.sleep(sleeptime)
					GPIO.output(setupcode, GPIO.LOW)
                			#print "1"
                                        time.sleep(sleeptime)
			#print asc
			print c
			#time.sleep(sleeptime) # additional waiting time
			#GPIO.output(setupcode, GPIO.LOW)
setupcode = 23
sleeptime = 0.05 # sleeptime
factor = 100
send("hello from sender 1", setupcode, sleeptime, factor)
