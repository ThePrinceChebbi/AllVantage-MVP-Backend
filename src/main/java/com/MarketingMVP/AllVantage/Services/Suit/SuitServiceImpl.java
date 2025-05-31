package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.SmallPostDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.SmallPostDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.Postable.Post.Post;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.Postable.Reel.Reel;
import com.MarketingMVP.AllVantage.Entities.Postable.Story.Story;
import com.MarketingMVP.AllVantage.Entities.Responses.Error.CustomErrorLog;
import com.MarketingMVP.AllVantage.Entities.Responses.Error.ErrorType;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Responses.Success.CustomSuccessLog;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.Post.PostableRepository;
import com.MarketingMVP.AllVantage.Repositories.Suit.SuitRepository;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn.LinkedInService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Instagram.InstagramService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.MetaAuth.MetaAuthService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO: Finish each social media BS independently and then refactor the entire damn thing
//FUCK ME AND MY MISCALCULATIONS

@Service
public class SuitServiceImpl implements SuitService {

    private final SuitRepository suitRepository;
    private final UserService userService;
    private final FileService fileService;
    private final SuitDTOMapper suitDTOMapper;
    private final FacebookService facebookService;
    private final PostableRepository postableRepository;
    private final LinkedInService linkedInService;
    private final MetaAuthService metaAuthService;
    private final InstagramService instagramService;

    public SuitServiceImpl(SuitRepository suitRepository, UserService userService, FileService fileService, SuitDTOMapper suitDTOMapper, FacebookService facebookService, PostableRepository postableRepository, LinkedInService linkedInService, MetaAuthService metaAuthService, InstagramService instagramService) {
        this.suitRepository = suitRepository;
        this.userService = userService;
        this.fileService = fileService;
        this.suitDTOMapper = suitDTOMapper;
        this.facebookService = facebookService;
        this.postableRepository = postableRepository;
        this.linkedInService = linkedInService;
        this.metaAuthService = metaAuthService;
        this.instagramService = instagramService;
    }

    @Override
    public ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file, String suitColor) {
        try{
            Client client = userService.getClientById(clientId);
            FileData fileData = fileService.processUploadedFile(file);

            Suit suit = new Suit();
            suit.setName(name);
            suit.setDescription(description);
            suit.setImage(fileData);
            suit.setSuitColor(suitColor);
            suit.setClient(client);
            suit.setEmployees(new ArrayList<>());
            suit.setFacebookPages(new ArrayList<>());
            suit.setInstagramAccounts(new ArrayList<>());
            suit.setLinkedInOrganizations(new ArrayList<>());

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
            FileData fileData = fileService.processUploadedFile(file);
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

    public <T> List<T> filterAccounts(List<Long> requestedIds, List<T> accounts, Function<T, Long> getIdFunction, PlatformType platformName) {
        if (requestedIds.isEmpty() || accounts.isEmpty()){
            return new ArrayList<>();
        }

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
            FacebookPage facebookPage = metaAuthService.authenticateFacebookPage(accountId,facebookPageId);
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
    public ResponseEntity<Object> addInstagramAccountToSuit(Long suitId, Long pageId, String instagramAccountId) {
        try{
            Suit suit = findSuitById(suitId);
            List<InstagramAccount> instagramAccounts = suit.getInstagramAccounts();
            InstagramAccount instagramAccount = instagramService.addInstagramAccount(instagramAccountId,pageId);
            instagramAccounts.add(instagramAccount);
            suit.setInstagramAccounts(instagramAccounts);
            suitRepository.save(suit);
            return ResponseEntity.status(HttpStatus.CREATED).body(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> addLinkedInOrganizationToSuit(Long suitId, Long accountId, String linkedInOrganizationId) {
        try{
            Suit suit = findSuitById(suitId);
            List<LinkedInOrganization> linkedInOrganizations = suit.getLinkedInOrganizations();
            LinkedInOrganization linkedInOrganization = linkedInService.authenticateLinkedInOrganization(accountId,linkedInOrganizationId);
            linkedInOrganizations.add(linkedInOrganization);
            suit.setLinkedInOrganizations(linkedInOrganizations);
            suitRepository.save(suit);
            return ResponseEntity.status(HttpStatus.CREATED).body(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
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
    public ResponseEntity<Object> getSuitById(Long suitId, UserDetails userDetails) {
        Suit suit = suitRepository.findById(suitId).orElseThrow(
                () -> new ResourceNotFoundException("Suit with id " + suitId + " not found")
        );

        UserEntity user = userService.getUserByUsername(userDetails.getUsername());

        if (user instanceof Client client) {
            if (!suit.getClient().getId().equals(client.getId())) {
                return ResponseEntity.status(401).body("Unauthorized to access this suit, you are not the owner of this suit");
            }
        } else if (user instanceof Employee employee) {
            if (!suit.getEmployees().contains(employee)) {
                return ResponseEntity.status(401).body("Unauthorized to access this suit, you are not an employee of this suit");
            }
        }
        return ResponseEntity.ok(suitDTOMapper.apply(suit));
    }

    @Override
    public ResponseEntity<Object> getAllSuitPosts(Long suitId, int pageNumber, UserDetails userDetails) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            Suit suit = findSuitById(suitId);
            if (user instanceof Client client) {
                if (!suit.getClient().getId().equals(client.getId())) {
                    return ResponseEntity.status(401).body("Unauthorized to access this suit, you are not the owner of this suit");
                }
            } else if (user instanceof Employee employee) {
                if (!suit.getEmployees().contains(employee)) {
                    return ResponseEntity.status(401).body("Unauthorized to access this suit, you are not an employee of this suit");
                }
            }
            List<Postable> posts = suit.getPosts().stream()
                    .filter(p -> p instanceof Postable && !(p instanceof Story))
                    .map(p -> (Postable) p)
                    .sorted(Comparator.comparing(Postable::getCreatedAt).reversed())
                    .toList();

            int pageSize = 10;
            int totalPages = (int) Math.ceil((double) posts.size() / pageSize);
            if (pageNumber < 0 || pageNumber >= totalPages) {
                return ResponseEntity.status(400).body("Invalid page number");
            }
            int startIndex = pageNumber * pageSize;
            int endIndex = Math.min(startIndex + pageSize, posts.size());
            List<SmallPostDTO> paginatedPosts = posts.subList(startIndex, endIndex).stream().map(new SmallPostDTOMapper()).toList();
            return ResponseEntity.ok(paginatedPosts);
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> postToSuit(Long suitId, String postSendDTOJson, UserDetails employeeDetails, List<MultipartFile> files) {
        try{

            boolean hasVideo = files.stream().anyMatch(file -> Objects.requireNonNull(file.getContentType()).startsWith("video/"));
            boolean hasImage = files.stream().anyMatch(file -> Objects.requireNonNull(file.getContentType()).startsWith("image/"));

            if (hasVideo && hasImage) {
                throw new IllegalArgumentException("Cannot upload both video and image files at the same time.");
            }else if (hasVideo && files.size() > 1) {
                throw new IllegalArgumentException("Cannot upload multiple video files.");
            }else if (hasImage && files.size() > 10) {
                throw new IllegalArgumentException("Cannot upload more than 10 image files.");
            }

            PostSendDTO postSendDTO = deserializeJson(postSendDTOJson);

            Suit suit = findSuitById(suitId);
            /*Employee employee = userService.getEmployeeByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(employee)){
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }*/

            List<FileData> fileDataList = files.stream()
                    .map(file -> {
                        try {
                            return fileService.processUploadedFile(file);
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
                    PlatformType.FACEBOOK
            );

            List<InstagramAccount> instagramAccounts = filterAccounts(
                    postSendDTO.getInstagramAccountIds(),
                    suit.getInstagramAccounts(),
                    InstagramAccount::getId,
                    PlatformType.INSTAGRAM
            );

            List<LinkedInOrganization> linkedInOrganizations = filterAccounts(
                    postSendDTO.getLikedInOrganizationIds(),
                    suit.getLinkedInOrganizations(),
                    LinkedInOrganization::getId,
                    PlatformType.LINKEDIN
            );

            int threadNumber = facebookPages.size() + instagramAccounts.size() + linkedInOrganizations.size() /*+ xAccounts.size() + snapchatAccounts.size() + tikTokAccounts.size()*/;
            ExecutorService executor = Executors.newFixedThreadPool(threadNumber); // Adjust pool size as needed
            List<Callable<PlatformPostResult>> tasks = new ArrayList<>();
            List<FacebookPost> facebookPosts = new ArrayList<>();
            List<InstagramPost> instagramPosts = new ArrayList<>();
            List<LinkedinPost> linkedInPosts = new ArrayList<>();

            // Add tasks for each platform
            if (!facebookPages.isEmpty()) {
                for (FacebookPage facebookPage : facebookPages) {
                    tasks.add(() -> facebookService.createFacebookPost(
                            fileDataList,
                            postSendDTO.getTitle(),
                            postSendDTO.getContent(),
                            facebookPage.getId()
                    ));
                }
            }
            if (!instagramAccounts.isEmpty()) {
                for (InstagramAccount instagramAccount : instagramAccounts) {
                    tasks.add(() -> instagramService.createInstagramPost(
                            fileDataList,
                            postSendDTO.getContent(),
                            postSendDTO.getScheduledAt(),
                            instagramAccount));
                }
            }
            if (!linkedInOrganizations.isEmpty()) {
                for (LinkedInOrganization linkedInOrganization : linkedInOrganizations) {
                    tasks.add(() -> linkedInService.createLinkedInPost(
                            fileDataList,
                            postSendDTO.getContent(),
                            linkedInOrganization,
                            false
                    ));
                }
            }
            List<Future<PlatformPostResult>> futures = executor.invokeAll(tasks);
            List<CustomErrorLog> errors = new ArrayList<>();
            for (Future<PlatformPostResult> future : futures) {
                try {
                    PlatformPostResult result = future.get();
                    if (result.isSuccess()) {
                        Object entity = result.getResult();
                        switch (result.getPlatform()) {
                            case FACEBOOK -> facebookPosts.add((FacebookPost) entity);
                            case INSTAGRAM -> instagramPosts.add((InstagramPost) entity);
                            case LINKEDIN -> linkedInPosts.add((LinkedinPost) entity);
                        }
                    } else {
                        CustomErrorLog error = new CustomErrorLog(
                                new Date(),
                                result.getResult().toString(),
                                //employee,
                                null,
                                ErrorType.POST_ERROR,
                                result.getPlatform()
                        );
                        errors.add(error);
                    }
                } catch (Exception e) {
                    CustomErrorLog error = new CustomErrorLog(
                            new Date(),
                            e.getMessage(),
                            null,
                            //employee,
                            ErrorType.UNEXPECTED_ERROR,
                            null
                    );
                    errors.add(error);
                }
            }

            executor.shutdown();

            Post post = new Post();
            post.setTitle(postSendDTO.getTitle());
            post.setContent(postSendDTO.getContent());
            post.setCreatedAt(new Date());
            post.setScheduledToPostAt(postSendDTO.getScheduledAt() != null ? postSendDTO.getScheduledAt() : new Date());
            post.setLastEditedAt(new Date());
            post.setFacebookPosts(facebookPosts);
            post.setInstagramPosts(instagramPosts);
            post.setLinkedinPosts(linkedInPosts);
            post.setEmployee(null);

            Post savedPost= postableRepository.save(post);
            List<Postable> posts= suit.getPosts();
            posts.add(savedPost);
            suit.setPosts(posts);
            suitRepository.save(suit);
            Map<String, Object> response = new HashMap<>();
            response.put("post", savedPost);
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> postReelToSuit(Long suitId, MultipartFile videoFile, String reelPostDTOJson) {
        try{
            PostSendDTO postSendDTO = deserializeJson(reelPostDTOJson);

            Suit suit = findSuitById(suitId);
            /*Employee employee = userService.getEmployeeByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(employee)){
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }*/

            FileData fileData = fileService.processUploadedFile(videoFile);
            List<FacebookPage> facebookPages = filterAccounts(
                    postSendDTO.getFacebookPageIds(),
                    suit.getFacebookPages(),
                    FacebookPage::getId,
                    PlatformType.FACEBOOK
            );
            List<InstagramAccount> instagramAccounts = filterAccounts(
                    postSendDTO.getInstagramAccountIds(),
                    suit.getInstagramAccounts(),
                    InstagramAccount::getId,
                    PlatformType.INSTAGRAM
            );
            List<LinkedInOrganization> linkedInOrganizations = filterAccounts(
                    postSendDTO.getLikedInOrganizationIds(),
                    suit.getLinkedInOrganizations(),
                    LinkedInOrganization::getId,
                    PlatformType.LINKEDIN
            );

            int threadNumber = facebookPages.size() +
                    instagramAccounts.size() +
                    linkedInOrganizations.size();
            int maxThreads = Math.min(threadNumber, 20);

            /*File tempVideoFile = File.createTempFile("upload_" + videoFile.getOriginalFilename(), ".tmp");
            videoFile.transferTo(tempVideoFile);*/
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

            List<Callable<PlatformPostResult>> tasks = new ArrayList<>();
            List<FacebookReel> facebookReels = new ArrayList<>();
            List<InstagramReel> instagramReels = new ArrayList<>();
            List<LinkedinReel> linkedinReels = new ArrayList<>();

            // Add tasks for each platform
            if (!facebookPages.isEmpty()) {
                for (FacebookPage facebookPage : facebookPages) {
                    tasks.add(() -> facebookService.createFacebookReel(
                            fileData,
                            postSendDTO.getTitle(),
                            postSendDTO.getContent(),
                            facebookPage.getId()
                    ));
                }
            }
            if (!instagramAccounts.isEmpty()) {
                for (InstagramAccount instagramAccount : instagramAccounts) {
                    tasks.add(() -> instagramService.createInstagramReel(
                            fileData,
                            postSendDTO.getContent(),
                            postSendDTO.getScheduledAt(),
                            instagramAccount
                    ));
                }
            }
            if (!linkedInOrganizations.isEmpty()) {
                for (LinkedInOrganization linkedInOrganization : linkedInOrganizations) {
                    tasks.add(() -> linkedInService.createLinkedInPost(
                            List.of(fileData),
                            postSendDTO.getContent(),
                            linkedInOrganization,
                            true
                    ));
                }
            }

            List<Future<PlatformPostResult>> futures = executor.invokeAll(tasks, 5, TimeUnit.MINUTES);
            List<CustomErrorLog> errors = new ArrayList<>();
            List<CustomSuccessLog> successLogs = new ArrayList<>();
            for (Future<PlatformPostResult> future : futures) {
                try {
                    PlatformPostResult result = future.get();
                    if (!result.isSuccess() || result.getResult() instanceof String) {
                        CustomErrorLog error = new CustomErrorLog(
                                new Date(),
                                result.getResult().toString(),
                                //employee,
                                null,
                                ErrorType.POST_ERROR,
                                result.getPlatform()
                        );
                        errors.add(error);
                    }else {
                        Object entity = result.getResult();
                        switch (result.getPlatform()) {
                            case FACEBOOK -> facebookReels.add((FacebookReel) entity);
                            case INSTAGRAM -> instagramReels.add((InstagramReel) entity);
                            case LINKEDIN -> linkedinReels.add((LinkedinReel) entity);
                        }
                        CustomSuccessLog success = new CustomSuccessLog(
                                new Date(),
                                result.getResult(),
                                //employee,
                                null,
                                result.getPlatform()
                        );
                        successLogs.add(success);
                    }
                } catch (Exception e) {
                    CustomErrorLog error = new CustomErrorLog(
                            new Date(),
                            e.getMessage(),
                            null,
                            //employee,
                            ErrorType.UNEXPECTED_ERROR,
                            null
                    );
                    errors.add(error);
                }
            }

            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }

            Reel reel = new Reel();
            reel.setTitle(postSendDTO.getTitle());
            reel.setContent(postSendDTO.getContent());
            reel.setCreatedAt(new Date());
            reel.setScheduledToPostAt(postSendDTO.getScheduledAt() != null ? postSendDTO.getScheduledAt() : new Date());
            reel.setLastEditedAt(new Date());
            reel.setFacebookReels(facebookReels);
            reel.setInstagramReels(instagramReels);
            reel.setLinkedinReels(linkedinReels);
            reel.setEmployee(null);

            List<Postable> posts= suit.getPosts();
            posts.add(postableRepository.save(reel));
            suit.setPosts(posts);
            suitRepository.save(suit);
            Map<String, Object> response = new HashMap<>();
            response.put("reel", reel);
            response.put("success", successLogs);
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllSuits() {
        return ResponseEntity.ok(suitRepository.findAll().stream().map(suitDTOMapper).toList());
    }

    @Override
    public ResponseEntity<Object> getPostInsights(Long suitId, Long postId) {
        ExecutorService executor = null;
        try {
            Suit suit = findSuitById(suitId);

            Post post = suit.getPosts().stream()
                    .filter(p -> p.getId().equals(postId) && p instanceof Post)
                    .map(p -> (Post) p)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found in suit " + suitId));

            int threadCount = suit.getFacebookPages().size()
                    + suit.getInstagramAccounts().size()
                    + suit.getLinkedInOrganizations().size();

            executor = Executors.newFixedThreadPool(threadCount > 0 ? threadCount : 1);
            List<Callable<PlatformInsightsResult>> tasks = getCallables(post);
            List<Future<PlatformInsightsResult>> futures = executor.invokeAll(tasks);

            List<CustomErrorLog> errors = new ArrayList<>();
            List<CustomSuccessLog> successLogs = new ArrayList<>();

            for (Future<PlatformInsightsResult> future : futures) {
                try {
                    PlatformInsightsResult result = future.get();
                    if (result.isSuccess()) {
                        successLogs.add(new CustomSuccessLog(
                                new Date(), result.getData(), null, result.getPlatform()));
                    } else {
                        errors.add(new CustomErrorLog(
                                new Date(), result.getMessage(), null, ErrorType.POST_ERROR, result.getPlatform()));
                    }
                } catch (Exception e) {
                    errors.add(new CustomErrorLog(
                            new Date(), e.getMessage(), null, ErrorType.UNEXPECTED_ERROR, null));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("post", post);
            response.put("success", successLogs);
            response.put("errors", errors);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        } finally {
            if (executor != null) executor.shutdown();
        }
    }

    @Override
    public ResponseEntity<Object> getUsersBySuitId(Long suitId) {
        try {
            Suit suit = findSuitById(suitId);
            List<UserEntity> users = new ArrayList<>();
            users.add(suit.getClient());
            users.addAll(suit.getEmployees());
            return ResponseEntity.ok(users.stream().map(new UserDTOMapper()).toList());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    private List<Callable<PlatformInsightsResult>> getCallables(Post post) {
        List<Callable<PlatformInsightsResult>> tasks = new ArrayList<>();
        String metricList = "post_reactions_like_total,post_reactions_love_total,post_reactions_wow_total";

        for (FacebookPost facebookPost : post.getFacebookPosts()) {
            tasks.add(() -> facebookService.getFacebookPostInsights(facebookPost.getPage().getId(), facebookPost.getFacebookPostId(), metricList));
        }
        for (InstagramPost instagramPost : post.getInstagramPosts()) {
            tasks.add(() -> instagramService.getInstagramPostInsights(instagramPost.getAccount().getFacebookPage().getId(), instagramPost.getInstagramPostId(), Arrays.stream(metricList.split(",")).toList()));
        }
        for (LinkedinPost linkedinPost : post.getLinkedinPosts()) {
            tasks.add(() -> linkedInService.getLinkedInInsights(linkedinPost.getOrganization().getId(), linkedinPost.getLinkedinPostId()));
        }
        return tasks;
    }

    private PostSendDTO deserializeJson(String json) throws JsonProcessingException {
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        return PostSendDTO.builder()
                .title(jsonNode.get("title").asText())
                .content(jsonNode.get("content").asText())
                .scheduledAt(jsonNode.get("scheduledAt").asText().isEmpty() ? null : new Date(jsonNode.get("scheduledAt").asLong()))
                .facebookPageIds(jsonNode.get("facebookPageIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                .instagramAccountIds(jsonNode.get("instagramAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                .likedInOrganizationIds(jsonNode.get("linkedInOrganizationIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                .build();
    }

}
