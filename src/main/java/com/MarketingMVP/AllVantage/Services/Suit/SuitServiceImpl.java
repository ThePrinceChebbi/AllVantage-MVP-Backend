package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Post.Post.PostDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Post.Reel.ReelDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.Reel.ReelDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.SmallPost.SmallPostDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.SmallPost.SmallPostDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Post.Story.StoryDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.Story.StoryDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Post.StorySendDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Organization.LinkedInOrganization;
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
import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Exceptions.UnauthorizedActionException;
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
import java.time.LocalDate;
import java.time.ZoneId;
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
    public ResponseEntity<Object> updateSuitInfo(Long suitId, String suitColor, String name, String description, MultipartFile file, UserDetails userDetails) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            if (!(user instanceof Admin)) {
                throw new UnauthorizedActionException("Unauthorized to access this suit");
            }
            Suit suit = findSuitById(suitId);
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");

            suit.setName(name);
            suit.setDescription(description);
            suit.setSuitColor(suitColor);

            if (file != null && !file.isEmpty()) {
                FileData fileData = fileService.processUploadedFile(file);
                suit.setImage(fileData);
                Suit updatedSuit = suitRepository.save(suit);
                return ResponseEntity.ok(suitDTOMapper.apply(updatedSuit));
            }else{
                Suit updatedSuit = suitRepository.save(suit);
                return ResponseEntity.ok(suitDTOMapper.apply(updatedSuit));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> deactivateSuit(Long suitId, UserDetails userDetails) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            if (!(user instanceof Admin)) {
                throw new UnauthorizedActionException("Unauthorized to delete this suit");
            }

            Suit suit = findSuitById(suitId);
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
            suit.setActive(false);
            suitRepository.save(suit);
            return ResponseEntity.status(204).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> reactivateSuit(Long suitId, UserDetails userDetails) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            if (!(user instanceof Admin)) {
                throw new UnauthorizedActionException("Unauthorized to delete this suit");
            }

            Suit suit = findSuitById(suitId);
            if (suit.isActive()) throw new RuntimeException("Suit is already active");
            suit.setActive(true);
            suitRepository.save(suit);
            return ResponseEntity.status(204).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> addEmployeeToSuit(Long suitId, UUID employeeId) {
        try{
            Suit suit = findSuitById(suitId);
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
            Employee employee = userService.getEmployeeById(employeeId);
            if (suit.getEmployees().contains(employee)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee already exists in this suit");
            }
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
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
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
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
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
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
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
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
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
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
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
    public ResponseEntity<Object> removeInstagramAccountFromSuit(Long suitId, Long accountId) {
        try{
            Suit suit = findSuitById(suitId);
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
            List<InstagramAccount> instagramAccounts = suit.getInstagramAccounts();
            instagramAccounts.removeIf(instagramAccount -> instagramAccount.getId().equals(accountId));
            suit.setInstagramAccounts(instagramAccounts);
            suitRepository.save(suit);
            return ResponseEntity.ok(suitDTOMapper.apply(suit));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> removeLinkedInOrgFromSuit(Long suitId, Long orgId) {
        try{
            Suit suit = findSuitById(suitId);
            if (!suit.isActive()) throw new UnauthorizedActionException("Unauthorized to access this suit");
            List<LinkedInOrganization> linkedInOrganizations = suit.getLinkedInOrganizations();
            linkedInOrganizations.removeIf(instagramAccount -> instagramAccount.getId().equals(orgId));
            suit.setLinkedInOrganizations(linkedInOrganizations);
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
            Map<String,Object> map = new HashMap<>();
            map.put("posts", paginatedPosts);
            map.put("totalEntries",suit.getPosts().size());
            return ResponseEntity.ok(map);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> postToSuit(Long suitId, String postSendDTOJson, List<MultipartFile> files, UserDetails employeeDetails) {
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
            UserEntity user = userService.getUserByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(user) && !user.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }

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

            if (errors.size() >= threadNumber) {
                throw new RuntimeException("Post creation failed");
            }

            Date date = new Date();

            Post post = new Post();
            post.setTitle(postSendDTO.getTitle());
            post.setContent(postSendDTO.getContent());
            post.setCreatedAt(date);
            post.setScheduledToPostAt(postSendDTO.getScheduledAt() != null ? postSendDTO.getScheduledAt() : date);
            post.setLastEditedAt(date);
            post.setFacebookPosts(facebookPosts);
            post.setInstagramPosts(instagramPosts);
            post.setLinkedinPosts(linkedInPosts);
            post.setEmployee(user instanceof Employee employee? employee : null);

            Post savedPost= postableRepository.save(post);
            List<Postable> posts= suit.getPosts();
            posts.add(savedPost);
            suit.setPosts(posts);
            suitRepository.save(suit);
            Map<String, Object> response = new HashMap<>();
            response.put("post", new SmallPostDTOMapper().apply(post));
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> postReelToSuit(Long suitId, MultipartFile videoFile, String reelPostDTOJson, UserDetails userDetails) {
        try{
            PostSendDTO postSendDTO = deserializeJson(reelPostDTOJson);

            Suit suit = findSuitById(suitId);

            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            if (!suit.getEmployees().contains(user) && !user.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }

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
                                user,
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
                                user,
                                result.getPlatform()
                        );
                        successLogs.add(success);
                    }
                } catch (Exception e) {
                    CustomErrorLog error = new CustomErrorLog(
                            new Date(),
                            e.getMessage(),
                            user,
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
            if (errors.size() >= threadNumber) {
                throw new RuntimeException("Reel creation failed");
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
            reel.setEmployee(user instanceof Employee employee ? employee : null);

            List<Postable> posts= suit.getPosts();
            posts.add(postableRepository.save(reel));
            suit.setPosts(posts);
            suitRepository.save(suit);
            ReelDTO reelDTO = new ReelDTOMapper(userService).apply(reel);
            Map<String, Object> response = new HashMap<>();
            response.put("reel", reelDTO);
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
    public ResponseEntity<Object> createStory(Long suitId, MultipartFile videoFile, String reelPostDTOJson, UserDetails userDetails) {
        try{
            JsonNode jsonNode = new ObjectMapper().readTree(reelPostDTOJson);
            StorySendDTO storySendDTO = StorySendDTO.builder()
                    .title(jsonNode.get("title").asText())
                    .scheduledAt(jsonNode.get("scheduledAt").asText().isEmpty() ? null : new Date(jsonNode.get("scheduledAt").asLong()))
                    .facebookPageIds(jsonNode.get("facebookPageIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .instagramAccountIds(jsonNode.get("instagramAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .build();

            Suit suit = findSuitById(suitId);

            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            if (!suit.getEmployees().contains(user) && !user.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }

            FileData fileData = fileService.processUploadedFile(videoFile);

            List<FacebookPage> facebookPages = filterAccounts(
                    storySendDTO.getFacebookPageIds(),
                    suit.getFacebookPages(),
                    FacebookPage::getId,
                    PlatformType.FACEBOOK
            );
            List<InstagramAccount> instagramAccounts = filterAccounts(
                    storySendDTO.getInstagramAccountIds(),
                    suit.getInstagramAccounts(),
                    InstagramAccount::getId,
                    PlatformType.INSTAGRAM
            );


            int threadNumber = facebookPages.size() +
                    instagramAccounts.size();
            int maxThreads = Math.min(threadNumber, 20);

            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

            List<Callable<PlatformPostResult>> tasks = new ArrayList<>();
            List<FacebookStory> facebookStories = new ArrayList<>();
            List<InstagramStory> instagramStories = new ArrayList<>();

            // Add tasks for each platform
            if (!facebookPages.isEmpty()) {
                for (FacebookPage facebookPage : facebookPages) {
                    tasks.add(() -> facebookService.storyOnFacebookPage(
                            fileData,
                            storySendDTO.getTitle(),
                            facebookPage.getId()
                    ));
                }
            }
            if (!instagramAccounts.isEmpty()) {
                for (InstagramAccount instagramAccount : instagramAccounts) {
                    tasks.add(() -> instagramService.createInstagramStory(
                            fileData,
                            storySendDTO.getScheduledAt(),
                            instagramAccount.getId()
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
                                user,
                                ErrorType.POST_ERROR,
                                result.getPlatform()
                        );
                        errors.add(error);
                    }else {
                        Object entity = result.getResult();
                        switch (result.getPlatform()) {
                            case FACEBOOK -> facebookStories.add((FacebookStory) entity);
                            case INSTAGRAM -> instagramStories.add((InstagramStory) entity);
                        }
                        CustomSuccessLog success = new CustomSuccessLog(
                                new Date(),
                                result.getResult(),
                                user,
                                result.getPlatform()
                        );
                        successLogs.add(success);
                    }
                } catch (Exception e) {
                    CustomErrorLog error = new CustomErrorLog(
                            new Date(),
                            e.getMessage(),
                            user,
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
            if (errors.size() >= threadNumber) {
                throw new RuntimeException("Reel creation failed");
            }

            Story story = new Story();
            story.setTitle(storySendDTO.getTitle());
            story.setContent("");
            story.setCreatedAt(new Date());
            story.setScheduledToPostAt(storySendDTO.getScheduledAt() != null ? storySendDTO.getScheduledAt() : new Date());
            story.setLastEditedAt(new Date());
            story.setFacebookStories(facebookStories);
            story.setInstagramStories(instagramStories);
            story.setEmployee(user instanceof Employee employee ? employee : null);

            List<Postable> posts= suit.getPosts();
            posts.add(postableRepository.save(story));
            suit.setPosts(posts);
            suitRepository.save(suit);
            StoryDTO storyDTO = new StoryDTOMapper(userService).apply(story);
            Map<String, Object> response = new HashMap<>();
            response.put("story", storyDTO);
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
    public ResponseEntity<Object> getAllSuits(UserDetails userDetails) {
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        if (user instanceof Client client) {
            List<Suit> suits = suitRepository.findByClientId(client.getId());
            if (suits.isEmpty()) {
                return ResponseEntity.status(404).body("No suits found for this client");
            }
            return ResponseEntity.ok(suits.stream().map(suitDTOMapper).toList());
        } else if (user instanceof Employee employee) {
            List<Suit> suits = userService.getEmployeeById(employee.getId()).getSuits();
            if (suits.isEmpty()) {
                return ResponseEntity.status(404).body("No suits found for this employee");
            }
            return ResponseEntity.ok(suits.stream().map(suitDTOMapper).toList());
        }

        return ResponseEntity.ok(suitRepository.findAll().stream().map(suitDTOMapper).toList());
    }

    @Override
    public ResponseEntity<Object> getPostInsights(Long suitId, Long postId) {
        ExecutorService executor = null;
        try {
            Suit suit = findSuitById(suitId);

            Postable postable = suit.getPosts().stream()
                    .filter(p -> p.getId().equals(postId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found in suit " + suitId));

            List<Callable<PlatformInsightsResult>> tasks = getCallables((postable));
            if (tasks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No platforms available for insights");
            }
            int threadCount = suit.getFacebookPages().size()
                    + suit.getInstagramAccounts().size()
                    + suit.getLinkedInOrganizations().size();

            executor = Executors.newFixedThreadPool(threadCount > 0 ? threadCount : 1);
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
            if (postable instanceof Post post) response.put("post", new PostDTOMapper(userService).apply(post));
            if (postable instanceof Reel reel) response.put("post", new ReelDTOMapper(userService).apply(reel));
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

    @Override
    public ResponseEntity<Object> getPostById(Long suitId, Long postId) {
        Suit suit = suitRepository.findById(suitId).orElseThrow(
                () -> new ResourceNotFoundException("Suit with id " + suitId + " not found")
        );
        Postable postable = suit.getPosts().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found in suit " + suitId));
        if (postable instanceof Post post) {
            return ResponseEntity.ok(new PostDTOMapper(userService).apply(post));
        } else if (postable instanceof Reel reel) {
            return ResponseEntity.ok(new ReelDTOMapper(userService).apply(reel));
        } else {
            return ResponseEntity.status(404).body("Post not found");
        }
    }

    @Override
    public ResponseEntity<Object> getPostingFrequency(Long suitId) {
        try {
            findSuitById(suitId);
            List<Map<String, Object>> analyticsData = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                Date queryDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

                int postCount = suitRepository.getPostCountPerDay(suitId, queryDate);
                int reelCount = suitRepository.getReelCountPerDay(suitId, queryDate);

                Map<String, Object> postEntry = new HashMap<>();
                postEntry.put("event", "Posts");
                postEntry.put("count", postCount);
                postEntry.put("timeStamp", queryDate);
                analyticsData.add(postEntry);

                Map<String, Object> reelEntry = new HashMap<>();
                reelEntry.put("event", "Reels");
                reelEntry.put("count", reelCount);
                reelEntry.put("timeStamp", queryDate);
                analyticsData.add(reelEntry);
            }

            return ResponseEntity.ok(Map.of("analyticsData", analyticsData));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllPostingFrequencies(UserDetails userDetails) {
        try {

            UserEntity user = userService.getUserByUsername(userDetails.getUsername());

            List<Map<String, Object>> analyticsData = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                Date queryDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

                int postCount = 0;
                int reelCount = 0;
                if (user.getRole().getName().equals("ADMIN")) {
                    postCount = postableRepository.getAllPostCountPerDay(queryDate);
                    reelCount = postableRepository.getAllReelCountPerDay(queryDate);
                } else if (user.getRole().getName().equals("EMPLOYEE")) {
                    postCount = postableRepository.getEmployeePostCountPerDay(queryDate, user.getId());
                    reelCount = postableRepository.getEmployeeReelCountPerDay(queryDate, user.getId());
                } else if (user.getRole().getName().equals("CLIENT") && user instanceof Client client) {
                    for (Suit suit : client.getSuits() ) {
                        postCount += suitRepository.getPostCountPerDay(suit.getId(), queryDate);
                        reelCount += suitRepository.getReelCountPerDay(suit.getId(), queryDate);
                    }
                } else {
                    return ResponseEntity.status(403).body("Unauthorized to access this data");
                }

                Map<String, Object> postEntry = new HashMap<>();
                postEntry.put("event", "Posts");
                postEntry.put("count", postCount);
                postEntry.put("timeStamp", queryDate);
                analyticsData.add(postEntry);

                Map<String, Object> reelEntry = new HashMap<>();
                reelEntry.put("event", "Reels");
                reelEntry.put("count", reelCount);
                reelEntry.put("timeStamp", queryDate);
                analyticsData.add(reelEntry);
            }

            return ResponseEntity.ok(Map.of("analyticsData", analyticsData));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> deletePostFromSuit(Long suitId, Long postId, UserDetails userDetails) {
        try {
            Suit suit = findSuitById(suitId);
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());

            Postable postable = suit.getPosts().stream()
                    .filter(p -> p.getId().equals(postId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found in suit " + suitId));

            UserEntity postOwner = postable.getEmployee();

            if (postOwner == null) {
                if (!user.getRole().getName().equals("ADMIN")) {
                    return ResponseEntity.status(401).body("Unauthorized to delete this post");
                }
            } else if (!postOwner.equals(user)) {
                return ResponseEntity.status(401).body("Unauthorized to delete this post");
            }

            // Remove post from suit and save
            suit.getPosts().remove(postable);
            suitRepository.save(suit);

            int threadCount = 0;
            List<Callable<String>> tasks = new ArrayList<>();

            if (postable instanceof Post post) {
                threadCount = post.getLinkedinPosts().size()
                        + post.getInstagramPosts().size()
                        + post.getFacebookPosts().size();

                // Save references before clearing
                List<LinkedinPost> liPosts = new ArrayList<>(post.getLinkedinPosts());
                List<InstagramPost> igPosts = new ArrayList<>(post.getInstagramPosts());
                List<FacebookPost> fbPosts = new ArrayList<>(post.getFacebookPosts());

                post.getLinkedinPosts().clear();
                post.getInstagramPosts().clear();
                post.getFacebookPosts().clear();
                postableRepository.save(post);

                // Deletion tasks (after clearing)
                for (LinkedinPost li : liPosts) {
                    tasks.add(() -> linkedInService.deleteLinkedInPost(li));
                }
                for (InstagramPost inst : igPosts) {
                    tasks.add(() -> instagramService.deleteInstagramPost(inst));
                }
                for (FacebookPost fb : fbPosts) {
                    tasks.add(() -> facebookService.deleteFacebookPost(fb));
                }

            } else if (postable instanceof Reel reel) {
                threadCount = reel.getLinkedinReels().size()
                        + reel.getInstagramReels().size()
                        + reel.getFacebookReels().size();

                List<LinkedinReel> liReels = new ArrayList<>(reel.getLinkedinReels());
                List<InstagramReel> igReels = new ArrayList<>(reel.getInstagramReels());
                List<FacebookReel> fbReels = new ArrayList<>(reel.getFacebookReels());

                reel.getLinkedinReels().clear();
                reel.getInstagramReels().clear();
                reel.getFacebookReels().clear();
                postableRepository.save(reel);

                for (LinkedinReel li : liReels) {
                    tasks.add(() -> linkedInService.deleteLinkedInReel(li));
                }
                for (InstagramReel inst : igReels) {
                    tasks.add(() -> instagramService.deleteInstagramReel(inst));
                }
                for (FacebookReel fb : fbReels) {
                    tasks.add(() -> facebookService.deleteFacebookReel(fb));
                }

            } else {
                return ResponseEntity.status(400).body("Invalid post type");
            }

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadCount, 10));
            List<Future<String>> results = executor.invokeAll(tasks);
            executor.shutdown();

            for (Future<String> result : results) {
                result.get();
            }

            postableRepository.delete(postable);

            return ResponseEntity.ok("Post deleted successfully");

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    private List<Callable<PlatformInsightsResult>> getCallables(Postable postable) {
        List<Callable<PlatformInsightsResult>> tasks = new ArrayList<>();
        String facebookMetricList = "post_impressions,post_reactions_like_total,post_video_views,post_clicks,post_reactions_by_type_total";
        String instagramMetricList = "reach,saved,likes,total_interactions";

        if (postable instanceof Post post ){
            for (FacebookPost facebookPost : post.getFacebookPosts()) {
                tasks.add(() -> facebookService.getFacebookPostInsights(facebookPost.getPage().getId(), facebookPost.getFacebookPostId(), facebookMetricList));
            }
            for (InstagramPost instagramPost : post.getInstagramPosts()) {
                tasks.add(() -> instagramService.getInstagramPostInsights(instagramPost.getAccount().getId(), instagramPost.getInstagramPostId(), Arrays.stream(instagramMetricList.split(",")).toList()));
            }
            for (LinkedinPost linkedinPost : post.getLinkedinPosts()) {
                tasks.add(() -> linkedInService.getLinkedInInsights(linkedinPost.getOrganization().getId(), linkedinPost.getLinkedinPostId()));
            }
        } else if (postable instanceof Reel reel) {
            for (FacebookReel facebookReel : reel.getFacebookReels()) {
                tasks.add(() -> facebookService.getFacebookReelInsights(facebookReel.getPage().getId(), facebookReel.getFacebookVideoId(), facebookMetricList));
            }
            for (InstagramReel instagramReel : reel.getInstagramReels()) {
                tasks.add(() -> instagramService.getInstagramReelsInsights(instagramReel.getAccount().getFacebookPage().getId(), instagramReel.getInstagramReelId(), Arrays.stream(instagramMetricList.split(",")).toList()));
            }
            for (LinkedinReel linkedinReel : reel.getLinkedinReels()) {
                tasks.add(() -> linkedInService.getLinkedInInsights(linkedinReel.getOrganization().getId(), linkedinReel.getLinkedinReelId()));
            }
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
