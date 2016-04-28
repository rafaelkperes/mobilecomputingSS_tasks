import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(24, GPIO.OUT)

while True:
        print " 0 0 "
	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	time.sleep(1)

        print " 1 0 "
	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.LOW)
	time.sleep(1)

        print " 0 1 "
        GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(1)

        print " 1 1 "
	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(1)
	
	
