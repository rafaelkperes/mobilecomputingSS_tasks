import RPi.GPIO as GPIO
import time
import binascii as ba

def strtobin(strg):
	strbin = bin(int(ba.hexlify(strg), 16))[2:] #remove unnecessary "0b"
	if len(strbin) == 6:
                strbin = "0"+strbin
	return strbin

def send(strg, setupcode, sleeptime):
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(setupcode, GPIO.OUT)       

	while True:
		for c in strg:
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
				else:
					GPIO.output(setupcode, GPIO.HIGH)
				time.sleep(sleeptime)
			print asc
			print c
              
setupcode = 23
sleeptime = 0.5
send("hello from sender 1", setupcode, sleeptime)

