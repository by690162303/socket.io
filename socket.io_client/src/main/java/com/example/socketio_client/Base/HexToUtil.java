package com.example.socketio_client.Base;

/**
 * Created by 白杨 on 2016/3/21.
 */
public class HexToUtil {
    //-------------------------------------------------------
    /**
     * 	判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
     */

    static public int isOdd(int num)
    {
        return num & 0x1; //二进制判断
    }
    //-------------------------------------------------------
    static public int HexToInt(String inHex)//Hex字符串转int
    {
        return Integer.parseInt(inHex, 16);
    }
    static public String IntToOctal(String str){
        return Integer.parseInt(str,2)+"";
    }
    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//
    {
        return (byte)Integer.parseInt(inHex,16);
    }
    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }

    /**
     *     字节数组转转hex字符串
     */

    static public String ByteArrToHex(byte[] inBytArr)
    {
        StringBuilder strBuilder=new StringBuilder();
        int j=inBytArr.length;
        for (int i = 0; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    /**
     * 字节数组转转hex字符串，可选长度
     * @param inBytArr
     * @param byteCount
     * @return
     */
    static public String ByteArrToHex(byte[] inBytArr,int byteCount)//
    {
        StringBuilder strBuilder=new StringBuilder();
        int j = byteCount;
        for (int i = 0; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }
    /**
     *     转hex字符串转字节数组
     */
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen)==1) //判断奇数还是偶数
        {//奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {//偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2)
        {
            result[j]=HexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }
}
