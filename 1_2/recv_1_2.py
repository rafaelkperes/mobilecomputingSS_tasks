import RPi.GPIO as GPIO
import time
import binascii as ba
import sys

st = 1 # sleeptime

def bintostr(strg):
        binstr = ba.unhexlify('%x' % int(strg, 2))
        return binstr

def recv(setupcode):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(setupcode, GPIO.IN)

        str1 = ""
        cnt1 = 0
        staux1 = False
        started1 = False
        
        str2 = ""
        cnt2 = 0
        staux2 = False
        started2 = False
        while True:
                value = GPIO.input(setupcode)
                time.sleep(st) 
                if int(time.time()) % 2 == 0:
                        # start # starting code 01
                        if not started1:
                                if not staux1 and value == 1:
                                        staux1 = True
                                elif staux1 and value == 0:
                                        staux1 = False
                                        started1 = True
                                        print "started 1!"
                        # reset
                        elif cnt1 == 7:
                                print bintostr(str1)
                                str1 = ""
                                cnt1 = 0
                                started1 = False
                        # read
                        elif started1:
                                cnt1 += 1
                                if cnt1 == '1':
                                        str1 += 'b'                                                               
                                print "sender 1: " + str(value)
                                str1 += str(value)                   
                #sys.stdout.write(bintostr(strbin))

setupcode = 17
recv(setupcode)
