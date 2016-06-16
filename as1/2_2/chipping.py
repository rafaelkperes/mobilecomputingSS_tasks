import binascii as ba

# LOGIC CONSTANTS
charsize = 7
chipsize = 2
id1 = 0
id2 = 1

chipping = [[1, 1], [1, -1]]

# ANALOGIC CONSTANTS
boundaries = [100, 800]
factor = 1000
sleeptime = 1

def strtobin(strg):
	strbin = bin(int(ba.hexlify(strg), 16))[2:]
	if len(strbin) == 6:
				strbin = "0" + strbin
	return strbin

def bintostr(strg):
	binstr = ba.unhexlify('%x' % int(strg, 2))
	return binstr

def startingcode(sending):
	return "01" + sending

def code(bits, chipping, num):
	newbits = []
	mychipping = chipping[num]
	for b in bits:
		if b == "0":
			newbits.append(-1)
		else:
			newbits.append(int(b))
	chippedbits = []
	for b in newbits:
		for col in mychipping:
			chippedbits.append(b * col)
	return chippedbits

def bitdecode(bits, chipping, num):
	mychipping = chipping[num]
	acc = 0
	for j in range(len(mychipping)):
		acc += bits[j] * mychipping[j]
	if acc > 0:
		return 1
	else:
		return 0

def decode(bits, chipping, num):
	decoded = []
	mychipping = chipping[num]
	for i in range(0, len(bits), 2):
		decoding = []
		for j in range(len(mychipping)):
			decoding.append(bits[i + j])
		decoded.append(bitdecode(decoding, chipping, num))
	# for i in range(0, len(bits), 2):
	# 	acc = 0
	# 	for j in range(len(mychipping)):
	# 		acc += bits[i + j] * mychipping[j]
	# 	decoded.append(acc)
	# for i in range(len(decoded)):
	# 	if decoded[i] > 0:
	# 		decoded[i] = 1
	# 	else:
	# 		decoded[i] = 0
	return ''.join(map(str, decoded))

if __name__ == "__main__":
	bits = "01001000"
	print "Coding " + bits + ":"
	print printcode(bits, chipping, 0)

	chipped = [-2, 0, 2, 0, 0, -2, -2, 0, 2, 0, -2, 0, -2, 0, -2, 0]
	print "Decoding " + chipped + ":"
	#print decode(chipped, chipping, 0)