package fxs.free.com.wavetestdemo;

public class Test {
    public String test;

    public String[] types = new String[]{"1", "2", "3"};

    public String getTest() {
        return test == null ? "" : test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
