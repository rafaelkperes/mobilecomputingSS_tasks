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
        
 	#syncronize
 	while True:
		if int(time.time() * factor) % int(10 * sleeptime * factor) == 0:
                        for c in strg:                                
				#starting code 01
				#GPIO.output(setupcode, GPIO.HIGH)
				#time.sleep(sleeptime)
				#GPIO.output(setupcode, GPIO.LOW)
				#time.sleep(sleeptime)
				asc = strtobin(c)
				for b in asc:
					if b == '1':
						GPIO.output(setupcode, GPIO.LOW)
						print "1"
						time.sleep(sleeptime)
						GPIO.output(setupcode, GPIO.HIGH)
						print "0"
						time.sleep(sleeptime)
					elif b == '0':
						GPIO.output(setupcode, GPIO.HIGH)
						print "0"
						time.sleep(sleeptime)
						GPIO.output(setupcode, GPIO.LOW)
						print "1"
                                                time.sleep(sleeptime)
				print asc
				print c
				time.sleep(sleeptime) # additional waiting
				GPIO.output(setupcode, GPIO.LOW)
				#I think we should delete the waiting time(line 42) and GPIO.LOW(line 43)
				#The waiting time will cause a reapted samling of last bit
				#The GPIO.LOW will force two LEDs off, which will probably cause an unncessary bit at receiver
setupcode = 23
sleeptime = 3 # sleeptime
factor = 1000
send("hello from sender 1", setupcode, sleeptime, factor)
