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
		if int(time.time() * factor) % int(20 * sleeptime * factor) == 0:
            for c in strg:
				#set time for sender 1
                    while not (int(time.time() * factor) % int(20 * sleeptime * factor) < (10 * sleeptime * factor)):
                        pass                                
				#starting code 01
				GPIO.output(setupcode, GPIO.HIGH)
				time.sleep(sleeptime)
				GPIO.output(setupcode, GPIO.LOW)
				time.sleep(sleeptime)
				asc = strtobin(c)
				for b in asc:
					print b
					if b == '1':
						GPIO.output(setupcode, GPIO.LOW)
					elif b == '0':
						GPIO.output(setupcode, GPIO.HIGH)
					time.sleep(sleeptime)
				print asc
				print c
				time.sleep(sleeptime) # additional waiting time
				GPIO.output(setupcode, GPIO.LOW)
setupcode = 23
sleeptime = 0.04 # sleeptime
factor = 1000
send("hello from sender 1", setupcode, sleeptime, factor)
