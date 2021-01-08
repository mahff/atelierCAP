package smartcard;

import java.util.List;
import javax.smartcardio.Card;
//import javax.smartcardio.*;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SCOperators {
	
	private static int i,j,n;
	
	public SCOperators() {
		
	}
	
//	PC/SC terminal Gemalto PC Twin Reader (B8CD86A8) 00 00
//	ATR :  3b000000 20000000 53000000 10000000
//	Lecture 1 (data) :  ffffffaa ffffffff ffffffff ffffffff
//	Lecture 2 (retour brut) :  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 ffffff90 00000000
//	Lecture 2 (data) :  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
//	Lecture 2 (code erreur) :  ffffff90 00000000
//	Lecture 3 (bytes) - carte V2 :  67000000 00000000
//	Verification 4 (CSC0) :  ffffff90 00000000
//	Update (CSC1) :  ffffff90 00000000
//	Verification 4 (CSC1 -- incorrect) :  63000000 00000000
//	Lecture compteur CSC1 :  00000000 00000000 00000000 ffffff80 ffffff90 00000000
//	Verification 4 (CSC1 -- incorrect 2è fois) :  63000000 00000000
//	Lecture compteur CSC1 :  00000000 00000000 00000000 ffffffc0 ffffff90 00000000
//	Verification 4 (CSC1 -- incorrect 3è fois) :  63000000 00000000
//	Lecture compteur CSC1 :  00000000 00000000 00000000 ffffffe0 ffffff90 00000000
//	Verification 4 (CSC1 -- correct) :  ffffff90 00000000
//	Lecture compteur CSC1 :  00000000 00000000 00000000 00000000 ffffff90 00000000

	static public String toString(byte[] byteTab){
        String returnString="";
        String hexNumber;
        
        for(i=0;i<byteTab.length;i++){
            hexNumber="";
            hexNumber=Integer.toHexString(byteTab[i]);
            if(hexNumber.length()<8){
                n=8-hexNumber.length();
                for(j=0;j<n;j++){
                   hexNumber+="0";
                }
            }
            returnString+=" "+hexNumber;
            
        }
        return returnString;
    }
	
	public static CardChannel connectToCard(CardTerminal terminal) throws CardException {
		Card carte = null;
		CardChannel channel = null;
		if(terminal.isCardPresent()) {
			carte = terminal.connect("T=0");
			System.out.println("ATR : "+SCOperators.toString(carte.getATR().getBytes()));//ATR (answer To Reset)
			channel = carte.getBasicChannel();
		}
		
		if(channel != null) {
			System.out.println("Connexion to SmartCard succeeded.");
		}
		return channel;
	}
	
	public static CardTerminals initReaders() throws CardException {
		int key=1;//O pour NFC, 1 pour smartcard
		
		CardTerminals readersToReturn = null;
		
		if(key==0){
        //Pour NFC
            System.out.println("test");
            List<CardTerminal> terminauxDispos = smartCard.getTerminals();
            System.out.println(terminauxDispos.size());
        }
        else if(key==1){
            //Pour smartcards
            // List<CardTerminal> ableSmartCardReaders = smartCard.getTerminals();
            CardTerminals availableSmartCardReaders = TerminalFactory.getDefault().terminals();
            
            if (availableSmartCardReaders.list().size() > 0) {
            	readersToReturn = availableSmartCardReaders;
            }
        }
		
		return readersToReturn;
	}
	
	public static String readCSC0(CardChannel channel) throws CardException {
		//Verifiy code CSC0
	    byte[] APDU4 = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x07 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
	    CommandAPDU commande4 = new CommandAPDU(APDU4);
	    ResponseAPDU response = channel.transmit(commande4);
//	    System.out.println("Verification 4 (CSC0) : " + toString(r4.getBytes()));
	    
		return toString(response.getData());
	}
	
	public static String updateCSC1(CardChannel channel) throws CardException {
		//Verifiy code CSC0
        byte[] APDU4 = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x07 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
        CommandAPDU verifySCS0Command = new CommandAPDU(APDU4);
        ResponseAPDU verifySCS0Response = channel.transmit(verifySCS0Command);
        System.out.println("Verification 4 (CSC0) : " + toString(verifySCS0Response.getBytes()));
        
        //update code CSC1 AA AA AA AA
        byte[] APDU5 = {(byte) 0x80,(byte) 0xDE ,(byte) 0x00 ,(byte) 0x38 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
        CommandAPDU updateCSC1Command = new CommandAPDU(APDU5);
        ResponseAPDU updateCSC1Response = channel.transmit(updateCSC1Command);
        System.out.println("Update (CSC1) : " + toString(updateCSC1Response.getBytes()));
		
        return toString(updateCSC1Response.getBytes());
	}

	public static String readCSCCounter(CardChannel channel, int counterIndex) throws CardException {	    
		byte wordAddr = 0x00;
		if(counterIndex == 0) {
			wordAddr = 0x07;
		} else if (counterIndex == 1) {
			wordAddr = 0x39;
		} else if (counterIndex == 2) {
			wordAddr = 0x3B;
		}
		
		CommandAPDU commande = new CommandAPDU(0x80,0xBE,0x00,wordAddr,0x04);
		ResponseAPDU r = channel.transmit(commande);
		System.out.println("Lecture compteur CSC " + counterIndex + " : " + toString(r.getBytes()));
		System.out.println("Lecture compteur CSC " + counterIndex + " : " + r.getBytes().toString());
		
		return toString(r.getBytes());
	}
	
//	public static String verifyCSC2(CardChannel channel) throws CardException {	    
//		//Verifiy code CSC1 -- correct
//		byte[] APDU = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x3b ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
//		CommandAPDU command = new CommandAPDU(APDU);
//		ResponseAPDU response = channel.transmit(command);
//		System.out.println("Verification 4 (CSC2 -- correct) : " + toString(response.getBytes()));
//		    
//		return toString(response.getBytes());
//	}
	
	public static String verifyCSC(CardChannel channel, int CSCIndex, byte D0, byte D1, byte D2, byte D3) throws CardException {	    
		//Verifiy code CSC1 -- correct
		byte wordAddr = 0x00;
		if(CSCIndex == 0) {
			wordAddr = 0x07;
		} else if (CSCIndex == 1) {
			wordAddr = 0x39;
		} else if (CSCIndex == 2) {
			wordAddr = 0x3B;
		} else {
			wordAddr = 0x3A;
		}
		
		byte[] APDU = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) wordAddr,(byte) 0x04,(byte) D0 , (byte) D1 , (byte) D2 ,(byte) D3};
		CommandAPDU command = new CommandAPDU(APDU);
		ResponseAPDU response = channel.transmit(command);
		System.out.println("\tVerify Operation return code : " + toString(response.getBytes()));
		    
		return toString(response.getBytes());
	}
	
	public static String updateCSC(CardChannel channel, int CSCIndex, byte D0, byte D1, byte D2, byte D3, byte newD0, byte newD1, byte newD2, byte newD3, byte csc0D0,byte csc0D1,byte csc0D2,byte csc0D3) throws CardException {
//		Verifiy code CSC0
		
		byte wordAddrToVerify = 0x00;
		byte wordAddrToUpdate = 0x00;
		if(CSCIndex == 0) {
			wordAddrToVerify = 0x07;
			wordAddrToUpdate = 0x06;
		} else if (CSCIndex == 1) {
			wordAddrToVerify = 0x39;
			wordAddrToUpdate = 0x38;
		} else if (CSCIndex == 2) {
			wordAddrToVerify = 0x3b;
			wordAddrToUpdate = 0x3a;
		}
		
        byte[] verifyAPDU = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) wordAddrToVerify ,(byte) 0x04,(byte) D0 , (byte) D1 , (byte) D2 ,(byte) D3};
        CommandAPDU verifySCSCommand = new CommandAPDU(verifyAPDU);
        ResponseAPDU verifySCSResponse = channel.transmit(verifySCSCommand);
        System.out.println("Verification (CSC" + CSCIndex + ") : " + toString(verifySCSResponse.getBytes()));
        
        //Read code CSC1
        byte[] readCSCAPDU = {(byte) 0x80,(byte) 0xBE ,(byte) 0x00 ,(byte) wordAddrToUpdate ,(byte) 0x04};
        CommandAPDU readCSCCommand = new CommandAPDU(readCSCAPDU);
        ResponseAPDU readCSCResponse = channel.transmit(readCSCCommand);
        System.out.println("Read (CSC " + CSCIndex + " ) : " + toString(readCSCResponse.getBytes()));
        
        byte[] verifyCSC0APDU = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 07 ,(byte) 0x04,(byte) csc0D0 , (byte) csc0D1 , (byte) csc0D2 ,(byte) csc0D3};
        CommandAPDU verifyCSC0Command = new CommandAPDU(verifyCSC0APDU);
        ResponseAPDU verifyCSC0Response = channel.transmit(verifyCSC0Command);
        System.out.println("Verification (CSC" + 0 + "for update lesser CSC) : " + toString(verifyCSC0Response.getBytes()));
        
        //update code CSC1 AA AA AA AA
        byte[] updateAPDU = {(byte) 0x80,(byte) 0xDE ,(byte) 0x00 ,(byte) wordAddrToUpdate ,(byte) 0x04,(byte) newD0 , (byte) newD1 , (byte) newD2 ,(byte) newD3};
        CommandAPDU updateCSCCommand = new CommandAPDU(updateAPDU);
        ResponseAPDU updateCSCResponse = channel.transmit(updateCSCCommand);
        System.out.println("Update (CSC " + CSCIndex + " ) : " + toString(updateCSCResponse.getBytes()));
		
      //Read code CSC1
        readCSCResponse = channel.transmit(readCSCCommand);
        System.out.println("Read Again (CSC " + CSCIndex + " ) : " + toString(readCSCResponse.getBytes()));
        
        return toString(updateCSCResponse.getBytes());
	}
	
	public static String readOperatingMode(CardChannel channel) throws CardException {
		CommandAPDU commande = new CommandAPDU(0x80,0xBE,0x00,0x04,0x04);
		ResponseAPDU r = channel.transmit(commande);
		System.out.println("Operating Mode : " + toString(r.getBytes()));
	    
		return toString(r.getBytes());
	}
		
	public static String writeAESKey(CardChannel channel, String aesKeyString) throws CardException {
		byte wordAddrToUpdate = 0x10;
		byte wordsSize = 0x18;
        
        byte[] updateUserAreaPrefixAPDU = {(byte) 0x80, (byte) 0xDE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) wordsSize};
        
        byte[] aesKeyBytesArray = aesKeyString.getBytes();
        
//        int byteSequenceLength = sequence.length;
//        int updateAPDUPrefixLength = updateUserAreaPrefixAPDU.length;
        
        byte[] updateUserAreaAPDU = new byte[aesKeyBytesArray.length + updateUserAreaPrefixAPDU.length];

        
        System.arraycopy(updateUserAreaPrefixAPDU, 0, updateUserAreaAPDU, 0, updateUserAreaPrefixAPDU.length);  
        System.arraycopy(aesKeyBytesArray, 0, updateUserAreaAPDU, updateUserAreaPrefixAPDU.length, aesKeyBytesArray.length);

        CommandAPDU updateUserAreaCommand = new CommandAPDU(updateUserAreaAPDU);
		ResponseAPDU r = channel.transmit(updateUserAreaCommand);
		
		System.out.println("Write AESKey : " + toString(r.getBytes()));
	    
		return toString(r.getBytes());
	}
	
	public static String writeUserName(CardChannel channel, String userNameString) throws CardException {
		byte wordAddrToUpdate = 0x28;
        
//		double sequenceSize = sequence.length;
		
		byte [] stringToWrite = new byte[64];
		
		byte[] userNameBytesArray = userNameString.getBytes();
		
		if( userNameBytesArray.length < 64 ) {
			System.arraycopy(userNameBytesArray, 0, stringToWrite, 0, userNameBytesArray.length);
			for(int k = userNameBytesArray.length; k < 64; k++) {
				stringToWrite[k] = (byte) 0x00;
			}
		} else {
			stringToWrite = userNameBytesArray;
//			System.arraycopy(sequence, 0, stringToWrite, 0, sequence.length);
		}
		
        byte[] updateUserAreaPrefixAPDU = {(byte) 0x80, (byte) 0xDE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x40};
        
        int updateAPDUPrefixLength = updateUserAreaPrefixAPDU.length;
        
        byte[] updateUserAreaAPDU = new byte[64 + updateAPDUPrefixLength];
//        byte[] updateUserAreaAPDU = {(byte) 0x80, (byte) 0xDE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x04}
        System.arraycopy(updateUserAreaPrefixAPDU, 0, updateUserAreaAPDU, 0, updateAPDUPrefixLength);  
        System.arraycopy(stringToWrite, 0, updateUserAreaAPDU, updateAPDUPrefixLength, 64);
//        System.out.println("first part apdu " + areaIndex + " : " + updateUserAreaPrefixAPDU);
//        System.out.println("second part apdu " + areaIndex + " : " + sequence);
//		CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
//        byte[] updateUserAreaAPDU2 = {(byte) 0x80, (byte) 0xDE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x04, (byte) 0x11,(byte) 0x22 ,(byte) 0x33 ,(byte) 0x44 };
        CommandAPDU updateUserAreaCommand = new CommandAPDU(updateUserAreaAPDU);
		ResponseAPDU r = channel.transmit(updateUserAreaCommand);
//		System.out.println("Write apdu " + areaIndex + " : " + r.toString());
//		System.out.println("Write in User " + areaIndex + " : " + toString(r.getBytes()));
	    
		return toString(r.getBytes());
	}
	
	public static byte[] readUserArea(CardChannel channel, int areaIndex, int DataLength) throws CardException {
		byte wordAddrToUpdate = 0x00;
		if(areaIndex == 1) {
			wordAddrToUpdate = 0x10;
		} else if (areaIndex == 2) {
			wordAddrToUpdate = 0x28;
		}
        
        byte[] readUserAreaAPDU = {(byte) 0x80, (byte) 0xBE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x40};

		//CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
        CommandAPDU readUserAreaCommand = new CommandAPDU(readUserAreaAPDU);
		ResponseAPDU r = channel.transmit(readUserAreaCommand);
		System.out.println("Read user " + areaIndex + ": " + toString(r.getBytes()));
	    
		return r.getBytes();
	}
	
	public static byte[] readAESKey(CardChannel channel) throws CardException {
		byte wordAddrToUpdate = 0x10;
        
        byte[] readUserAreaAPDU = {(byte) 0x80, (byte) 0xBE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x18};

		//CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
        CommandAPDU readUserAreaCommand = new CommandAPDU(readUserAreaAPDU);
		ResponseAPDU r = channel.transmit(readUserAreaCommand);
//		System.out.println("Read AESKey : " + toString(r.getBytes()));
	    
		return r.getBytes();
	}
	
	public static byte[] readUserName(CardChannel channel) throws CardException {
		byte wordAddrToUpdate = 0x28;
        
        byte[] readUserAreaAPDU = {(byte) 0x80, (byte) 0xBE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x40};

		//CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
        CommandAPDU readUserAreaCommand = new CommandAPDU(readUserAreaAPDU);
		ResponseAPDU r = channel.transmit(readUserAreaCommand);
//		System.out.println("Read user name: " + toString(r.getBytes()));
	    
		return r.getBytes();
	}
	
	public static String readAESKeyString(CardChannel channel) throws CardException {
		byte wordAddrToUpdate = 0x10;
        
        byte[] readUserAreaAPDU = {(byte) 0x80, (byte) 0xBE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x18};

		//CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
        CommandAPDU readUserAreaCommand = new CommandAPDU(readUserAreaAPDU);
		ResponseAPDU r = channel.transmit(readUserAreaCommand);
//		System.out.println("Read AESKey : " + toString(r.getBytes()));
	    byte[] toReturn = new byte[24];
	    
	    System.arraycopy(r.getBytes(), 0, toReturn, 0, 24);
	    
		return new String(toReturn, StandardCharsets.UTF_8);
	}
	
	public static String readUserNameString(CardChannel channel) throws CardException {
		byte wordAddrToUpdate = 0x28;
        
        byte[] readUserAreaAPDU = {(byte) 0x80, (byte) 0xBE, (byte) 0x00, (byte) wordAddrToUpdate, (byte) 0x40};

		//CommandAPDU updateUserAreaCommand = new CommandAPDU(0x80,0xDE,0x00,wordAddrToUpdate,0x40, sequence);
        CommandAPDU readUserAreaCommand = new CommandAPDU(readUserAreaAPDU);
		ResponseAPDU r = channel.transmit(readUserAreaCommand);
//		System.out.println("Read user name: " + toString(r.getBytes()));
	    
		byte[] toReturn = new byte[64];
	    
	    System.arraycopy(r.getBytes(), 0, toReturn, 0, 64);
	    
		return new String(toReturn, StandardCharsets.UTF_8);
	}
	
	public static String readAccessConditionsArea(CardChannel channel) throws CardException {
		CommandAPDU commande = new CommandAPDU(0x80,0xBE,0x00,0x05,0x04);
		ResponseAPDU r = channel.transmit(commande);
		System.out.println("Access Conditions Area : " + toString(r.getBytes()));
	    
		return toString(r.getBytes());
	}
}
