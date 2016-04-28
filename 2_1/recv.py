import serial

ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

while True:
        line = ser.readline()
        line = line.rstrip()
        print line

ser.close()
