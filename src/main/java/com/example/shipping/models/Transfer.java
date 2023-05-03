package com.example.shipping.models;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "id")
		})
public class Transfer {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private LocalDateTime transferTime;
	private String locaName;
	@ManyToOne
	@JoinColumn(name = "orderId")
	private Order order;
	public Transfer(Order order, String loca, LocalDateTime time) {
		this.order = order;
		this.locaName = loca;
		this.transferTime = time;
	}

	public Transfer() {

	}

	@Override
	public String toString() {
		return "Transfer{" +
				"id=" + id +
				", transferTime=" + transferTime +
				", locaName='" + locaName + '\'' +
				", order=" + order +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getTransferTime() {
		return transferTime;
	}

	public void setTransferTime(LocalDateTime transferTime) {
		this.transferTime = transferTime;
	}

	public String getLocaName() {
		return locaName;
	}

	public void setLocaName(String locaName) {
		this.locaName = locaName;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
