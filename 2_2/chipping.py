def code(bits, chipping, num):
    newbits = []
    mych = chipping[num]
    for b in bits:
        if b == "0":
            newbits.append(-1)
        else:
            newbits.append(int(b))
    chippedbits = []
    for b in newbits:
        for col in mych:
            chippedbits.append(b * col)
    return chippedbits
    
def decode(bits, chipping, num):
    decoded = []
    mych = chipping[num]
    for i in xrange(0, len(bits), 2):
        num = 0
        for j in range(len(mych)):
            num += bits[i + j] * mych[j]
        decoded.append(num)
    for i in range(len(decoded)):
        if decoded[i] > 0:
            decoded[i] = 1
        else:
            decoded[i] = 0
    return ''.join(map(str, decoded))

if __name__ == "__main__":
    bits = "01001000"
    chipping = [[1, 1], [1, -1]]
    print "Coding " + bits + ":"
    print printcode(bits, chipping, 0)

    chipped = [-2, 0, 2, 0, 0, -2, -2, 0, 2, 0, -2, 0, -2, 0, -2, 0]
    print "Decoding " + chipped + ":"
	print decode(chipped, chipping, 0)