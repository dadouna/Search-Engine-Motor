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
        counter += 1
        elem = line.split()
        try:
            value = int(elem[-1])
        except ValueError:
            print("Wrong formatted line...")
            print("Ignoring...")
            continue
        if value > 0:
            relevant += 1

        if counter % 10 == 0:
            print("precision", relevant/counter)
            print("recall", relevant/100)
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