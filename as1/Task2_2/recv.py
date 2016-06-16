import serial
import time
import numpy as np
import binascii as ba

ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)
sleeptime = 0.05
boundaries = [400, 800] # different raspberry pi and different time, the values from ADC will change.
checked = False
signal = []
row1 = np.array([1, -1])
row2 = np.array([1, 1])
sender1_str = ''
sender2_str = ''
sender1_bit = np.array([])
sender2_bit = np.array([])


def chunks(l, n):
    """Yield successive n-sized chunks from l."""
    return [l[i:i+n] for i in range(0, len(l), n)]

def bintostr(strg):
	binstr = ba.unhexlify('%x' % int(strg, 2))
	return binstr


while True:
    t0 = time.time()
    ser.flushInput() # empty ADC memory, every time, we can get a new value
    line = ser.readline()
    line = line.rstrip()
    t1 = time.time()
    #print t1 - t0
    time.sleep(sleeptime - (t1 - t0)) #reading from ADC also costs some time, which will affecting Synchronization.
    #print line

    if int(line) < boundaries[0] and not checked:# 0 starting signal check
        checked = True
    if int(line) > boundaries[1] and checked:# 1
        checked = False
        for i in range(14):
            t2 = time.time()
            ser.flushInput() # empty ADC memory
            line = ser.readline()
            line = line.rstrip()
            #print line
            if int(line) < boundaries[0]:
                signal.append(-2)
            elif int(line) < boundaries[1]:
                signal.append(0)
            else:
                signal.append(2)
            t3 = time.time()
            
            time.sleep(sleeptime - (t3 - t2)) #same reason, in order to achieve exact synchronization
        #print signal
        
        array_chunked = np.array(chunks(signal, 2)) #slipted in 2-sized chunk
        #print array_chunked

        sender1_bit = np.dot(array_chunked, row1)
        sender2_bit = np.dot(array_chunked, row2) #convert 7 * 2 array to 7 * 1 (decode)
        #print sender1_bit

        # -2 to 0, 2 to 1
        for i in range (7):
            if sender1_bit[i] < 0:
                sender1_str = sender1_str + '0'
            else:
                sender1_str = sender1_str + '1'
                
            if sender2_bit[i] < 0:
                sender2_str = sender2_str + '0'
            else:
                sender2_str = sender2_str + '1'
                

        print bintostr('0b' + sender1_str)
        print bintostr('0b' + sender2_str)
        
        signal = []
        sender1_str = ''
        sender2_str = ''
     
ser.close()
