package org.example;

import java.util.ArrayList;
import java.util.List;

public class GoldPrice {
	String date;
	String time;
	List <Gold> listPrice;
	public GoldPrice(String date, String time) {
		super();
		this.date = date;
		this.time = time;
		this.listPrice = new ArrayList<Gold>();
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<Gold> getListPrice() {
		return listPrice;
	}
	public void setListPrice(List<Gold> listPrice) {
		this.listPrice = listPrice;
	}
	@Override
	public String toString() {
		return "Ngày: " + date + "\t Giờ: " + time + "\n" + listPrice.toString() + "\n------------------------------------------------------\n";
	}
}
