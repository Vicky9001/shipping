package com.example.shipping.controllers;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.example.shipping.models.ERole;
import com.example.shipping.models.Role;
import com.example.shipping.payload.request.LoginRequest;
import com.example.shipping.payload.request.SignupRequest;
import com.example.shipping.repository.RoleRepository;
import com.example.shipping.repository.UserRepository;
import com.example.shipping.security.jwt.JwtUtils;
import com.example.shipping.security.services.UserDetailsImpl;
import com.example.shipping.security.utils.Result;
import com.example.shipping.security.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shipping.models.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/login")
	public Result authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Map<String, Object> response = new HashMap<>();
		try {
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

				//SecurityContextHolder.getContext().setAuthentication(authentication);
				String jwt = jwtUtils.generateJwtToken(authentication);

				UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
				List<String> roles = userDetails.getAuthorities().stream()
						.map(item -> item.getAuthority())
						.collect(Collectors.toList());

			response.put("name", userDetails.getUsername());
			response.put("email", userDetails.getEmail());
			response.put("id", userDetails.getId());
			response.put("roles",roles);
			response.put("jwt", jwt);

			return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Success", response)).getBody();
//				return ResponseEntity.ok(new JwtResponse(jwt,
//														 userDetails.getId(),
//														 userDetails.getUsername(),
//														 userDetails.getEmail(),
//														 roles));

		} catch (BadCredentialsException ex) {
			return ResponseEntity.badRequest()
					.body(new Result(ResultCode.INFOERR, "Fail", response)).getBody();
		}
    }

	@PostMapping("/signup")
	public Result registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		Map<String, Object> response = new HashMap<>();
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new Result(ResultCode.EXISTERR, "Error: Username is already taken!", response)).getBody();
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new Result(ResultCode.EXISTERR, "Error: Email is already in use!", response)).getBody();
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(),
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_SHIPPER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "shipper":
					Role shipperRole = roleRepository.findByName(ERole.ROLE_SHIPPER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(shipperRole);

					break;
				case "carrier":
					Role carrierRole = roleRepository.findByName(ERole.ROLE_CARRIER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(carrierRole);

					break;
				default:
					throw new RuntimeException("Error: Role is not found.");
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		response.put("name", user.getUsername());
		response.put("email", user.getEmail());
		List<Map<String, Object>> roleList = new ArrayList<>();
		for (Role role : roles) {
			Map<String, Object> roleMap = new HashMap<>();
			roleMap.put("role", role.getName());
			// 其他属性
			roleList.add(roleMap);
		}
		response.put("roles",roleList);

		return ResponseEntity.ok(new Result(ResultCode.SUCCESS, "Success", response)).getBody();
	}
}
