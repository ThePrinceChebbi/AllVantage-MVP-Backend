package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.Post.Post;
import com.MarketingMVP.AllVantage.Repositories.Suit.SuitRepository;
import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SuitServiceImpl implements SuitService {

    private final SuitRepository suitRepository;
    private final UserService userService;
    private final FileService fileService;
    private final SuitDTOMapper suitDTOMapper;
    private final FacebookService facebookService;

    public SuitServiceImpl(SuitRepository suitRepository, UserService userService, FileService fileService, SuitDTOMapper suitDTOMapper, FacebookService facebookService) {
        this.suitRepository = suitRepository;
        this.userService = userService;
        this.fileService = fileService;
        this.suitDTOMapper = suitDTOMapper;
        this.facebookService = facebookService;
    }

    @Override
    public ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file) {
        try{
            Client client = userService.getClientById(clientId);
            FileData fileData = fileService.processUploadedFile(file,"image");

            Suit suit = new Suit();
            suit.setName(name);
            suit.setDescription(description);
            suit.setImage(fileData);
            suit.setClient(client);
            suit.setEmployees(new ArrayList<>());
            suit.setFacebookPages(new ArrayList<>());
            suit.setInstagramAccounts(new ArrayList<>());
            suit.setLinkedInAccounts(new ArrayList<>());
            suit.setXAccounts(new ArrayList<>());
            suit.setSnapchatAccounts(new ArrayList<>());
            suit.setTikTokAccounts(new ArrayList<>());

            suitRepository.save(suit);

            return ResponseEntity.status(201).body(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> updateSuitInfo(Long suitId, String name, String description) {
        try{
            Suit suit = findSuitById(suitId);
            suit.setName(name);
            suit.setDescription(description);
            return ResponseEntity.ok(suitDTOMapper.apply(suitRepository.save(suit)));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> updateSuitImage(Long suitId, MultipartFile file) {
        try{
            Suit suit = findSuitById(suitId);
            FileData originalFile = suit.getImage();
            FileData fileData = fileService.processUploadedFile(file, file.getContentType());
            suit.setImage(fileData);
            Suit updatedSuit = suitRepository.save(suit);
            fileService.deleteFileFromFileSystem(originalFile);
            return ResponseEntity.ok(suitDTOMapper.apply(updatedSuit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


    //TODO: Implement deleteSuit when other de-authentication methods are implemented
    @Override
    public ResponseEntity<Object> deleteSuit(Long suitId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> addEmployeeToSuit(Long suitId, UUID employeeId) {
        try{
            Suit suit = findSuitById(suitId);
            Employee employee = userService.getEmployeeById(employeeId);
            List<Employee> employees = suit.getEmployees();
            employees.add(employee);
            suit.setEmployees(employees);
            suitRepository.save(suit);
            return ResponseEntity.ok(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> removeEmployeeFromSuit(Long suitId, UUID employeeId) {
        try{
            Suit suit = findSuitById(suitId);
            List<Employee> employees = suit.getEmployees();
            employees.removeIf(employee -> employee.getId().equals(employeeId));
            suit.setEmployees(employees);
            suitRepository.save(suit);
            return ResponseEntity.ok(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    //TODO: Implement the full post - platform relationship accounting for media Ids and facebook post ids
    @Transactional
    @Override
    public ResponseEntity<Object> postToSuit(Long suitId, PostSendDTO postSendDTO, UserDetails employeeDetails, List<MultipartFile> files) {
        try{
            Suit suit = findSuitById(suitId);
            Employee employee = userService.getEmployeeByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(employee)){
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }

            List<FileData> fileDataList = files.stream()
                    .map(file -> {
                        try {
                            return fileService.processUploadedFile(file, file.getContentType());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            return null;
                        }
                    })
                    .toList();

            List<FacebookPage> facebookPages = filterAccounts(
                    postSendDTO.getFacebookPageIds(),
                    suit.getFacebookPages(),
                    FacebookPage::getId,
                    PlatformType.FACEBOOK_PAGE
            );
            List<InstagramAccount> instagramAccounts = filterAccounts(
                    postSendDTO.getInstagramAccountIds(),
                    suit.getInstagramAccounts(),
                    InstagramAccount::getId,
                    PlatformType.INSTAGRAM
            );
            List<LinkedInAccount> linkedInAccounts = filterAccounts(
                    postSendDTO.getLinkedInAccountIds(),
                    suit.getLinkedInAccounts(),
                    LinkedInAccount::getId,
                    PlatformType.LINKEDIN
            );
            List<XAccount> xAccounts = filterAccounts(
                    postSendDTO.getXAccountIds(),
                    suit.getXAccounts(),
                    XAccount::getId,
                    PlatformType.X
            );
            List<SnapchatAccount> snapchatAccounts = filterAccounts(
                    postSendDTO.getSnapchatAccountIds(),
                    suit.getSnapchatAccounts(),
                    SnapchatAccount::getId,
                    PlatformType.SNAPCHAT
            );
            List<TikTokAccount> tikTokAccounts = filterAccounts(
                    postSendDTO.getTikTokAccountIds(),
                    suit.getTikTokAccounts(),
                    TikTokAccount::getId,
                    PlatformType.TIKTOK
            );

            Post post = new Post(
                    postSendDTO.getTitle(),
                    postSendDTO.getContent(),
                    fileDataList,
                    new Date(),
                    postSendDTO.getScheduledAt(),
                    new Date(),
                    employee,
                    facebookPages,
                    instagramAccounts,
                    linkedInAccounts,
                    xAccounts,
                    snapchatAccounts,
                    tikTokAccounts
            );
            suit.getPosts().add(post);
            suitRepository.save(suit);
            return ResponseEntity.ok(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    public <T> List<T> filterAccounts(List<Long> requestedIds, List<T> accounts, Function<T, Long> getIdFunction, PlatformType platformName) {
        Set<Long> existingIds = accounts.stream()
                .map(getIdFunction)
                .collect(Collectors.toSet());

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new IllegalArgumentException(platformName.toString() + " accounts with IDs " + missingIds + " do not belong to this suit.");
        }

        return accounts.stream()
                .filter(account -> requestedIds.contains(getIdFunction.apply(account)))
                .toList();
    }

    @Override
    public ResponseEntity<Object> addFacebookPageToSuit(Long suitId, Long accountId, String facebookPageId) {
        try{
            Suit suit = findSuitById(suitId);
            List<FacebookPage> facebookPages = suit.getFacebookPages();
            FacebookPage facebookPage = facebookService.authenticateFacebookPage(accountId,facebookPageId);
            facebookPages.add(facebookPage);
            suit.setFacebookPages(facebookPages);
            suitRepository.save(suit);
            return ResponseEntity.status(HttpStatus.CREATED).body(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> removeFacebookPageFromSuit(Long suitId, Long pageId) {
        try{
            Suit suit = findSuitById(suitId);
            List<FacebookPage> facebookPages = suit.getFacebookPages();
            facebookPages.removeIf(facebookPage -> facebookPage.getId().equals(pageId));
            suit.setFacebookPages(facebookPages);
            suitRepository.save(suit);
            return ResponseEntity.ok(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public Suit findSuitById(Long suitId) throws ResourceNotFoundException {
        return suitRepository.findById(suitId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Suit with Id : %s was not found",suitId))
        );
    }

    @Override
    public ResponseEntity<Object> getAllClientSuits(UUID clientId) {
        if (!userService.clientExists(clientId)){
            return ResponseEntity.status(404).body("Client not found");
        }
        return suitRepository.findByClientId(clientId).isEmpty() ?
                ResponseEntity.status(404).body("No Suits found for this client") :
                ResponseEntity.ok(suitRepository.findByClientId(clientId).stream().map(suitDTOMapper).toList());
    }

    @Override
    public ResponseEntity<Object> getSuitById(Long suitId) {
        return ResponseEntity.ok(suitRepository.findById(suitId).map(suitDTOMapper));
    }

    @Override
    public String test(Long fileId, Long accountId) {
        return facebookService.uploadMediaToFacebook(fileService.getFileDataById(fileId),accountId);
    }

}
