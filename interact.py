import subprocess



def fromjava() :
    p = subprocess.Popen(["java", "/home/mahff/eclipse-workspace/smartCard/bin/interprocess"], stdout=subprocess.PIPE)
    line = p.stdout.readline()
    while(line != "endl\n"):
        if line.startswith("fromjava".encode()) :
            return line
        line = p.stdout.readline()
    return None


def tojava(text) :
    process = subprocess.Popen(["java", "CLASS_PATH"], stdin=subprocess.PIPE)
    process.stdin.write("tojava "+text+"\r\n")
    process.stdin.write("endl\r\n")


def fromcpp():
    p = subprocess.Popen(["CPP_PATH", "CLASS_PATH"], stdout=subprocess.PIPE)
    line = p.stdout.readline()
    while(line != "endl\n"):
        if line.startswith("fromcpp".encode()) :
            return line
        line = p.stdout.readline()
    return None

def tocpp(txt) :
    process = subprocess.Popen(["CO_PATH", "CLASS_PATH"], stdin=subprocess.PIPE)
    process.stdin.write("tocpp "+text+"\r\n")
    process.stdin.write("endl\r\n")


print(fromjava())
