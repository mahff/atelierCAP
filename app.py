from flask import Flask, render_template, request, redirect, url_for, flash, session

from client import *
from encryptClass import encryptClass

app = Flask(__name__)
app.secret_key = "super secret key"

username = None

enc = encryptClass()
@app.route('/', methods=['GET', 'POST'])
def index():
    name = None
    password = None
    data = None
    aeskey = None
    biometry = None
    if request.method == 'POST':
        if 'name' in request.form and 'password' in request.form and 'aeskey' in request.form and 'biometry' in request.form:
            name = request.form['name']
            username = name
            password = request.form['password']
            aeskey = request.form['aeskey']
            biometry = request.form['biometry']
            if not name or name=="" :
                flash('Seems like you did not type your username !', 'danger')
                return redirect(request.url)
            if not password or password=="":
                flash('Seems like you did not type your password !', 'danger')
                return redirect(request.url)
            if not aeskey or aeskey=="":
                flash('Seems like you did not type your private AES key !', 'danger')
                return redirect(request.url)
            if not biometry or biometry=="":
                flash('Seems like you did not type your biometry !', 'danger')
                return redirect(request.url)

            if 'submit' in request.form :
                send("auth")
                data = receive()
                if data.startswith("identity") :
                    data = send("identity::"+name)
                    data = receive()
                if data.startswith("confirmid") :
                    split = data.split("::")
                    if len(split) == 3 :
                        simpleHash = enc.simpleHash(password, split[1])
                        doubleHash = enc.doubleFactor(simpleHash, split[2])
                        send("password::"+doubleHash)
                        data = receive()
                        if data.startswith("confirmpass")  :
                            send("iris::"+biometry)
                            data = receive()
                            if data.startswith("irisok") :
                                send("aes::"+aeskey)
                                data = receive()
                                if data.startswith("confirmaes"):
                                    return redirect("application.html")
                                else:
                                    return redirect("restricted.html")
                            elif data.startswith("denyiris") :
                                flash('Wrong Person', 'danger')
                                return redirect(request.url)
                        elif data.startswith("denypass") :
                            flash('Wrong password', 'danger')
                            return redirect(request.url)
                elif data.startswith("denyid") :
                    flash('Unkown user', 'danger')
                    return redirect(request.url)

        else:
            flash('Seems like you missed a field, please fill all of them ;)', 'danger')
            return redirect(request.url)
    return render_template('index.html', name=name, password=password)

@app.route('/register.html', methods=['GET', 'POST'])
def register() :
    name=None
    password=None
    repeat_pass=None
    question1=None
    answer1=None
    question2=None
    answer2 = None
    aessalt = None
    data = None
    if request.method == 'POST':
        if 'name' in request.form and 'password' in request.form and 'repeatpass' in request.form and 'question1' in request.form and 'answer1' in request.form and 'question2' in request.form and 'answer2' in request.form:
            name = request.form['name']
            username = name
            password = request.form['password']
            repeatpass = request.form['repeatpass']
            question1 = request.form['question1']
            answer1 = request.form['answer1']
            question2 = request.form['question2']
            answer2 = request.form['answer2']

            if len(name) >= 64 :
                flash('Username is too long', 'danger')
                return redirect(request.url)
            if not name or name=="" :
                flash('Seems like you did not type your username !', 'danger')
                return redirect(request.url)
            if not password or password=="":
                flash('Seems like you did not type your password !', 'danger')
                return redirect(request.url)
            if not repeatpass or repeatpass=="":
                flash('Seems like you did not retype your password !', 'danger')
                return redirect(request.url)
            if not question1 or question1=="":
                flash('Seems like you did not type your first question !', 'danger')
                return redirect(request.url)
            if not answer1 or answer1=="":
                flash('Seems like you did not retype your first Answer !', 'danger')
                return redirect(request.url)
            if not question2 or question2=="":
                flash('Seems like you did not type your second question !', 'danger')
                return redirect(request.url)
            if not answer2 or answer2=="":
                flash('Seems like you did not type your second Answer !', 'danger')
                return redirect(request.url)
            if password!=repeatpass and password!="" :
                flash('Seems like your password and repeat password do not match!', 'danger')
                return redirect(request.url)
            if 'submit' in request.form :
                send("register")
                data = receive()
                if(data.startswith("register")) :
                    split = data.split("::")
                    if len(split) == 3 :
                        aessalt = split[2]
                        simpleHash = enc.simpleHash(password, split[1])
                        send("account::"+name+"::"+simpleHash+"::"+question1+"::"+answer1+"::"+question2+"::"+answer2)
                        data = receive()
                    else:
                        send("error")
                        data = receive()
                if (data.startswith("userexists")) :
                    flash('Seems like you already have an account, did you forgot your password <a href="/formSubmit.html">Check Out This Form!</a>', 'account')
                    return redirect(request.url)
                elif (data.startswith("confirmregister")):
                    flash('SUCCESSS Username='+name+" AES Key="+aessalt, 'account')
                    return redirect('/')
        else :
            flash('Seems like you missed a field, please fill all of them ;)', 'danger')
            return redirect(request.url)
    return render_template('register.html', name=name, password=password, question1=question1, question2=question2, answer1=answer1, answer2=answer2)

@app.route('/application.html', methods=['GET', 'POST'])
def application() :
    if request.method == 'POST':
        if 'disconnect' in request.form :
            disconnect()
            return redirect('/')
    return render_template('application.html')

@app.route('/changepass.html', methods=['GET', 'POST'])
def changepass() :
    name = None
    data = None
    if request.method == 'POST':
        if 'name' in request.form:
            name = request.form['name']
            username = name
            print(name)
            if not name or name=="":
                flash('Seems like you did not type your username !', 'danger')
                return redirect(request.url)

            send("changepass::"+name)
            data = receive()
            if data.startswith("question"):
                question = data.split("::")[1]
                flash('Question :'+question, 'question')
                return redirect('confirmchange.html')
            elif data.startswith("denyuser") :
                flash('Invalid username !', 'danger')
                return redirect(request.url)
    return render_template('changepass.html')

@app.route('/restricted.html', methods=['GET', 'POST'])
def restricted() :
    return render_template('restricted.html')


@app.route('/confirmchange.html', methods=['GET', 'POST'])
def confirmchange() :
    password = None
    response = None
    biometry = None
    data = None
    if 'password' in request.form and 'response' in request.form and 'biometry' in request.form:
        password = request.form['password']
        response = request.form['response']
        biometry = request.form['biometry']

        if not password or password=="":
            flash('Seems like you did not type your password !', 'danger')
            return redirect(request.url)
        if not response or response=="":
            flash('Seems like you did not type your response!', 'danger')
            return redirect(request.url)
        if not biometry or biometry=="":
            flash('Seems like you did not type your biometry !', 'danger')
            return redirect(request.url)
        send("answerquestion::"+response)
        data = receive()
        if data.startswith("denychange"):
            flash('Wrong response !', 'danger')
            return redirect("changepass.html")
        elif data.startswith("passsalt"):
            salt = data.split("::")[1]
            simpleHash = enc.simpleHash(password, salt)
            send("iris::"+biometry)
            data = receive()
            if data.startswith("irisok") :
                send("passhash::"+simpleHash)
                data = receive()
                if data.startswith("changeok"):
                    return redirect("/")
            elif data.startswith("denyiris") :
                flash('Wrong Person', 'danger')
                return redirect("changepass.html")
    return render_template('confirmchange.html')


@app.route('/changecard.html', methods=['GET', 'POST'])
def changecard() :
    name = None
    data = None
    if request.method == 'POST':
        if 'name' in request.form:
            name = request.form['name']
            username = name
            print(name)
            if not name or name=="":
                flash('Seems like you did not type your username !', 'danger')
                return redirect(request.url)

            send("changecard::"+name)
            data = receive()
            if data.startswith("question"):
                question = data.split("::")[1]
                flash('Question :'+question, 'question')
                return redirect('confirmcard.html')
    return render_template('changecard.html')

@app.route('/confirmcard.html', methods=['GET', 'POST'])
def confirmcard() :
    password = None
    response = None
    biometry = None
    data = None
    if 'password' in request.form and 'response' in request.form and 'biometry' in request.form:
        password = request.form['password']
        response = request.form['response']
        biometry = request.form['biometry']
        if not password or password=="":
            flash('Seems like you did not type your password !', 'danger')
            return redirect(request.url)
        if not response or response=="":
            flash('Seems like you did not type your response!', 'danger')
            return redirect(request.url)
        if not biometry or biometry=="":
            flash('Seems like you did not type your biometry !', 'danger')
            return redirect(request.url)
        send("cardanswer::"+response)
        data = receive()
        if data.startswith("password") :
            split = data.split("::")
            simpleHash = enc.simpleHash(password, split[1])
            doubleHash = enc.doubleFactor(simpleHash, split[2])
            send("password::"+doubleHash)
            data = receive()
            if data.startswith("confirmpass"):
                send("iris::"+biometry)
                data = receive()
                if data.startswith("irisok") :
                    send("confirmchange")
                    data = receive()
                    if data.startswith("confirmchange"):
                        aessalt = data.split("::")[1]
                        flash('SUCCESSS AES Key='+aessalt, 'account')
                        return redirect('/')
                    elif data.startswith("denychange"):
                        flash('Wrong Person', 'danger')
                        return redirect("changecard.html")
                elif data.startswith("denyiris") :
                    flash('Wrong Person', 'danger')
                    return redirect("/")
            elif data.startswith("denypass"):
                flash('Wrong password', 'danger')
                return redirect("changecard.html")
    return render_template('confirmcard.html')


if __name__ == '__main__':
    connect()
    app.run(debug=True, use_reloader=False, ssl_context=('localhost.crt', 'localhost.key'))
