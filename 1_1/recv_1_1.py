import RPi.GPIO as GPIO
import time
import binascii as ba
import sys

def bintostr(strg):
        binstr = ba.unhexlify('%x' % int(strg, 2))
        return binstr

def recv(setupcode):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(setupcode, GPIO.IN)

        while True:
                #starting code 01
                started = False
                while not started:                
                        value = GPIO.input(setupcode)
                        time.sleep(1)
                        if value == 1:
                                value = GPIO.input(setupcode)
                                time.sleep(1)
                                if value == 0:
                                        started = True
                print "started!"        
        
                strbin = ""
                for cnt in range(8):
                        if cnt == '1':
                                strbin += 'b'
                        else:
                                value = GPIO.input(setupcode)
                                time.sleep(1)
                                #print value
                                strbin += str(value)
                print bintostr(strbin)
                #sys.stdout.write(bintostr(strbin))

setupcode = 17
recv(setupcode)
