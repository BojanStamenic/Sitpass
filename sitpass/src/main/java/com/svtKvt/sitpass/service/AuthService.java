package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.AccountRequest;
import com.svtKvt.sitpass.model.RequestStatus;
import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.repository.AccountRequestRepository;
import org.apache.coyote.BadRequestException; // Pretpostavka o postojećem repozitorijumu za korisnike
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountRequestRepository accountRequestsRepo;
    private final UserDetailsCustom userDetailsService;
    private final UserService usersService;

    public AuthService(PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       AccountRequestRepository accountRequestsRepo,
                       UserDetailsCustom userDetailsService,
                       UserService usersService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.accountRequestsRepo = accountRequestsRepo;
        this.userDetailsService = userDetailsService;
        this.usersService = usersService;
    }

    public Map<String, String> login(String email, String password) throws BadRequestException {
        UserDetails userDetails2 = userDetailsService.loadUserByUsername(email);
        logger.info(userDetails2);
        logger.info(password);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)); //LINIJA KOJA PRAVI PROBLEM
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            String accessToken = jwtService.generateToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("email", email);
            response.put("accessToken", accessToken);

            return response;
        } catch (Exception e) {

            logger.error("Authentication failed", e);
            throw new BadRequestException("Invalid credentials. Please try again.");
        }
    }

    public List<AccountRequest> findAllPendingRequests() {
        List<AccountRequest> reqs = accountRequestsRepo.findAllPending();
        logger.info("Found {} pending", reqs.size());
        return reqs;
    }

    public AccountRequest send(AccountRequest accountRequest) throws BadRequestException {
        validateEmail(accountRequest.getEmail());
        validateAddress(accountRequest.getAddress());
        validatePassword(accountRequest.getPassword());

        accountRequestsRepo.findByEmail(accountRequest.getEmail()).ifPresent(ar -> {
            try {
                throw new BadRequestException("Account already exists with this email.");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        });

        accountRequest.setCreatedAt(LocalDate.now());
        accountRequest.setStatus(RequestStatus.PENDING);
        logger.info("Sending {}", accountRequest);
        return accountRequestsRepo.save(accountRequest);
    }

    public AccountRequest handle(AccountRequest accountRequest) throws BadRequestException {
        AccountRequest req = accountRequestsRepo.findById(accountRequest.getId()).orElseThrow(() ->
                new BadRequestException("Account does not exist with this id."));
        req.setStatus(accountRequest.getStatus());

        if (req.getStatus() == RequestStatus.REJECTED) {
            req.setRejectionReason(accountRequest.getRejectionReason());
        }

        if (req.getStatus() == RequestStatus.ACCEPTED) {
            User user = new User();
            user.setEmail(req.getEmail());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setCreatedAt(req.getCreatedAt());
            user.setAddress(req.getAddress());
            usersService.saveUser(user);
        }

        logger.info("Handled {}", accountRequest);
        return accountRequestsRepo.save(req);
    }

    private void validateEmail(String email) throws BadRequestException {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new BadRequestException("Invalid email format.");
        }
    }

    private void validateAddress(String address) throws BadRequestException {
        if (address == null || address.length() <= 3) {
            throw new BadRequestException("Address must be more than 3 characters.");
        }
    }

    private void validatePassword(String password) throws BadRequestException {
        if (password == null || password.length() <= 3) {
            throw new BadRequestException("Password must be more than 3 characters.");
        }
    }
}
