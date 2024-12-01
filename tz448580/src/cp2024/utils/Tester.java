package cp2024.utils;

public class Tester {
    
    public void check(Boolean value, Boolean expected) {
        if (value == expected) {
            System.out.println("OK");
        } else {
            System.out.println("FAIL");
        }
    }

}
