package script;

public class OpCodes {
    public static final int OP_0 = 0x00;

    public static final int OP_PUSHDATA1 = 0x4c;
    public static final int OP_PUSHDATA2 = 0x4d;
    public static final int OP_PUSHDATA4 = 0x4e;

    public static final int OP_1 = 0x51;
    public static final int OP_16 = 0x60;

    public static final int OP_DUP = 0x76;

    public static final int OP_EQUAL = 0x87;
    public static final int OP_EQUALVERIFY = 0x88;

    public static final int OP_ADD = 0x93;

    public static final int OP_RIPEMD160 = 0xa6;
    public static final int OP_SHA1 = 0xa7;
    public static final int OP_SHA256 = 0xa8;
    public static final int OP_HASH160 = 0xa9;
    public static final int OP_HASH256 = 0xaa;
    public static final int OP_CODESEPARATOR = 0xab;
    public static final int OP_CHECKSIG = 0xac;
    public static final int OP_CHECKSIGVERIFY = 0xad;
    public static final int OP_CHECKMULTISIG = 0xae;
    public static final int OP_CHECKMULTISIGVERIFY = 0xaf;
}
