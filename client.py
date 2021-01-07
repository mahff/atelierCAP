import socket, sys


SOCK = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

def connect():
    server_address = ('localhost', 10000)
    print("Connecting to...", server_address)
    SOCK.connect(server_address)
    print("Connected to...", server_address)


def receive():
    data = SOCK.recv(1024).decode()
    print("Client received...", data)
    return data

def send(data):
    print("Sending...", data)
    SOCK.send(data.encode())
    print("Data sent...", data)

def disconnect():
    SOCK.close()
