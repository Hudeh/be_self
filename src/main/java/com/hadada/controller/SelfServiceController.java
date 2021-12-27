package com.hadada.controller;

import com.hadada.encrypt.EncryptDecrypt;
import com.hadada.exception.CustomException;
import com.hadada.modal.*;
import com.hadada.repositories.*;
import com.hadada.security.JWTAuthorizationFilter;
import com.hadada.security.JwtFilter;
import com.hadada.security.TokenManager;
import com.hadada.services.EmailService;
import com.hadada.services.FileService;
import com.hadada.services.OtpService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.net.URLEncoder;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("hadada")
@CrossOrigin(origins="*",allowedHeaders="*")
public class SelfServiceController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AppRepository appRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CallBackCountRepository callBackCountRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private CollectedPdfRepository collectedPdfRepository;
    @Autowired
    public OtpService otpService;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    public FileService fileService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CustomerRepository repository;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/test-check")
    public String testCheck() {
        return "Service is healthy.";
    }

    @GetMapping("/get-lender-details/{authToken}")
    public Customer getLenderDetails(@PathVariable final String authToken) {
        return modelMapper.map(repository.findByAuthKey(authToken), Customer.class);
    }

    @GetMapping("/get-health")
    public String getHealth() {
        return "2.0.2";
    }

    @GetMapping("/get-apps/{email}/{environment}")
    public List<App> getApps(@PathVariable String email, @PathVariable String environment, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return appRepository.findByEmailAndEnvironment(email, environment);
    }

    @GetMapping("/get-app/{email}/{appKey}")
    public App getApp(@PathVariable String email, @PathVariable String appKey, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return appRepository.findByAppKey(appKey);
    }

    @GetMapping("/get-callback-count/{email}/{environment}")
    public List<CallBackCount> getCallBackCount(@PathVariable String email, @PathVariable String environment, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        List<App> apps = appRepository.findByEmailAndEnvironment(email, environment);
        List<Long> appIds = new ArrayList();
        apps.forEach(app -> appIds.add(app.getAppId()));
        List<CallBackCount> callBackCounts = callBackCountRepository.findByAppIdIn(appIds);
        if (environment.equalsIgnoreCase("production")) {
            List<String> sessionKeys = new ArrayList();
            List<Statement> statements = statementRepository.findByEmail(email);
            statements.forEach(statement -> sessionKeys.add(statement.getSessionKey()));
            List<CallBackCount> callBackStatementCounts = callBackCountRepository.findBySessionKeyIn(sessionKeys);
            callBackCounts.addAll(callBackStatementCounts);
        }
        return callBackCounts;
    }

    @GetMapping("/delete-app/{email}/{appKey}")
    public String deleteApp(@PathVariable String email, @PathVariable String appKey, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        App app = appRepository.findByAppKey(appKey);
        appRepository.delete(app);
        return "app deleted";
    }

    @PostMapping("/create-app")
    public App createApps(@RequestBody App app, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, app.getEmail())) {
            throw new CustomException();
        }
        if (app.getAppId() != null) {
            Optional<App> optional = appRepository.findById(app.getAppId());
            App appObj = optional.get();
            appObj.setAppName(app.getAppName());
            appObj.setCallBackUrl(app.getCallBackUrl());
            appObj.setAppType(app.getAppType());
            app = appRepository.save(appObj);
        } else {
            UUID uuid = UUID.randomUUID();
            String appKey = uuid.toString();
            app.setAppKey(appKey.substring(appKey.length() - 16));
            app = appRepository.save(app);
        }
        return app;
    }

    @PostMapping("/create-organization")
    public Organization createOrganization(@RequestBody Organization organization) {
        if (organization.getOrganizationKey() == null) {
            List<Organization> organizationsList = organizationRepository.findByOfficialEmail(organization.getOfficialEmail());
            if (organizationsList.size() > 0) {
                return organization;
            }
            UUID uuid = UUID.randomUUID();
            organization.setClientId(uuid.toString());
            organization = organizationRepository.save(organization);
        } else {
            Optional<Organization> optional = organizationRepository.findById(organization.getOrganizationKey());
            Organization organizationObj = optional.get();
            UUID uuid = UUID.randomUUID();
            organizationObj.setClientId(uuid.toString());
            organizationObj.setEmployeeName(organization.getEmployeeName());
            organizationObj.setOrganizationName(organization.getOrganizationName());
            organizationObj.setOrganizationCountry(organization.getOrganizationCountry());
            organization = organizationRepository.save(organizationObj);
        }

        return organization;
    }

    @GetMapping("/get-organization/{email}")
    public Organization getOrganization(@PathVariable String email) {
        List<Organization> organizationsList = organizationRepository.findByOfficialEmail(email);
        Organization organization = new Organization();
        if (organizationsList.size() > 0) {
            organization = organizationsList.get(0);
        }
        return organization;
    }

    @GetMapping("/get-wallet/{email}")
    public Long getWallet(@PathVariable String email) {
        List<Customer> customerListList = customerRepository.findByUsername(email);
        Customer customer = new Customer();
        if (customerListList.size() > 0) {
            customer = customerListList.get(0);
        }
        return customer.getWallet();
    }

    @PostMapping("/create-user")
    public Customer createUser(@RequestBody Customer customer) {
        List<Organization> organizationsList = organizationRepository.findByOfficialEmail(customer.getUsername());
        if (organizationsList.size() == 0) {
            customer.setStatus("organization is not present");
            return customer;
        } else {
            List<Customer> customerList = customerRepository.findByUsername(customer.getUsername());
            if (customerList.size() > 0) {
                customer.setStatus("customer is already present");
                return customer;
            } else {
                UUID uuid = UUID.randomUUID();
                customer.setAuthKey(EncryptDecrypt.encryptKey(uuid.toString()));
                customer.setClientId(organizationsList.get(0).getClientId());
                String password = EncryptDecrypt.encryptKey(customer.getPassword());
                customer.setPassword(password);
                String pin = EncryptDecrypt.encryptKey(customer.getPin());
                customer.setPin(pin);
                customer.setRoleKey(1l);
                customer = customerRepository.save(customer);
                Optional<Organization> optional = organizationRepository.findById(organizationsList.get(0).getOrganizationKey());
                Organization organizationObj = optional.get();
                organizationObj.setStatus("Active");
                organizationRepository.save(organizationObj);
            }
        }
        return customer;
    }

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public Customer login(@RequestBody Customer customer, HttpServletResponse res) {
        List<Customer> customerList = customerRepository.findByUsername(customer.getUsername());
        if (customerList.size() > 0) {
            Customer customerObj = customerList.get(0);
            String password = EncryptDecrypt.decryptKey(customerObj.getPassword());
            if (!password.equals(customer.getPassword())) {
                customer.setStatus("Password is incorrect");
                return customer;
            }
            customer = customerObj;
        } else {
            customer.setStatus("Username is not registered");
            return customer;
        }
        String token = tokenManager.generateJwtToken(customer.getUsername());
        customer.setToken(null);
        customer.setPassword(null);
        customer.setPin(null);
        customer.setAuthKey(null);
        Cookie jwtTokenCookie = null;
        try {
            jwtTokenCookie = new Cookie("HCSession", URLEncoder.encode(token, "UTF-8"));
            System.out.println(jwtTokenCookie);
        } catch (Exception e) {
            System.out.println(e);
        }
        jwtTokenCookie.setMaxAge(86400);
        jwtTokenCookie.setSecure(true);
        jwtTokenCookie.setHttpOnly(true);
        jwtTokenCookie.setPath("/");
        res.addCookie(jwtTokenCookie);
        return customer;
    }

    @PostMapping(path = "/resetPassword", consumes = "application/json", produces = "application/json")
    public Customer resetPassword(@RequestBody Customer customer, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, customer.getUsername())) {
            throw new CustomException();
        }
        List<Customer> customerList = customerRepository.findByUsername(customer.getUsername());
        if (customerList.size() > 0) {
            Customer customerObj = customerList.get(0);
            Optional<Customer> optional = customerRepository.findById(customerObj.getCustomerId());
            customerObj = optional.get();
            String password = EncryptDecrypt.encryptKey(customer.getPassword());
            customerObj.setPassword(password);
            customerRepository.save(customerObj);
            customer.setStatus("Password updated successfully");
        } else {
            customer.setStatus("Username is not registered");
            return customer;
        }
        return customer;
    }

    @GetMapping("/generateOtp/{username}")
    public String generateOtp(@PathVariable String username) {
        int otp = otpService.generateOTP(username);
        System.out.println("OTP" + otp);
        emailService.sendMail(username,"Hadada otp verification", "Your otp is " + otp);
        return "OTP sent successfully";
    }

    @RequestMapping(value = "/validateOtp", method = RequestMethod.GET)
    public @ResponseBody
    String validateOtp(@RequestParam("otpnum") int otpnum, @RequestParam("username") String username, @RequestParam("resetPassword") Boolean resetPassword, HttpServletResponse res) {
        final String SUCCESS = "Entered Otp is valid";
        final String FAIL = "Entered Otp is NOT valid. Please Retry!";
        logger.info(" Otp Number : " + otpnum);
        if (otpnum >= 0) {
            int serverOtp = otpService.getOtp(username);
            if (serverOtp > 0) {
                if (otpnum == serverOtp) {
                    otpService.clearOTP(username);
                    if (resetPassword) {
                        String token = tokenManager.generateJwtToken(username);
                        Cookie jwtTokenCookie = null;
                        try {
                            jwtTokenCookie = new Cookie(JWTAuthorizationFilter.HEADER, URLEncoder.encode(token, "UTF-8"));
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        jwtTokenCookie.setMaxAge(86400);
                        jwtTokenCookie.setSecure(true);
                        jwtTokenCookie.setHttpOnly(true);
                        jwtTokenCookie.setPath("/");
                        res.addCookie(jwtTokenCookie);
                    }
                    return SUCCESS;
                } else {
                    return FAIL;
                }
            } else {
                return FAIL;
            }
        } else {
            return FAIL;
        }
    }

    @RequestMapping(value = "/validatePin", method = RequestMethod.GET)
    public PinStatus validatePin(@RequestParam("username") String username, @RequestParam("pin") String pin, @RequestParam("syncApps") Boolean syncApps, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) throws CustomException {
        PinStatus pinStatus = new PinStatus();
        if (!jwtFilter.checkValidAuthorization(token, username)) {
            throw new CustomException();
        }
        String SUCCESS = "Entered Pin is valid";
        final String FAIL = "Entered Pin is NOT valid. Please Retry!";
        pinStatus.setStatus(FAIL);
        List<Customer> customerList = customerRepository.findByUsername(username);
        if (customerList.size() > 0) {
            Customer customerObj = customerList.get(0);
            String customerPin = EncryptDecrypt.decryptKey(customerObj.getPin());
            if (!customerPin.equals(pin)) {
                return pinStatus;
            } else {
                pinStatus.setStatus(SUCCESS);
                pinStatus.setAuthKey(EncryptDecrypt.decryptKey(customerObj.getAuthKey()));
                if (syncApps) {
                    List<App> productionApps = new ArrayList<>();
                    List<App> updatesApps = new ArrayList<>();
                    List<App> apps = appRepository.findByEmailAndEnvironment(username, "sandbox");
                    Iterator i = apps.iterator();
                    while (i.hasNext()) {
                        App app = (App) i.next();
                        Optional<App> optional = appRepository.findByAppMigratedId(app.getAppId());
                        App productionApp = null;
                        if (!optional.isPresent()) {
                            productionApp = optional.get();
                        }
                        if (null == productionApp) {
                            App newApp = new App();
                            newApp.setEnvironment("production");
                            newApp.setAppName(app.getAppName());
                            newApp.setAppType(app.getAppType());
                            newApp.setCallBackUrl(app.getCallBackUrl());
                            newApp.setAppMigratedId(app.getAppId());
                            newApp.setEmail(app.getEmail());
                            UUID uuid = UUID.randomUUID();
                            String appKey = uuid.toString();
                            newApp.setAppKey(appKey.substring(appKey.length() - 16));
                            productionApps.add(newApp);
                        } else {
                            productionApp.setEmail(app.getEmail());
                            productionApp.setAppType(app.getAppType());
                            productionApp.setAppName(app.getAppName());
                            productionApp.setCallBackUrl(app.getCallBackUrl());
                            updatesApps.add(productionApp);

                        }
                    }
                    if (productionApps.size() > 0) {
                        appRepository.saveAll(productionApps);
                    }
                    if (updatesApps.size() > 0) {
                        appRepository.saveAll(updatesApps);
                    }
                }
            }
        }
        return pinStatus;
    }

    @PostMapping("/create-statement")
    public Statement createStatement(@RequestBody Statement statement, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, statement.getEmail())) {
            throw new CustomException();
        }
        UUID uuid = UUID.randomUUID();
        String sessionKey = uuid.toString();
        statement.setSessionKey(sessionKey.substring(sessionKey.length() - 16));
        statement = statementRepository.save(statement);
        return statement;
    }

    @PostMapping("/save-kyc/{email}")
    public ResponseEntity<String> saveKYC(@PathVariable String email, @RequestParam("kycDocument") MultipartFile kycDocument, @RequestParam(required = false, name = "kycDocument1") MultipartFile kycDocument1, @RequestParam(required = false, name = "kycDocument2") MultipartFile kycDocument2, @RequestParam(required = false, name = "kycDocument3") MultipartFile kycDocument3) {
        FormData formData = new FormData();
        formData.setKycDocument(kycDocument);
        formData.setKycDocument1(kycDocument1);
        formData.setKycDocument2(kycDocument2);
        formData.setKycDocument3(kycDocument3);
        formData.setEmail(email);
        try {
            fileService.save(formData);

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Files uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload the file");
        }
    }

    @PostMapping("/update-kyc/{email}/{kycDocumentId}")
    public ResponseEntity<String> saveKYC(@PathVariable String email, @PathVariable Long kycDocumentId, @RequestParam("kycDocument") MultipartFile kycDocument, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }

        try {
            fileService.updateFile(kycDocument, kycDocumentId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Files updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not update the file");
        }
    }

    @GetMapping("/get-kyc-documents/{email}")
    public List<KycDocument> getKYCDocuments(@PathVariable String email, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return fileService.getAllFiles(email);
    }

    @GetMapping("/get-kyc-document/{email}/{kycDocumentId}")
    public KycDocument getKYCDocument(@PathVariable String email, @PathVariable Long kycDocumentId, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return fileService.getFile(kycDocumentId);
    }

    @GetMapping("/delete-kyc-document/{email}/{kycDocumentId}")
    public String deleteKYCDocument(@PathVariable String email, @PathVariable Long kycDocumentId, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        fileService.deleteFile(kycDocumentId);
        return "Document deleted";
    }

    @GetMapping("/get-statements/{email}")
    public List<Statement> getStatements(@PathVariable String email, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return statementRepository.findByEmail(email);
    }

    @PostMapping("/collected-pdf/{email}")
    public List<CollectedPDF> getPdfs(@PathVariable String email, @RequestBody ArrayList<String> sessionKeys, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, email)) {
            throw new CustomException();
        }
        return collectedPdfRepository.findBySessionKeyIn(sessionKeys);
    }

    @GetMapping("/delete-statements/{statementId}/{username}")
    public String deleteStatement(@PathVariable Long statementId, @PathVariable String username, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, username)) {
            throw new CustomException();
        }
        statementRepository.deleteById(statementId);
        return "statement deleted";
    }

    @GetMapping("/delete-pdf/{collectedPdfId}/{username}")
    public String deletePdf(@PathVariable Long collectedPdfId, @PathVariable String username, @CookieValue(value = JWTAuthorizationFilter.HEADER, defaultValue = "token") String token) {
        if (!jwtFilter.checkValidAuthorization(token, username)) {
            throw new CustomException();
        }
        collectedPdfRepository.deleteById(collectedPdfId);
        return "pdf deleted";
    }

    private void sendMail(Integer otp, String email) {
        final String username = "no-reply@hadada.co";
        final String password = "%3A%2F%2Fmail";
        String sender = "no-reply@hadada.co";
        String host = "smtp.gmail.com";
        String port = "465";

        Properties properties = new Properties();
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Create session object passing properties and authenticator instance
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Hadada otp verification");
            message.setText("Your otp is " + otp);
            Transport.send(message);
            System.out.println("Mail sent successfully");
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

}
