import matplotlib.pyplot as plt
import sys

def main(fil):
    f = open(fil)
    lines = f.readlines()
    f.close()
    counter = 0
    relevant = 0
    precision = []
    recall = []
    X = []
    for line in lines:
        counter += 1 # hur många dokument vi har läst
        elem = line.split()
        try:
            value = int(elem[-1]) # hämta sista elementet per rad dvs relevansen
        except ValueError:
            print("Wrong formatted line...")
            print("Ignoring...")
            continue
        if value > 0:
            relevant += 1 #antalet relevanta dokument

        if counter % 10 == 0: # precision recall var tionde steg 1-10 1-20 1-40 1-50
            print("precision", relevant/counter) # precision är antalet relevanta delat på relevant+ickerelevanta dvs alla dokument
            print("recall", relevant/100) # 100 är totala antal relevanta dokument som finns
            precision.append(relevant/counter)
            recall.append(relevant/100)
            X.append(counter)

    plt.plot(X,precision,'b-',lw=2, label="Precision")
    plt.plot(X,recall,'r-', lw=2, label="Recall")
    plt.legend()
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Please provide an input file")
    else:
        main(sys.argv[1])

        #Invers relaterad varandras motsatser en 