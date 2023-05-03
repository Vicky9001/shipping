package com.example.shipping.controllers;

import com.example.shipping.models.*;
import com.example.shipping.payload.request.OrderRequest;
import com.example.shipping.payload.response.MessageResponse;
import com.example.shipping.repository.OrderRepository;
import com.example.shipping.repository.RoleRepository;
import com.example.shipping.repository.TransferRepository;
import com.example.shipping.repository.UserRepository;
import com.example.shipping.security.utils.Result;
import com.example.shipping.security.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shipping.models.ERole.ROLE_CARRIER;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/order")
public class OrderController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	TransferRepository transferRepository;

	@Autowired
	RoleRepository roleRepository;

	@PostMapping("/create")
	public ResponseEntity<?> registerUser(@Valid @RequestBody OrderRequest orderRequest) {

		Map<String, Object> response = new HashMap<>();
		User shipper = userRepository.findById(orderRequest.getShipperId()).get();
		if (shipper == null) {
			return ResponseEntity
					.badRequest()
					.body(new Result(ResultCode.INFOERR, "Shipper not found!", response));
		}
		int state=0;
		LocalDateTime createDate= LocalDateTime.now();
		System.out.println(orderRequest.getStart());
		Order order = new Order(shipper,
							 orderRequest.getStart(),
							 orderRequest.getDestination(),
								orderRequest.getReceiver(),
								orderRequest.getWeight(),
								orderRequest.getMoney(),
				                state,
				                createDate);

		System.out.println(order);
		orderRepository.save(order);

		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Success", response));
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateOrder(@Valid @RequestBody OrderRequest orderRequest) {
		Order order = orderRepository.findById(orderRequest.getId()).orElse(null);
		Map<String, Object> response = new HashMap<>();
		if (order == null) {
			return ResponseEntity.badRequest().body(new Result(ResultCode.INFOERR, "Order not found!", response));
		}
		User carrier = userRepository.findById(orderRequest.getCarrierId()).orElse(null);
		Role c = roleRepository.findByName(ERole.valueOf("ROLE_CARRIER")).get();
		if (carrier == null || !carrier.getRoles().contains(c)) {
			return ResponseEntity.badRequest().body(new Result(ResultCode.INFOERR, "Carrier not found!", response));
		}
		order.setCarrier(carrier);
		orderRepository.save(order);
		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Update successfully!", response));
	}

	@PostMapping("/changeState")
	public ResponseEntity<?> changeState(@Valid @RequestBody OrderRequest orderRequest) {
		Order order = orderRepository.findById(orderRequest.getId()).orElse(null);
		Map<String, Object> response = new HashMap<>();
		if (order == null) {
			return ResponseEntity.badRequest().body(new Result(ResultCode.INFOERR, "Order not found!", response));
		}
		//1-已付款待揽件，2-揽件运输中，3-已交付，4-退货拒收 40-退货待揽件 41-退货运输中 42-退货交付 5-订单结束
		int state = orderRequest.getState();
		order.setState(state);
		orderRepository.save(order);
		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Update successfully!", response));
	}

	@PostMapping("/transfer")
	public ResponseEntity<?> transfer(@Valid @RequestBody OrderRequest orderRequest) {
		Order order = orderRepository.findById(orderRequest.getId()).orElse(null);
		Map<String, Object> response = new HashMap<>();
		if (order == null) {
			return ResponseEntity.badRequest().body(new Result(ResultCode.INFOERR, "Order not found!", response));
		}
		if (order.getState() == 0) {
			return ResponseEntity.badRequest().body(new Result(ResultCode.INFOERR, "State of order wrong!", response));
		}

		String loca = orderRequest.getLoca();
		LocalDateTime time = LocalDateTime.now();

		Transfer transfer = new Transfer(order,loca,time);
		transferRepository.save(transfer);

		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Update successfully!", response));
	}

	@GetMapping("/searchCarrier/{carrierId}")
	public ResponseEntity<?> searchOrdersByCarrierId(@PathVariable("carrierId") Long carrierId) {
		Result res = new Result();
		// 从userRepository中获取carrier对象
		User carrier = userRepository.findById(carrierId).orElse(null);
		Role c = roleRepository.findByName(ERole.valueOf("ROLE_CARRIER")).get();
		Map<String, Object> response = new HashMap<>();
		if (carrier == null || !carrier.getRoles().contains(c)) {
			return ResponseEntity.ok(new Result(ResultCode.INFOERR, "Invalid carrier ID", response));
		}
		// 从orderRepository中获取该carrier的所有订单
		List<Order> orders = orderRepository.findByCarrier(carrier);

		if (orders.isEmpty()) {
			return ResponseEntity.ok(new Result(ResultCode.INFOERR, "No orders found for this carrier", response));
		}
		double totalMoney = orders.stream().mapToDouble(Order::getMoney).sum(); // 计算money总和
		List<Map<String, Object>> ordersList = new ArrayList<>();
		for (Order order : orders) {
			Map<String, Object> orderMap = new HashMap<>();
			orderMap.put("id", order.getId());
			orderMap.put("from", order.getStart());
			orderMap.put("to", order.getDestination());
			orderMap.put("money", order.getMoney());
			// 其他属性
			ordersList.add(orderMap);
		}
		res.setCode(ResultCode.SUCCESS);
		res.setMessage("Success");
		response.put("orders", ordersList);
		response.put("totalMoney", totalMoney); // 将money总和添加到响应中

		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Success", response));
	}

	@GetMapping("/searchShipment/{orderId}")
	public ResponseEntity<?> searchShipmentsByOrderId(@PathVariable("orderId") Long orderId) {
		// 从orderRepository中获取指定订单
		Order order = orderRepository.findById(orderId).orElse(null);
		Map<String, Object> response = new HashMap<>();
		if (order == null) {
			return ResponseEntity.ok(new Result(ResultCode.INFOERR, "Invalid order ID", response));
		}
		// 获取该订单的所有转运记录
		List<Transfer> shipments = transferRepository.findByOrder(order);
		if (shipments.isEmpty()) {
			return ResponseEntity.ok(new Result(ResultCode.INFOERR, "No shipments found for this order", response));
		}
		List<Map<String, Object>> shipList = new ArrayList<>();
		for (Transfer ship : shipments) {
			Map<String, Object> shipMap = new HashMap<>();
			shipMap.put("id", ship.getId());
			shipMap.put("location", ship.getLocaName());
			shipMap.put("transferTime", ship.getTransferTime());
			// 其他属性
			shipList.add(shipMap);
		}
		response.put("shipments", shipList);
		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Success", response));
	}
}
