import mysql.connector
import random

class dbUtils():
    DATABASE = None
    CURSOR = None
    def __init__(self):

        dbUtils.DATABASE = mysql.connector.connect(host="localhost", user="root", password="", database="atelier")
        dbUtils.CURSOR = dbUtils.DATABASE.cursor()

    def getUserPassword(self, username) :
        try:
            statement = ("SELECT password FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                password = result[0]
                return password
            else:
                return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))


    def getUserSalt(self, username):
        try:
            salt = ""
            statement = ("SELECT salt FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                salt = result[0]
                return salt
            else:
                return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))
            return False

    def getUsername(self, username):
        try:
            salt = ""
            statement = ("SELECT username FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                username = result[0]
                return username
            else:
                return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))
            return False


    def addUser(self, username, password, salt, question1, answer1, question2, answer2, cipher, aessalt, nonce, tag):
        statement = "INSERT INTO userdata (username, password, salt, question1, answer1, question2, answer2, cipher, aessalt, nonce, tag) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
        try:
            data = (username, password, salt, question1, answer1, question2, answer2, cipher, aessalt, nonce, tag)
            dbUtils.CURSOR.execute(statement, data)
            dbUtils.DATABASE.commit()
            print(dbUtils.CURSOR.rowcount, "record inserted.")
        except mysql.connector.Error as error:
            return False

    def getRandomQuestion(self, username) :
        try:
            statement = ("SELECT question1, answer1, question2, answer2 FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                rand = random.randrange(0,2,1)
                print(rand)
                if rand == 0 :
                    arr = [result[0], result[1]]
                    return arr
                else:
                    arr = [result[2], result[3]]
                    return arr
            return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))
            return False

    def userExists(self, username):
        try:
            statement = ("SELECT username FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                return result[0]
            return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))
            return False

    def getUserAES(self, username) :
        try:
            statement = ("SELECT cipher, aessalt, nonce, tag FROM userdata WHERE username=%s")
            dbUtils.CURSOR.execute(statement, (username,))
            result = dbUtils.CURSOR.fetchone()
            if result :
                data = {
                    "cipher" : result[0],
                    "salt" : result[1],
                    "nonce" : result[2],
                    "tag" : result[3]
                }
                return data
            return False
        except mysql.connector.Error as error:
            print("Failed to get record from MySQL table: {}".format(error))
            return False

    def updateUserPassword(self, username, password, salt) :
        statement = ("UPDATE userdata SET password = %s, salt= %s WHERE username = %s")
        try:
            data = (password, salt, username)
            dbUtils.CURSOR.execute(statement, data)
            dbUtils.DATABASE.commit()
            print(dbUtils.CURSOR.rowcount, "record(s) affected")
            return True
        except mysql.connector.Error as error:
            return False

    def updateUserAES(self, username, cipher, aessalt, nonce, tag) :
        statement = ("UPDATE userdata SET cipher= %s, aessalt=%s, nonce=%s, tag=%s WHERE username = %s")
        try:
            data = (cipher, aessalt, nonce, tag, username)
            dbUtils.CURSOR.execute(statement, data)
            dbUtils.DATABASE.commit()
            print(dbUtils.CURSOR.rowcount, "record(s) affected")
            return True
        except mysql.connector.Error as error:
            return False

if __name__ == '__main__':
    db = dbUtils()
    print(db.getRandomQuestion("Redit")[1])
    print(db.getRandomQuestion("tech"))
