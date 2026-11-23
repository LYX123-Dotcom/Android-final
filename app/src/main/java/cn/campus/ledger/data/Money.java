package cn.campus.ledger.data;
import java.math.BigDecimal;
public final class Money {
    private Money() {}
    public static long parse(String raw) {
        if (!raw.matches("[0-9]{1,8}(\\.[0-9]{1,2})?")) throw new IllegalArgumentException();
        long value=new BigDecimal(raw).movePointRight(2).longValueExact();
        if(value<=0) throw new IllegalArgumentException();
        return value;
    }
    public static String format(long cents) { return BigDecimal.valueOf(cents,2).toPlainString(); }
}
