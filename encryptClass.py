import hashlib
from Cryptodome.Cipher import AES
from Crypto.Random import get_random_bytes
from base64 import b64encode, b64decode

class encryptClass() :

    def generateHash(self):
        return hashlib.sha256().hexdigest()

    def simpleHash(self,password, salt):
        return hashlib.sha256((salt+"#"+password).encode()).hexdigest()


    def doubleFactor(self, simple, salt2):
        return hashlib.sha256((salt2+"#"+simple).encode()).hexdigest()

    def generateAES(self):
        plain_text = get_random_bytes(16)
        salt = get_random_bytes(16)
        private_key = hashlib.scrypt(
            plain_text, salt=salt, n=2**14, r=8, p=1, dklen=32)
        cipher_config = AES.new(private_key, AES.MODE_GCM)
        cipher_text, tag = cipher_config.encrypt_and_digest(plain_text)
        data =  {
            'cipher': b64encode(cipher_text).decode('utf-8'),
            'salt': b64encode(salt).decode('utf-8'),
            'nonce': b64encode(cipher_config.nonce).decode('utf-8'),
            'tag': b64encode(tag).decode('utf-8')
        }
        plain_text = b64encode(plain_text).decode('utf-8')
        return data, plain_text


    def decrytAES(self, enc_dict, password):
        decrypted = None
        try:
            salt = b64decode(enc_dict['salt'])
            cipher_text = b64decode(enc_dict['cipher'])
            nonce = b64decode(enc_dict['nonce'])
            tag = b64decode(enc_dict['tag'])
            password = b64decode(password)
            private_key = hashlib.scrypt(password, salt=salt, n=2**14, r=8, p=1, dklen=32)

            cipher = AES.new(private_key, AES.MODE_GCM, nonce=nonce)
            decrypted = cipher.decrypt_and_verify(cipher_text, tag)
        except ValueError:
            print("\nAn error has occurred.")
            return None
        return b64encode(decrypted).decode('utf-8')

if __name__ == '__main__':
    enc = encryptClass()
    # enchash = enc.generateHash()
    # print(enchash)
    # print(enc.simpleHash("123456",enchash))
    # print(enc.doubleFactor("123456", enchash, enchash))
    print(enc.generateHash())
    print(enc.generateHash())
    print(enc.generateHash())
    print(enc.generateHash())
    cipher, plain_text = enc.generateAES()
    print(cipher, plain_text)

    print(enc.decrytAES(cipher, plain_text))
