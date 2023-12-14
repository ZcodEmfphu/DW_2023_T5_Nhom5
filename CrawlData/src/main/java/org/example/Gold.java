package org.example;

public class Gold {
	String type;
	double buying;
	double selling ;
	public Gold(String type, double buying, double selling) {
		super();
		this.type = type;
		this.buying = buying;
		this.selling = selling;
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
		return "Loại vàng: " + type + "\n Giá mua: " + buying + "\t Giá bán: " + selling + "\n==================\n";
	}
	
	

}
