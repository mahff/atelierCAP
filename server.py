import socket
import sys
from dbUtils import dbUtils
from encryptClass import encryptClass

enc = encryptClass()
db = dbUtils()


sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

server_address = ('localhost', 10000)
sock.bind(server_address)

sock.listen(1)

aessalt = None
aes = None
plain_text = None

salt1 = None
salt2 = None
username = None
password = None
answer = None

connection = None

def receive():
    data = connection.recv(1024).decode()
    print("Server received...", data)
    return data

def send(data):
    print("Sending...", data)
    connection.send(data.encode())
    print("Data sent...", data)

while True :
    connection, client_address = sock.accept()
    try:
        print (sys.stderr, 'connection from', client_address)
        while True:
            data = receive()
            if data:

                if data.startswith("auth"):
                    send("identity")

                if data.startswith("register") :
                    salt = enc.generateHash()
                    aes, plain_text = enc.generateAES()
                    textformat = "register::"+salt+"::"+plain_text
                    send(textformat)

                if data.startswith("account") :
                    split = data.split("::")
                    if not db.addUser(split[1], split[2], salt, split[3], split[4], split[5], split[6], aes['cipher'], aes['salt'], aes['nonce'], aes['tag']):
                        send("confirmregister")
                    else:
                        send("userexists")

                if data.startswith("identity") :
                    split = data.split("::")
                    username = split[1]
                    user = db.userExists(username)
                    if not user :
                        send("denyid")

                    else:
                        salt1 = db.getUserSalt(username)
                        password = db.getUserPassword(username)
                        print("Salt1 ...", salt1)
                        salt2 = enc.generateHash()
                        print("Salt2...", salt2)
                        # send who are you + salt2
                        textformat = "confirmid::"+salt1+"::"+salt2
                        send(textformat)

                if data.startswith("iris") :
                    biometry = data.split("::")[1]
                    if biometry == "SUCCESS":
                        send("irisok")
                    else :
                        send("denyiris")
                if data.startswith("password") :
                    userhash = data.split("::")[1]
                    mhash = enc.doubleFactor(password, salt2)
                    print("Hashes...", userhash, mhash)
                    if userhash==mhash:
                        send("confirmpass")
                    else:
                        send("denypass")

                if data.startswith("aes") :
                    aes = data.split("::")[1]
                    aesdata = db.getUserAES(username)
                    if not aesdata :
                        send("denyaes")
                    else:
                        aesdecrypt = enc.decrytAES(aesdata, aes)
                        if aes == aesdecrypt:
                            send("confirmaes")
                        else:
                            send("denyaes")
                if data.startswith("changepass") or data.startswith("changecard"):
                    username = data.split("::")[1]
                    question = db.getRandomQuestion(username)
                    if question :
                        answer = question[1]
                        send("question::"+question[0])
                    else:
                        send("denyuser")
                if data.startswith("answerquestion") :
                    useranswer = data.split("::")[1]
                    if useranswer == answer :
                        salt = enc.generateHash()
                        send("passsalt::"+salt)
                    else:
                        send("denychange")
                if data.startswith("passhash") :
                    pswd = data.split("::")[1]
                    db.updateUserPassword(username, pswd, salt)
                    send("changeok")
                if data.startswith("cardanswer"):
                    useranswer = data.split("::")[1]
                    if useranswer == answer :
                        salt1 = db.getUserSalt(username)
                        password = db.getUserPassword(username)
                        print("Salt1 ...", salt1)
                        salt2 = enc.generateHash()
                        print("Salt2...", salt2)
                        # send who are you + salt2
                        textformat = "password::"+salt1+"::"+salt2
                        send(textformat)
                    else:
                        send("denychange")
                if data.startswith("confirmchange"):
                    aes, plain_text = enc.generateAES()
                    if not db.updateUserAES(username, aes['cipher'], aes['salt'], aes['nonce'], aes['tag']):
                        send("denychange")
                    else :
                        send("confirmchange::"+plain_text)


            else:
                print (sys.stderr, 'no more data from', client_address)
                connection.close()

    finally:
        connection.close()
