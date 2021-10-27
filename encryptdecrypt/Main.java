package encryptdecrypt;

import java.io.*;

interface EncryptionStrategy {
    String encrypt(String plainText, int key);
    String decrypt(String cipherText, int key);
}

class UnicodeEncryption implements EncryptionStrategy  {

    private static final int UNICODE_LENGTH;

    static  {
        int temp = '\uffff';
        UNICODE_LENGTH = temp + 1;
    }

    @Override
    public String encrypt(String plainText, int key) {
        key %= UNICODE_LENGTH;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            int numForChar = plainText.charAt(i);
            numForChar = (numForChar + key) % UNICODE_LENGTH;
            sb.append((char) numForChar);
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String cipherText, int key) {
        key %= UNICODE_LENGTH;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cipherText.length(); i++) {
            int numForChar = cipherText.charAt(i);
            numForChar = (numForChar - key) % UNICODE_LENGTH;
            sb.append((char) numForChar);
        }
        return sb.toString();
    }
}

class AlphabetEncryption implements EncryptionStrategy {

    private static final int UPPERCASE_A;
    private static final int UPPERCASE_Z;
    private static final int LOWERCASE_A;
    private static final int LOWERCASE_Z;
    private static final int ALPHABET_SIZE;

    static  {
        UPPERCASE_A = 'A';
        UPPERCASE_Z = 'Z';
        LOWERCASE_A = 'a';
        LOWERCASE_Z = 'z';
        ALPHABET_SIZE = 26;
    }

    @Override
    public String encrypt(String plainText, int key) {
        key %= ALPHABET_SIZE;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            int ch = plainText.charAt(i);
            if (ch >= UPPERCASE_A && ch <= UPPERCASE_Z) {
                ch = UPPERCASE_A + (ch - UPPERCASE_A + key) % ALPHABET_SIZE;
            } else if (ch >= LOWERCASE_A && ch <= LOWERCASE_Z){
                ch = LOWERCASE_A + (ch - LOWERCASE_A + key) % ALPHABET_SIZE;
            }
            sb.append((char) ch);
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String cipherText, int key) {
        key %= ALPHABET_SIZE;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cipherText.length(); i++) {
            int ch = cipherText.charAt(i);
            if (ch >= UPPERCASE_A && ch <= UPPERCASE_Z) {
                ch = UPPERCASE_A + (ALPHABET_SIZE + ch - UPPERCASE_A - key) % ALPHABET_SIZE;
            } else if (ch >= LOWERCASE_A && ch <= LOWERCASE_Z){
                ch = LOWERCASE_A + (ALPHABET_SIZE + ch - LOWERCASE_A - key) % ALPHABET_SIZE;
            }
            sb.append((char) ch);
        }
        return sb.toString();
    }
}

public class Main {
    private static final int MAX_MEMORY_READ_BYTES;
    static {
        MAX_MEMORY_READ_BYTES = 512;
    }
    private static String parseMode(String arg) {
        if("dec".equals(arg)) {
            return "dec";
        }
        return "enc";
    }
    private static String parseText(String arg) {
        if(arg == null) {
            return "";
        }
        return arg;
    }
    private static int parseKey(String arg) {
        if(arg == null) {
            return 0;
        }
        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            key = 0;
        }
        return key;
    }
    private static FileInputStream parseInputFile(String path) {
        try {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                return new FileInputStream(file);
            } else{
                return null;
            }
        } catch (NullPointerException | FileNotFoundException e) {
            return null;
        }
    }
    private static PrintStream parseOutputFile(String path) {
        try {
            File file = new File(path);
            PrintStream ps = null;
            if (!file.exists()){
                if (file.createNewFile()) {
                    ps = new PrintStream(file);
                }
            } else if (file.canWrite()) {
                ps = new PrintStream(file);
            }
            return ps;
        } catch (NullPointerException | IOException | SecurityException e) {
            return System.out;
        }
    }
    private static void manageCMDInput(String _mode, int _key, String _data, PrintStream _out, EncryptionStrategy _alg) {
        if (_key == 0 || "".equals(_data)) {
            _out.print(_data);
            return;
        }
        if ("enc".equals(_mode)) {
            _out.print(_alg.encrypt(_data, _key));
        } else {
            _out.print(_alg.decrypt(_data, _key));
        }
    }
    private static void manageFileInput(String _mode, int _key, FileInputStream _in, PrintStream _out, EncryptionStrategy _alg){
        byte[] buffer = new byte[MAX_MEMORY_READ_BYTES];
        int readLength;
        if ("enc".equals(_mode)) {
            try {
                while ((readLength = _in.read(buffer)) != -1) {
                    if (readLength < MAX_MEMORY_READ_BYTES) {
                        byte[] newBuffer = new byte[readLength];
                        System.arraycopy(buffer, 0, newBuffer, 0 ,readLength);
                        buffer = newBuffer;
                    }
                    _out.print(_alg.encrypt(new String(buffer), _key));
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                while ((readLength = _in.read(buffer)) != -1) {
                    if (readLength < MAX_MEMORY_READ_BYTES) {
                        byte[] newBuffer = new byte[readLength];
                        System.arraycopy(buffer, 0, newBuffer, 0 ,readLength);
                        buffer = newBuffer;
                    }
                    _out.print(_alg.decrypt(new String(buffer), _key));
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static void closeResources(PrintStream _out, FileInputStream _in) {
        if (_in != null) {
            try {
                _in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (_out != null && _out != System.out) {
            _out.close();
        }
    }
    private static EncryptionStrategy parseAlgorithm(String arg) {
        if ("unicode".equals(arg)) {
            return new UnicodeEncryption();
        }
        return new AlphabetEncryption();
    }
    public static void main(String[] args) {
        String _mode = "enc";
        String _data = null;
        int _key = 0;
        PrintStream _out = System.out;
        FileInputStream _in = null;
        String _alg = "shift";
        int len = args.length/2;
        for (int i = 0; i < len; i++){
            switch (args[2 * i]) {
                case "-mode":
                    _mode = parseMode(args[2 * i + 1]);
                    break;
                case "-key":
                    _key = parseKey(args[2 * i + 1]);
                    break;
                case "-data":
                    _data = parseText(args[2 * i + 1]);
                    break;
                case "-in":
                    _in = parseInputFile(args[2 * i + 1]);
                    break;
                case "-out":
                    _out = parseOutputFile(args[2 * i + 1]);
                    break;
                case "-alg":
                    _alg = args[2 * i + 1];
                    break;
                default:
                    break;
            }
        }
        if (_data != null) {
            manageCMDInput(_mode, _key, _data, _out, parseAlgorithm(_alg));
        } else if (_in != null){
            manageFileInput(_mode, _key, _in, _out, parseAlgorithm(_alg));
        }
        closeResources(_out, _in);
    }
}
