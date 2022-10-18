import deck.wrapper.ContextWrapper;

public class dextest {

    public int simpleAdd(int a, int b) {
      return a + b;
    }

    public String run(ContextWrapper contextWrapper) {
      int ret = simpleAdd(1000, 200);
      return String.valueOf(ret);
    }
  }
