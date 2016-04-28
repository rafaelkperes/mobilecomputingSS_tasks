import serial
import time
import numpy as np

ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

sleeptime = 3.0
values = []
value = 0
boundaries = [100, 800]
factor = 1000
character = []
charsize = 7 * 2
checked = False

while True:
        line = ser.readline()
        line = line.rstrip()
        lastline = line
        
        while (int(time.time() * factor) % int(sleeptime * factor) < (sleeptime * factor) / 2):
                checked = True
                # several times per interval
                line = ser.readline()
                line = line.rstrip()
                if abs(int(lastline) - int(line)) > 50:
                        break
                #print line
                try:
                        values.append(int(line))
                except ValueError:
                        pass

        if checked:
                checked = False
                print "In!"
                # once per interval
                if values:
                        nparray = np.asarray(values)
                        meanvalue = np.mean(nparray)
                        bitvalue = 0
                        if meanvalue < boundaries[0]:
                                bitvalue = 2
                        elif meanvalue < boundaries[1]:
                                bitvalue = 0
                        else:
                                bitvalue = -2
                        print meanvalue
                        print bitvalue
                        if len(character) < charsize:
                                character.append(bitvalue)
                        else:
                                print character
                                character = []
                values = []
                
        
        #time.sleep(sleeptime/16)

       

ser.close()
