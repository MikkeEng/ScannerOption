package com.mengroba.scanneroption.utils.epc;

import java.io.Serializable;

/**
 * Clase abstract que representa un EPC de un tag RFID (32bytes).
 * 
 * Tiene 2 implementaciones: 
 * - GarmentEpc : Preda
 * - BeaconEpc : Baliza
 * 
 * Para pasear: Epc.of("08283D24E014054C2580897F97C00400") devuelve o un GarmentEpc o un BeaconEpc.
 * 
 * Para activar/desactivar una prenda. GarmentEpc newEpc = ((GarmentEpc) epc).enable() o GarmentEpc newEpc = ((GarmentEpc) epc).disable()
 * 
 * Para modificar una prenda GarmentEpc newEpc = ((GarmentEpc) epc).with().brand(2).sectionType(2).garment(garmentCode).softTag()
 * 
 * Para modificar una baliza BeaconEpc newEpc = BeaconEpc.override(epc).block(12345).left().number(23890).noLimit().noExtreme();
 * 
 * TODO : Falta definir el formato final de BeaconEpc
 * TODO : Faltan algunos campos de GarmentEpc
 * 
 * @author jfreire
 */
public abstract class Epc implements Serializable {

    private static final long serialVersionUID = 325344887966660471L;
    
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static Epc of(String hex) {
        if (hex.length() != 32) {
            throw new IllegalArgumentException("Invalid epc " + hex);
        }

        int first = read(hex, 8);
        int second = read(hex, 16);
        int serial = read(hex, 24);
        int fourth = read(hex, 32);

        int version = (int) (first >> 27) & 0b11111;
        
        GarmentEpc garmentEpc = new GarmentEpc(first, second, serial, fourth);
        GarmentCode garmentCode = garmentEpc.garmentCode();

        if ((garmentCode.product() == 9 && garmentCode.model() >= 9900)
                || (garmentCode.product() == 0 && garmentCode.model() == 0000)) {
            return new BeaconEpc(garmentEpc);
        }

        return garmentEpc;
    }
    
    private static int read(String hex, int pos) {
        long result = 0;
        
        for (int i = pos - 8; i < pos; i++) {
            final char ch = hex.charAt(i);
            final int val;
            
            switch (ch) {
            case '0' : val = 0; break;
            case '1' : val = 1; break;
            case '2' : val = 2; break;
            case '3' : val = 3; break;
            case '4' : val = 4; break;
            case '5' : val = 5; break;
            case '6' : val = 6; break;
            case '7' : val = 7; break;
            case '8' : val = 8; break;
            case '9' : val = 9; break;
            case 'A' : case 'a' : val = 10; break;
            case 'B' : case 'b' : val = 11; break;
            case 'C' : case 'c' : val = 12; break;
            case 'D' : case 'd' : val = 13; break;
            case 'E' : case 'e' : val = 14; break;
            case 'F' : case 'f' : val = 15; break;
            default :
                throw new IllegalArgumentException("Invalid epc " + hex);
            }
            
            if (i == pos - 1) {
                result = result | val;
            } else {
                result = (result | val) << 4;
            }
        }
        
        return (int) result;
    }
    
    private static void write(char[] chars, long value, int pos) {
        long current = value;
        final int last = pos - 8;
        
        for (int i = pos - 1; i >= last; i--) {
            chars[i] = HEX[(int) (current & 0b1111)];
            current = current >> 4;
        }
    }

    private final int first;
    private final int second;
    private final int serial;
    private final int fourth;

    protected Epc(int first, int second, int serial, int fourth) {
        this.first = first;
        this.second = second;
        this.serial = serial;
        this.fourth = fourth;
    }

    public long serial() {
        return this.serial & 0xffffffffl;
    }

    public long first() {
        return this.first & 0xffffffffl;
    }

    public long second() {
        return this.second & 0xffffffffl;
    }

    public long fourth() {
        return this.fourth & 0xffffffffl;
    }

    public int version() {
        return (int) (this.first >> 27) & 0b11111;
    }
    
    public String serialAsHex() {
        char[] chars = new char[8];
        write(chars, this.serial, 8);
        
        return new String(chars);
    }

    public String firstAsHex() {
        char[] chars = new char[8];
        write(chars, this.first, 8);

        return new String(chars);
    }

    public String secondAsHex() {
        char[] chars = new char[8];
        write(chars, this.second, 8);

        return new String(chars);
    }
    
    @Override
    public int hashCode() {
        return this.first ^ this.second ^ this.serial ^ this.fourth;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Epc
                && ((Epc) obj).first == this.first
                && ((Epc) obj).second == this.second
                && ((Epc) obj).serial == this.serial
                && ((Epc) obj).fourth == this.fourth;
    }

    @Override
    public String toString() {
        char[] chars = new char[32];
        write(chars, this.first, 8);
        write(chars, this.second, 16);
        write(chars, this.serial, 24);
        write(chars, this.fourth, 32);
        
        return new String(chars);
    }
}
