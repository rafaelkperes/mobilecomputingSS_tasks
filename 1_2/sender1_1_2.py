import RPi.GPIO as GPIO
import time
import binascii as ba

def strtobin(strg):
	strbin = bin(int(ba.hexlify(strg), 16))[2:]
	if len(strbin) == 6:
                strbin = "0"+strbin
	return strbin

def send(strg, setupcode, sleeptime):
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(setupcode, GPIO.OUT)
        
 	#syncronize
 	while True:
                cnt = 0
		if int(time.time()) % int(20 * sleeptime) == 0:
 			while cnt < len(strg):
				#set time for sender 1
				if (int(time.time()) % int(20 * sleeptime) >= 0) and int(time.time()) % int(20 * sleeptime) <= (9 * sleeptime):
                                        
					#starting code 01
					GPIO.output(setupcode, GPIO.HIGH)
					time.sleep(sleeptime)
					GPIO.output(setupcode, GPIO.LOW)
					time.sleep(sleeptime)
					c = strg[cnt]
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
					cnt += 1
setupcode = 23
sleeptime = 0.3 # sleeptime
send("hello from sender 1", setupcode, sleeptime)
