package smartcard;

import java.util.List;
import javax.smartcardio.Card;
//import javax.smartcardio.*;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
//import java.javax.smartcardio;


/**
 *
 * @author Guillaume
 */
public class smartCard {
    private static CardTerminal terminal;
    private static Card carte;
    private static int i,j,n;
    private static String text=new String();
    
    static public List<CardTerminal> getTerminals() throws CardException {
        return TerminalFactory.getDefault().terminals().list();

    }
    
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
    
    public static void main(String[] args) throws CardException {
        int key=1;//O pour NFC, 1 pour smartcard
        if(key==0){
        //Pour NFC
	        System.out.println("test");
	        List<CardTerminal> terminauxDispos = smartCard.getTerminals();
	        System.out.println(terminauxDispos.size());
        }
        else if(key==1){
	        //Pour smartcards
	        List<CardTerminal> terminauxDispos = smartCard.getTerminals();
	        //Connexion au premier lecteur de carte
	        terminal = terminauxDispos.get(0);
	        
	        System.out.println(terminal.toString());
	        carte = terminal.connect("T=0");//Connexion à la carte
	        System.out.println("ATR : "+toString(carte.getATR().getBytes()));//ATR (answer To Reset)
	  
	        CardChannel channel = carte.getBasicChannel();//Connexion à la carte
	        
	        //Lecture 1
	        CommandAPDU commande1 = new CommandAPDU(0x80,0xBE,0x00,0x00,0x04);
	        ResponseAPDU r1 = channel.transmit(commande1);
	        System.out.println("Lecture 1 (data) : " + toString(r1.getData()));
	        
	        //Lecture 2
	        byte[] APDU = {(byte) 0x80,(byte) 0xBE ,(byte) 0x00 ,(byte) 0x01 ,(byte) 0x08};
	        CommandAPDU commande2 = new CommandAPDU(APDU);
	        ResponseAPDU r2 = channel.transmit(commande2);
	        System.out.println("Lecture 2 (retour brut) : " + toString(r2.getBytes()));
	        System.out.println("Lecture 2 (data) : " + toString(r2.getData()));
	        byte[] bytesTab = {(byte) r2.getSW1(),(byte) r2.getSW2()};
	        System.out.println("Lecture 2 (code erreur) : " + toString(bytesTab));
	        
	        //Lecture 3 : Lecture Commande : ‘Get Chip ID Info’	
	        CommandAPDU commande3 = new CommandAPDU(0x80,0xC0,0x00,0x00,0x08);
	        ResponseAPDU r3 = channel.transmit(commande3);
	        System.out.println("Lecture 3 (bytes) - carte V2 : " + toString(r3.getBytes()));
	        
	      //Lecture 3 bis
	        CommandAPDU commande3bis = new CommandAPDU(0x80,0xBE,0x00,0x00,0x08);
	        ResponseAPDU r3bis = channel.transmit(commande3bis);
	        System.out.println("Lecture 3 (bytes) - carte V2 : " + toString(r3bis.getBytes()));
	        
	        //Verifiy code CSC0
	        byte[] APDU4 = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x07 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
	        CommandAPDU commande4 = new CommandAPDU(APDU4);
	        ResponseAPDU r4 = channel.transmit(commande4);
	        System.out.println("Verification 4 (CSC0) : " + toString(r4.getBytes()));
	        
	        //update code CSC1 AA AA AA AA
	        byte[] APDU5 = {(byte) 0x80,(byte) 0xDE ,(byte) 0x00 ,(byte) 0x38 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
	        CommandAPDU commande5 = new CommandAPDU(APDU5);
	        ResponseAPDU r5 = channel.transmit(commande5);
	        System.out.println("Update (CSC1) : " + toString(r5.getBytes()));
	        
	        //Verifiy code CSC1 -- incorrect (AB au lieu de AA)
	        byte[] APDU7 = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x39 ,(byte) 0x04,(byte) 0xab , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
	        CommandAPDU commande7 = new CommandAPDU(APDU7);
	        ResponseAPDU r7 = channel.transmit(commande7);
	        System.out.println("Verification 4 (CSC1 -- incorrect) : " + toString(r7.getBytes()));
	        
	        CommandAPDU commande = new CommandAPDU(0x80,0xBE,0x00,0x39,0x04);
	        ResponseAPDU r = channel.transmit(commande);
	        System.out.println("Lecture compteur CSC1 : " + toString(r.getBytes()));
	        
	        r7 = channel.transmit(commande7);
	        System.out.println("Verification 4 (CSC1 -- incorrect 2è fois) : " + toString(r7.getBytes()));
	        
	        commande = new CommandAPDU(0x80,0xBE,0x00,0x39,0x04);
	        r = channel.transmit(commande);
	        System.out.println("Lecture compteur CSC1 : " + toString(r.getBytes()));
	        
	        r7 = channel.transmit(commande7);
	        System.out.println("Verification 4 (CSC1 -- incorrect 3è fois) : " + toString(r7.getBytes()));
	        
	        commande = new CommandAPDU(0x80,0xBE,0x00,0x39,0x04);
	        r = channel.transmit(commande);
	        System.out.println("Lecture compteur CSC1 : " + toString(r.getBytes()));
	        
	        
	        
	        //Verifiy code CSC1 -- correct
	        byte[] APDU6 = {(byte) 0x00,(byte) 0x20 ,(byte) 0x00 ,(byte) 0x39 ,(byte) 0x04,(byte) 0xaa , (byte) 0xaa , (byte) 0xaa ,(byte) 0xaa};
	        CommandAPDU commande6 = new CommandAPDU(APDU6);
	        ResponseAPDU r6 = channel.transmit(commande6);
	        System.out.println("Verification 4 (CSC1 -- correct) : " + toString(r6.getBytes()));
	        
	        commande = new CommandAPDU(0x80,0xBE,0x00,0x39,0x04);
	        r = channel.transmit(commande);
	        System.out.println("Lecture compteur CSC1 : " + toString(r.getBytes()));
	        
	        
	      
	        System.out.println(text);
	        carte.disconnect(false);
        }
        
        

    }
}