

package smartcard;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

public class SCPrompt {
	public static void main(String[] args) throws CardException {
		
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);  // Create a Scanner object
//		String userInput = null;
//		Card carte;
		String text=new String();
		
		CardTerminals availableSmartCardReaders = SCOperators.initReaders();
		if(availableSmartCardReaders.list().size() > 0) {
			CardTerminal currentTerminal = availableSmartCardReaders.list().get(0);
			if(!currentTerminal.isCardPresent()) {
				availableSmartCardReaders.waitForChange();
			}
            
            List<CardTerminal> insertedCardEventSmartCardReaders = availableSmartCardReaders.list(CardTerminals.State.CARD_INSERTION);
            System.out.println(insertedCardEventSmartCardReaders);
            
			if(insertedCardEventSmartCardReaders.size() > 0) {
				insertedCardEventSmartCardReaders.forEach((smartCardReader) -> {
//            		Scanner scanner = null;  // Create a Scanner object
	        		String userInput = null;
	        		CardChannel channel = null;
//	        		Card carte = null;
	                System.out.println(smartCardReader);
	                System.out.println(smartCardReader.getName());
	                System.out.println(smartCardReader.toString());
	                
	                try {
	                	channel = SCOperators.connectToCard(smartCardReader);
	//    	                SCOperators.read1(channel);
		                System.out.println(text);
	
					} catch (CardException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	                do {
	    				do {
	    					System.out.println("==== SmartCard Terminal Prompt ====");
	    					System.out.println("Main menu");
	    					
	    					System.out.println("\t1. Write AES Key on available card, type 1");
	    					System.out.println("\t2. Write user name on available card, type 2");
	    					System.out.println("\t3. Read AES Key on available card, type 3");
	    					System.out.println("\t4. Read user name on available card, type 4");
	    					
	    					System.out.println("To leave, type exit");
	    					
	    					userInput = scanner.nextLine().trim(); // remove stray leading/trailing whitespace
	    				} while (userInput.isEmpty());// keep asking for input if a blank line is read
	    				
	    				switch(userInput) {
	    				case "1":
	                			System.out.println("==== SmartCard Terminal Prompt ====");
	                			Scanner AESKeyScanner = new Scanner(System.in);
	                			
	                			String AESKeyInput = AESKeyScanner.nextLine().trim(); // remove stray leading/trailing whitespace
	                			if (AESKeyInput.length() > 0 && AESKeyInput.length() <= 24) {
	                				try {
	                					SCOperators.verifyCSC(channel, 0, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
	                					SCOperators.writeAESKey(channel,AESKeyInput);
	                				} catch (CardException e) {
	    	  							// TODO Auto-generated catch block
	                					try {
	    									if(!currentTerminal.isCardPresent()) {
	    										System.out.println("Please, insert a usable Smart Card.");
	    										currentTerminal.waitForCardPresent(0);
	    										if(currentTerminal.isCardPresent()) {
	    											channel = SCOperators.connectToCard(currentTerminal);
	    										}
	    									}
	    								} catch (CardException e1) {
	    									// TODO Auto-generated catch block
	    									e1.printStackTrace();
	    								}
	    	  						}
	                			} else {
	                				System.out.println("Please type non empty string of maximum 24 characters.");
	                			}
	    				  break;
	    				case "2":
	    					System.out.println("==== SmartCard Terminal Prompt ====");
                			Scanner userNameScanner = new Scanner(System.in);
                			
                			String userNameInput = userNameScanner.nextLine().trim(); // remove stray leading/trailing whitespace
                			if (userNameInput.length() > 0 && userNameInput.length() <= 64) {
                				try {
                					SCOperators.verifyCSC(channel, 0, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
                					SCOperators.writeUserName(channel,userNameInput);
                				} catch (CardException e) {
    	  							// TODO Auto-generated catch block
                					try {
    									if(!currentTerminal.isCardPresent()) {
    										System.out.println("Please, insert a usable Smart Card.");
    										currentTerminal.waitForCardPresent(0);
    										if(currentTerminal.isCardPresent()) {
    											channel = SCOperators.connectToCard(currentTerminal);
    										}
    									}
    								} catch (CardException e1) {
    									// TODO Auto-generated catch block
    									e1.printStackTrace();
    								}
    	  						}
                			} else {
                				System.out.println("Please type non empty string of maximum 64 characters.");
                				
                			}
	    				  break;
	    				case "3":
	              			try {
	              				SCOperators.verifyCSC(channel, 0, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
//	  							System.out.println(SCOperators.readAESKey(channel));
	  							System.out.println("\tAES Key value on card : " + SCOperators.readAESKeyString(channel) + "\n\n");
	  						} catch (CardException e) {
	  							try {
									if(!currentTerminal.isCardPresent()) {
										System.out.println("Please, insert a usable Smart Card.");
										currentTerminal.waitForCardPresent(0);
										if(currentTerminal.isCardPresent()) {
											channel = SCOperators.connectToCard(currentTerminal);
										}
									}
								} catch (CardException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	  						}
	              		      break;
	              		case "4":
	              			try {
	              				SCOperators.verifyCSC(channel, 0, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
//	                  			System.out.println(SCOperators.readUserName(channel));
	          	                System.out.println("\tUser Name on card : " + SCOperators.readUserNameString(channel) + "\n\n");
	              			} catch (CardException e) {
	  							// TODO Auto-generated catch block
	              				try {
									if(!currentTerminal.isCardPresent()) {
										System.out.println("Please, insert a usable Smart Card.");
										currentTerminal.waitForCardPresent(0);
										if(currentTerminal.isCardPresent()) {
											channel = SCOperators.connectToCard(currentTerminal);
										}
									}
								} catch (CardException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	  						}
	              		      break;
	    				case "exit":
	    					  // code block
	    						System.exit(0);
	    					      break;
	    				default:
	    				  // code block
	    					choice("FUCK");
	    				  }
	    			} while ((!userInput.equals("exittt")));
				});
			}
            
		}
	}
	
	public static void choice(String arg) {
		System.out.println("CHOICE with : " + arg);
	}
	
}

