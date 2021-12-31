public class Cursor {
  public String string;

  public Cursor(String string) {
    this.string = string;
  }

  public void move(int length) {
    this.string = this.string.substring(length);
  }
}
