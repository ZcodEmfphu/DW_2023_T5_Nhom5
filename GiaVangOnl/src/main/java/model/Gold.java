package model;

public class Gold {

  String time;
  String date;
  String type;
  double buying;
  double selling;

  public Gold(String time, String date, String type, double buying, double selling) {
    super();
    this.time = time;
    this.date = date;
    this.type = type;
    this.buying = buying;
    this.selling = selling;
  }

  public String getDate() {
    return date;
  }

  public String getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public double getBuying() {
    return buying;
  }

  public void setBuying(double buying) {
    this.buying = buying;
  }

  public double getSelling() {
    return selling;
  }

  public void setSelling(double selling) {
    this.selling = selling;
  }

  @Override
  public String toString() {
    return "Loại vàng: " + type + "\n Giá mua: " + buying + "\t Giá bán: " + selling
        + "\n==================\n";
  }


}
