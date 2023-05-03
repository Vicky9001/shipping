package com.example.shipping.models;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(	name = "orders",
		uniqueConstraints = {
			@UniqueConstraint(columnNames = "id")
		})
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private LocalDate createDate;

	@ManyToOne
	@JoinColumn(name = "shipperId")
	private User shipper;
	@ManyToOne
	@JoinColumn(name = "carrierId")
	private User carrier;

	@OneToMany(mappedBy = "order")
	private List<Transfer> transfers;

	private String destination;

	private int state;

	private double money;
	private String start;

	private double weight;

	private String receiver;

	public Order(User shipper, String start, String destination, String receiver, double weight, double money, int state, LocalDateTime createDate) {
		this.shipper = shipper;
		this.start= start;
		this.destination = destination;
		this.receiver = receiver;
		this.weight = weight;
		this.money = money;
		this.state = state;
		this.createDate = LocalDate.from(createDate);
	}

	public Order(User shipper, User carrier, String start, String destination, String receiver, double weight, double money, int state, LocalDateTime createDate) {
		this.shipper = shipper;
		this.carrier = carrier;
		this.start= start;
		this.destination = destination;
		this.receiver = receiver;
		this.weight = weight;
		this.money = money;
		this.state = state;
		this.createDate = LocalDate.from(createDate);
	}

	@Override
	public String toString() {
		return "Order{" +
				"id=" + id +
				", createDate=" + createDate +
				", shipper=" + shipper +
				", carrier=" + carrier +
				", transfers=" + transfers +
				", destination='" + destination + '\'' +
				", state=" + state +
				", money=" + money +
				", start='" + start + '\'' +
				", weight=" + weight +
				", receiver='" + receiver + '\'' +
				'}';
	}

	public Order() {

	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDate createDate) {
		this.createDate = createDate;
	}

	public User getShipper() {
		return shipper;
	}

	public void setShipper(User shipper) {
		this.shipper = shipper;
	}

	public User getCarrier() {
		return carrier;
	}

	public void setCarrier(User carrier) {
		this.carrier = carrier;
	}

	public List<Transfer> getTransfers() {
		return transfers;
	}

	public void setTransfers(List<Transfer> transfers) {
		this.transfers = transfers;
	}

	public int getState() {
		return state;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public void setState(int state) {
		this.state = state;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}
}
