import RPi.GPIO as GPIO
import time
import binascii as ba

st = 1 # sleeptime

def strtobin(strg):
        strbin = bin(int(ba.hexlify(strg), 16))
        return strbin

def send(strg, setupcode):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(setupcode, GPIO.OUT)
        
        started = False
        staux = False
        while True:
                for c in strg:
                        output = GPIO.LOW         
                        if int(time.time()) % 2 == 0:
                                #starting code 01
                                if not started:                                        
                                        if not staux:
                                                print "starting..."
                                                output = GPIO.HIGH
                                                staux = True
                                        else:
                                                output = GPIO.LOW
                                                cnt = 0
                                                staux = False
                                                started = True
                                                print "started!"
                                elif started:
                                        asc = strtobin(c)
                                        b = asc[cnt]
                                        print b                                        
                                        if b == '1':
                                                output = GPIO.LOW
                                        elif b == '0':
                                                output = GPIO.HIGH
                                        if cnt == 7:
                                                cnt = 0
                                                started = False
                                                staux = False
                                        else:
                                                cnt += 1                                        
                                        
                        GPIO.output(setupcode, output)
                        time.sleep(st)

setupcode = 23
send("hello", setupcode)
