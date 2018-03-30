import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class txtblk {

  private HashMap<String, Long> userToBalanceMap = new HashMap<>();
  private LinkedHashMap<String, Transaction> txnChain = new LinkedHashMap<>();
  private static boolean isExit = false;
  private static boolean isInteractive = false;
  private boolean isGenesisTransaction = true;
  private static boolean isVerbose = false;

  public static void main(String[] args) {
    txtblk tb = new txtblk();
    Scanner sc = new Scanner(System.in);
    while (!isExit) {
      if (isInteractive) {
        System.out.println("[F]ile");
        System.out.println("[T]ransaction");
        System.out.println("[P]rint");
        System.out.println("[H]elp");
        System.out.println("[D]ump");
        System.out.println("[W]ipe");
        System.out.println("[I]nteractive");
        System.out.println("[V]erbose");
        System.out.println("[B]alance");
        System.out.println("[O]utput transaction block");
        System.out.println("[C]heck transaction signature");
        System.out.println("[R]ead key file");
        System.out.println("[E]xit");
        System.out.print("Select a command: ");
      }
      String s = sc.nextLine();
      s = s.toLowerCase();
      switch (s) {
        case "f":
        case "file":
          System.out.print("Supply file name: ");
          String fileName = sc.nextLine();
          try {
            FileReader fileReader = new FileReader(fileName);
            Scanner fileScanner = new Scanner(fileReader);
            try {
              while (fileScanner.hasNext()) {
                String t = fileScanner.nextLine();
                String sig = fileScanner.nextLine();
                tb.validateAndAddTransaction(t, sig);
              }
            } catch (Exception e) {
              System.err.println("Error: " + e);
            }

          } catch (FileNotFoundException e) {
            System.err.println("Error: file " + fileName + " cannot be opened for reading");
          }
          break;

        case "t":
        case "transaction":
          System.out.print("Enter Transaction: ");
          String t = sc.nextLine();
          String sig = sc.nextLine();
          tb.validateAndAddTransaction(t, sig);
          break;

        case "p":
        case "print":
          tb.printLedger();
          break;

        case "h":
        case "help":
          System.out.println(
              "[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be 'Error: file <infilename> cannot be opened for reading' on a single line, where <infilename> is the name provided as additional command input.  \r\n"
                  + "\r\n"
                  + "[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\r\n"
                  + "\r\n" + "[E]xit:  Quit the program\r\n" + "\r\n"
                  + "[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\r\n"
                  + "\r\n" + "[H]elp:  Command Summary\r\n" + "\r\n"
                  + "[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be 'Error: file <outfilename> cannot be opened for writing' on a single line, where <outfilename> is the name provided as additional command input. \r\n"
                  + "\r\n" + "[W]ipe:  Wipe the entire ledger to start fresh.\r\n" + "\r\n"
                  + "[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\r\n"
                  + "\r\n"
                  + "[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result ('good' or 'bad'). \r\n"
                  + "\r\n"
                  + "[B]alance:  Supply username: (e.g. Alice).  This command prints the current balance of a user.    \r\n"
                  + "\r\n" + "Format of Transactions:\r\n"
                  + "<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N \r\n"
                  + "Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. \r\n"
                  + "");
          break;

        case "d":
        case "dump":
          System.out.print("Supply file name: ");
          String outputFileName = sc.nextLine();
          try {
            FileWriter fw = new FileWriter(new File(outputFileName));
            tb.dumpLedger(fw);
            fw.close();
          } catch (IOException e) {
            System.err.println("Error: file " + outputFileName + " cannot be opened for writing");
            e.printStackTrace();
          }
          break;

        case "w":
        case "wipe":
          tb = new txtblk();
          break;

        case "i":
        case "interactive":
          isInteractive = !isInteractive;
          break;

        case "v":
        case "verbose":
          isVerbose = !isVerbose;
          break;

        case "b":
        case "balance":
          System.out.print("Enter User: ");
          String user = sc.nextLine();
          if (tb.userToBalanceMap.containsKey(user)) {
            System.out.println(user + " has " + tb.userToBalanceMap.get(user));
          } else {
            System.out.println(user + " does not exist on this ledger");
          }
          break;

        case "o":
        case "output transaction block":
          System.out.println("outputting transaction block...");
          break;

        case "c":
        case "check transaction signature":
          System.out.println("checking transaction signature...");
          break;

        case "r":
        case "read key file":
          System.out.println("reading key file...");
          break;

        case "e":
        case "exit":
          isExit = true;
          System.out.println("Good-bye");
          break;

        default:
          System.out.println("Please enter a valid command.");
          break;
      }
    }
    sc.close();
  }

  private void dumpLedger(FileWriter fw) throws IOException {
    for (Entry<String, Transaction> s : txnChain.entrySet()) {
      fw.write(s.getValue().getTransactionString());
      fw.write(System.lineSeparator());
    }
  }

  private void printLedger() {
    if (txnChain.isEmpty()) {
      System.out.println("The ledger is currently empty");
    } else {
      for (Entry<String, Transaction> s : txnChain.entrySet()) {
        System.out.println(s.getValue().getTransactionString());
      }
    }
  }

  private void validateAndAddTransaction(String inputTxn, String inputSignature) {
    inputTxn = inputTxn.replaceAll(" ", "").replaceAll(";", "; ").replaceAll(",", ", ");
    int indexofFirstSemiColon = inputTxn.indexOf("; ");
    String inputTxnID = inputTxn.substring(0, indexofFirstSemiColon);
    String givenTxnID = inputTxnID;
    if (!isValidTxnId(givenTxnID)) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID
          + ": transaction Id should be 8 characters long and contain either characters between a and f or digits");
      return;
    }
    if (indexofFirstSemiColon + 2 >= inputTxn.length()) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": transaction is not formatted correctly");
      if (isVerbose) {
        System.err.println(givenTxnID
            + " : Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
      }
      return;
    }
    try {
      String hashedTxnId = isValidSHA1TxnId(inputTxn, indexofFirstSemiColon);
      if (!hashedTxnId.equals(inputTxnID)) {
        if (isVerbose) {
          System.err
              .println(givenTxnID + " : The transaction ID you have input is invalid. Resetting to "
                  + hashedTxnId + " to match the correct SHA1 hash.");
        }
        inputTxn =
            hashedTxnId + "; " + inputTxn.substring(indexofFirstSemiColon + 2, inputTxn.length());
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    String[] txnSplitArray = inputTxn.split(";");
    if (txnSplitArray.length < 5) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": transaction is not formatted correctly");
      if (isVerbose) {
        System.err.println(givenTxnID
            + " : Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
      }
      return;
    }
    inputTxnID = txnSplitArray[0].trim();
    int inputSize = Integer.parseInt(txnSplitArray[1].trim());
    if ((isGenesisTransaction && inputSize != 0)) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(
          givenTxnID + ": the number of input UTXOs for a genesis transaction should be 0");
      return;
    }
    if (!isGenesisTransaction && inputSize < 1) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID
          + ": the number of input UTXOs for a non-genesis transaction should be greater than 0");
      return;
    }
    ArrayList<TxnIdIndexPair> inputUTXOs = new ArrayList<>();
    txnSplitArray[2] = txnSplitArray[2].trim();
    int left = 0, right = 0;
    // if (txnSplitArray[2].charAt(left) != '('
    // || txnSplitArray[2].charAt(txnSplitArray[2].length() - 1) != '('
    // || txnSplitArray[2].indexOf(',') == -1) {
    // System.out.println(givenTxnID + ": bad");
    // if (isVerbose) {
    // System.out.println(givenTxnID + ": the input UTXOs are not formatted properly");
    // }
    // System.err.println(givenTxnID
    // + ": Sorry, invalid transaction. Please use the correct format to input your transaction.
    // Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
    // return;
    // }
    while (right < txnSplitArray[2].length()) {
      if (txnSplitArray[2].charAt(left) != '(') {
        System.err.println(
            "Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
        return;
      } else {
        left++;
        right++;
        while (right < txnSplitArray[2].length() && txnSplitArray[2].charAt(right) != ',') {
          right++;
        }
        String txnId = txnSplitArray[2].substring(left, right);
        right++;
        left = right;
        while (right < txnSplitArray[2].length() && txnSplitArray[2].charAt(right) != ')') {
          right++;
        }
        int amount = Integer.parseInt(txnSplitArray[2].substring(left, right).trim());
        inputUTXOs.add(new TxnIdIndexPair(txnId, amount));
        right++;
        left = right;
      }
    }
    if (inputSize != inputUTXOs.size()) {
      System.out.println(givenTxnID + ": bad");
      System.err
          .println(givenTxnID + ": Sorry, invalid transaction. Your transaction must contain ."
              + inputSize + " pairs of input UTXOs.");
      return;
    }
    int outputSize = Integer.parseInt(txnSplitArray[3].trim());
    if (outputSize < 1) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": the number of output UTXOs should be greater than 0");
      return;
    }
    ArrayList<UTXO> outputUTXOs = new ArrayList<>();
    txnSplitArray[4] = txnSplitArray[4].trim();
    left = 0;
    right = 0;
    while (right < txnSplitArray[4].length()) {
      if (txnSplitArray[4].charAt(left) != '(') {
        System.err.println(
            "Sorry, invalid transaction. Please use the correct format to input your transaction.");
        return;
      } else {
        left++;
        right++;
        while (right < txnSplitArray[4].length() && txnSplitArray[4].charAt(right) != ',') {
          right++;
        }
        String txnId = txnSplitArray[4].substring(left, right);
        right++;
        left = right;
        while (right < txnSplitArray[4].length() && txnSplitArray[4].charAt(right) != ')') {
          right++;
        }
        int amount = Integer.parseInt(txnSplitArray[4].substring(left, right).trim());
        outputUTXOs.add(new UTXO(txnId, amount));
        right++;
        left = right;
      }
    }
    if (outputSize != outputUTXOs.size()) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": Your transaction must contain ." + outputSize
          + " pairs of output UTXOs.");
      return;
    }
    long inputAmount = 0;
    for (TxnIdIndexPair t : inputUTXOs) {
      if (txnChain.containsKey(t.getTxnId())) {
        if (!txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex()).isSpent()) {
          inputAmount +=
              txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex()).getAmount();
        } else {
          System.out.println(givenTxnID + ": bad");
          System.err
              .println(givenTxnID + ": Invalid transaction. One of the input UTXOs already spent.");
          if (isVerbose) {
            System.err.println(givenTxnID + ": The UTXO with id " + t.getTxnId() + " and index "
                + t.getIndex() + " was already spent.");
          }
          return;
        }
      } else {
        System.out.println(givenTxnID + ": bad");
        System.err.println(givenTxnID + ": The input transaction with id " + t.getTxnId()
            + " does not exist in the ledger");
        return;
      }
    }
    long outputAmount = 0;
    for (UTXO t : outputUTXOs) {
      outputAmount += t.getAmount();
    }
    if (isGenesisTransaction || inputAmount == outputAmount) {
      if (isGenesisTransaction) {
        isGenesisTransaction = false;
      }
      for (TxnIdIndexPair t : inputUTXOs) {
        UTXO currentTransaction =
            txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex());
        inputAmount += currentTransaction.getAmount();
        userToBalanceMap.put(currentTransaction.getUser(),
            userToBalanceMap.get(currentTransaction.getUser()) - currentTransaction.getAmount());
        currentTransaction.setSpent(true);
      }
      for (UTXO t : outputUTXOs) {
        if (userToBalanceMap.containsKey(t.getUser())) {
          userToBalanceMap.put(t.getUser(), userToBalanceMap.get(t.getUser()) + t.getAmount());
        } else {
          userToBalanceMap.put(t.getUser(), t.getAmount());
        }
      }
      txnChain.put(inputTxnID,
          new Transaction(inputTxn, inputTxnID, inputSize, outputSize, inputUTXOs, outputUTXOs));
      System.out.println(givenTxnID + ": good");
    } else {
      System.out.println(givenTxnID + ": bad");
      System.err.println(
          givenTxnID + ": Insufficient balance in the sender side to complete this transaction.");
      return;
    }
  }

  private String isValidSHA1TxnId(String input, int index) throws NoSuchAlgorithmException {
    String hashInput = input.substring(index + 2, input.length()) + "\n";
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
//tring isValidSHA1TxnId(String input, int index) throws NoSuchAlgorithmException {
//    String hashInput = input.substring(index + 2, input.length()) + "\n";
//    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(hashInput.getBytes());
//    StringBuffer sb = new StringBuffer();
//    for (int i = 0; i < result.length; i++) {
//      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
//    }
//    return sb.substring(0, 8).toLowerCase();
//  }

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.substring(0, 8).toLowerCase();
  }

  private boolean isValidTxnId(String s) {
    s = s.toLowerCase();
    if (s == null || s.length() != 8) {
      return false;
    }
    for (char c : s.toCharArray()) {
      if (Character.isLetter(c)) {
        if (c < 'a' || c > 'f') {
          return false;
        }
      }
    }
    return true;
  }
}
