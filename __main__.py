from flask import Flask, render_template, request, redirect, url_for, flash, session

from client import *
from encryptClass import encryptClass

app = Flask(__name__)
app.secret_key = "super secret key"



enc = encryptClass()
@app.route('/', methods=['GET', 'POST'])
def index():
    name = None
    password = None
    data = None
    if request.method == 'POST':
        if 'name' in request.form and 'password' in request.form:
            name = request.form['name']
            password = request.form['password']
            if not name or name=="" :
                flash('Seems like you did not type your username !', 'danger')
                return redirect(request.url)
            if not password or password=="":
                flash('Seems like you did not type your password !', 'danger')
                return redirect(request.url)
            # get username from card
            if 'submit' in request.form :
                send("auth::"+name)
                data = receive()
                if data.startswith("authway") :
                    split = data.split("::")
                    if len(split) == 3 :
                        salt1 = split[1]
                        salt2 = split[2]
                        simple = enc.simpleHash(password, salt1)
                        doublehash = enc.doubleFactor(simple, salt2)
                        send("password::"+doublehash)
                        data = receive()
                        if data.startswith("confirmpass") :
                            return redirect("application.html")
                        else :
                            flash('Invalid password', 'danger')
                            return redirect(request.url)
                    else :
                        send("error")
                        flash('Invalid format', 'danger')
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
                    print(split)
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
    return render_template('changepass.html')

if __name__ == '__main__':
    connect()
    app.run(debug=True, use_reloader=False, ssl_context=('localhost.crt', 'localhost.key'))
