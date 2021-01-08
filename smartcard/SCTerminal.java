package smartcard;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

// Les cartes semblent toutes être en user mode

public class SCTerminal {
	private static CardTerminal terminal;
    private static Card carte;
    private static String text=new String();
    
        
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
            // List<CardTerminal> ableSmartCardReaders = smartCard.getTerminals();
            CardTerminals availableSmartCardReaders = TerminalFactory.getDefault().terminals();
            
            while(availableSmartCardReaders.list().size() > 0) {
            	availableSmartCardReaders.waitForChange();
                
                List<CardTerminal> insertedCardEventSmartCardReaders = availableSmartCardReaders.list(CardTerminals.State.CARD_INSERTION);
                System.out.println(insertedCardEventSmartCardReaders);
                
                
                if(insertedCardEventSmartCardReaders.size() > 0) {
                	insertedCardEventSmartCardReaders.forEach((smartCardReader) -> {
                        System.out.println(smartCardReader);
                        System.out.println(smartCardReader.getName());
                        System.out.println(smartCardReader.toString());
                        
                        try {
                        	//Connexion à la carte
//                            availableSmartCardReaders.getTerminal(smartCardReader.getName());
        					carte = smartCardReader.connect("T=0");
        					System.out.println("ATR : "+SCOperators.toString(carte.getATR().getBytes()));//ATR (answer To Reset)
        					CardChannel channel = carte.getBasicChannel();
//        	                SCOperators.read1(channel);
        	                System.out.println(text);
        	                
//        	                SCOperators.read1(channel);
        	                
//        	                SCOperators.readCSC1Counter(channel);
//        	                SCOperators.readCSC2Counter(channel);
        	                SCOperators.readCSCCounter(channel, 0);
        	                SCOperators.readCSCCounter(channel, 1);
        	                SCOperators.readCSCCounter(channel, 2);
        	                SCOperators.readOperatingMode(channel);
        	                
        	                System.out.println("====== verify csc begin ======");
        	                SCOperators.verifyCSC(channel, 1, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11);
        	                System.out.println("====== verify csc end ======");
        	                
        	                System.out.println("====== read userarea 1 begin ======");
        	                SCOperators.readUserArea(channel, 1, 1);
        	                System.out.println("====== read userarea 1 end ======");
        	                
        	                byte[] sequence = {(byte) 0x1a,(byte) 0x2b ,(byte) 0x3c ,(byte) 0x4d};
//        	                SCOperators.verifyCSC(channel, 1, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11);
        	                
        	                
        	                SCOperators.verifyCSC(channel, 0, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
        	                String AESKey = "rdjaO+peSaZ7A18uOn1dwA==";
        	                
        	                sequence = AESKey.getBytes();
        	                
//        	                SCOperators.writeInUserArea(channel, 1, sequence);
        	                SCOperators.writeAESKey(channel, AESKey);
        	                byte[] hexStr = SCOperators.readUserArea(channel, 1, 1);
        	                
        	                String s = new String(hexStr, StandardCharsets.UTF_8);
        	                
        	                System.out.println("TEST STRING : " + s);
        	                
        	                SCOperators.writeUserName(channel, "Amaury@Siharath");
        	                
        	                System.out.println(SCOperators.readAESKey(channel));
        	                System.out.println(SCOperators.readAESKeyString(channel));
        	                
        	                System.out.println(SCOperators.readUserName(channel));
        	                System.out.println(SCOperators.readUserNameString(channel));
        	                
//        	                byte[] hexStr2 = SCOperators.readUserArea(channel, 2, 1);
//        	                
//        	                String s2 = new String(hexStr2, StandardCharsets.UTF_8);
//        	                
//        	                System.out.println("TEST STRING : " + s2);
//        	                System.out.println("TEST STRING : " + s2.length());
//        	                System.out.println("TEST STRING : " + hexStr2.length);
//        	                
//        	                byte[] hexStr3 = new byte[64];
//        	                
//        	                System.arraycopy(hexStr2, 0, hexStr3, 0, 64);
//        	                
//        	                String s3 = new String(hexStr3, StandardCharsets.UTF_8);
//        	                
//        	                System.out.println("TEST STRING : " + s3);
//        	                System.out.println("TEST STRING : " + s3.length());
//        	                
//        	                System.out.println("====== update csc begin ======");
//        	                SCOperators.updateCSC(channel, 1, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa);
//        	                System.out.println("====== update csc end ======");
        	                
//        	                SCOperators.readAccessConditionsArea(channel);
        	                
//        	                SCOperators.verifyCSC1(channel);
//        	                SCOperators.verifyCSC2(channel);
//        	                
//        	                SCOperators.readCSC1Counter(channel);
//        	                SCOperators.readCSC2Counter(channel);
//        	                
//        	                SCOperators.updateCSC2(channel);
//        	                SCOperators.updateCSC1(channel);
//        	                
//        	                SCOperators.readCSC1Counter(channel);
//        	                SCOperators.readCSC2Counter(channel);
        	                /*
        	                SCOperators.updateCSC2(channel);
        	                
        	                SCOperators.verifyCSC1(channel);
        	                SCOperators.verifyCSC2(channel);
        	                */
        	                // System.out.println("test");
        	                
        	                carte.disconnect(false);
        				} catch (CardException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                        
                    });
                }
            }
        }
    }
}
