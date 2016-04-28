import RPi.GPIO as GPIO
import time
import binascii as ba

def strtobin(strg):
        strbin = bin(int(ba.hexlify(strg), 16))
        return strbin

def send(strg, setupcode):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(setupcode, GPIO.OUT)       
        
        while True:
                for c in strg:
                        #starting code 01
                        GPIO.output(setupcode, GPIO.HIGH)
                        time.sleep(1)
                        GPIO.output(setupcode, GPIO.LOW)
                        time.sleep(1)
                        
                        asc = strtobin(c)
                        #print asc
                        for b in asc:
                                #print b
                                if b == '1':
                                        GPIO.output(setupcode, GPIO.LOW)
                                elif b == '0':
                                        GPIO.output(setupcode, GPIO.HIGH)
                                #else:
                                        #print b
                                time.sleep(1)              

setupcode = 23
send("hello", setupcode)
