import RPi.GPIO as GPIO
import time
import binascii as ba
import sys

def bintostr(strg):
	binstr = ba.unhexlify('%x' % int(strg, 2))
	return binstr

def recv(setupcode, sleeptime):
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(setupcode, GPIO.IN)
      # this is much simplier than the previous version
	checked = False 
	while True:
		#starting code 01
		value = GPIO.input(setupcode)
 		time.sleep(sleeptime)
		if value == 0 and not checked:
			checked = True
 		elif value == 1 and checked:
			checked = False        
			strbin = "0b"
			for cnt in range(7):
				value = GPIO.input(setupcode)
				time.sleep(sleeptime)
				#print value
				strbin += str(value)
			#print strbin
			print bintostr(strbin)
setupcode = 17
sleeptime = 0.5
recv(setupcode, sleeptime)
