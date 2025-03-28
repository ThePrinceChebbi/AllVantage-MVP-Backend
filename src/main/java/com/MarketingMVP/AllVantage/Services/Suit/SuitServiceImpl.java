package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.PlatformPostResult;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Snapchat.SnapchatPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.X.XPost;
import com.MarketingMVP.AllVantage.Entities.Responses.Error.CustomErrorLog;
import com.MarketingMVP.AllVantage.Entities.Responses.Error.ErrorType;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Postable.Reel.Reel;
import com.MarketingMVP.AllVantage.Entities.Responses.Success.CustomSuccessLog;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Repositories.Post.PostableRepository;
import com.MarketingMVP.AllVantage.Repositories.Suit.SuitRepository;
import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public SuitServiceImpl(SuitRepository suitRepository, UserService userService, FileService fileService, SuitDTOMapper suitDTOMapper, FacebookService facebookService, PostableRepository postableRepository) {
        this.suitRepository = suitRepository;
        this.userService = userService;
        this.fileService = fileService;
        this.suitDTOMapper = suitDTOMapper;
        this.facebookService = facebookService;
        this.postableRepository = postableRepository;
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
    public ResponseEntity<Object> postToSuit(Long suitId, String postSendDTOJson, UserDetails employeeDetails, List<MultipartFile> files) {
        try{
            JsonNode jsonNode = new ObjectMapper().readTree(postSendDTOJson);
            PostSendDTO postSendDTO = PostSendDTO.builder()
                    .title(jsonNode.get("title").asText())
                    .content(jsonNode.get("content").asText())
                    .scheduledAt(jsonNode.get("scheduledAt").asText().isEmpty() ? null : new Date(jsonNode.get("scheduledAt").asLong()))
                    .facebookPageIds(jsonNode.get("facebookPageIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .instagramAccountIds(jsonNode.get("instagramAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .linkedInAccountIds(jsonNode.get("linkedInAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .xAccountIds(jsonNode.get("xAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .snapchatAccountIds(jsonNode.get("snapchatAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .tikTokAccountIds(jsonNode.get("tikTokAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .build();

            Suit suit = findSuitById(suitId);
            /*Employee employee = userService.getEmployeeByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(employee)){
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }*/

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


            /*Post post = new Post(
                    postSendDTO.getTitle(),
                    postSendDTO.getContent(),
                    new Date(),
                    postSendDTO.getScheduledAt(),
                    new Date(),
                    //employee,
                    null,
                    facebookPages,
                    instagramAccounts,
                    linkedInAccounts,
                    xAccounts,
                    snapchatAccounts,
                    tikTokAccounts,
                    fileDataList
            );*/

            int threadNumber = facebookPages.size() + instagramAccounts.size() + xAccounts.size() + linkedInAccounts.size() + snapchatAccounts.size() + tikTokAccounts.size();
            ExecutorService executor = Executors.newFixedThreadPool(threadNumber); // Adjust pool size as needed
            List<Callable<PlatformPostResult>> tasks = new ArrayList<>();
            List<FacebookPost> facebookPosts = new ArrayList<>();
            List<InstagramPost> instagramPosts = new ArrayList<>();
            List<SnapchatPost> snapchatPosts = new ArrayList<>();
            List<XPost> xPosts = new ArrayList<>();
            List<LinkedinPost> linkedInPosts = new ArrayList<>();

            // Add tasks for each platform
            if (!facebookPages.isEmpty()) {
                for (FacebookPage facebookPage : facebookPages) {
                    tasks.add(() -> facebookService.createFacebookPost(
                            fileDataList,
                            postSendDTO.getTitle(),
                            postSendDTO.getContent(),
                            postSendDTO.getScheduledAt(),
                            facebookPage
                    ));
                }
            }
            /*if (!instagramAccounts.isEmpty()) {
                tasks.add(() -> instagramService.postToInstagram(post, instagramAccounts));
            }
            if (!xAccounts.isEmpty()) {
                tasks.add(() -> xService.postToX(post, xAccounts));
            }
            if (!linkedInAccounts.isEmpty()) {
                tasks.add(() -> linkedInService.postToLinkedIn(post, linkedInAccounts));
            }*/
            List<Future<PlatformPostResult>> futures = executor.invokeAll(tasks);
            List<CustomErrorLog> errors = new ArrayList<>();
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
            /*List<Postable> posts= suit.getPosts();
            posts.add(postableRepository.save(post));
            suit.setPosts(posts);
            suitRepository.save(suit);
            Map<String, Object> response = new HashMap<>();
            response.put("suit", suitDTOMapper.apply(suit));
            response.put("errors", errors);
            return ResponseEntity.ok(response);
            */
            return ResponseEntity.ok("Post successful");
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
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

    /*private <T> List<T> makePostList(List<T> accounts, Function<T, Long> constructor) {

    }*/

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
    public FacebookMedia test(Long fileId, Long accountId) {
        return facebookService.uploadMediaToFacebook(fileService.getFileDataById(fileId),accountId);
    }

    @Override
    public ResponseEntity<Object> postToFacebook(Long suitId, List<MultipartFile> files, String title, String content, Date scheduledAt, Long facebookPageId) {
        return ResponseEntity.ok(facebookService.createFacebookPostDirectly(
                files,
                title,
                content,
                scheduledAt,
                facebookPageId
        ));
    }

    @Override
    public ResponseEntity<Object> postReelToSuit(Long suitId, MultipartFile videoFile, String reelPostDTOJson) {
        try{
            JsonNode jsonNode = new ObjectMapper().readTree(reelPostDTOJson);
            PostSendDTO postSendDTO = PostSendDTO.builder()
                    .title(jsonNode.get("title").asText())
                    .content(jsonNode.get("content").asText())
                    .scheduledAt(jsonNode.get("scheduledAt").asText().isEmpty() ? null : new Date(jsonNode.get("scheduledAt").asLong()))
                    .facebookPageIds(jsonNode.get("facebookPageIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .instagramAccountIds(jsonNode.get("instagramAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .linkedInAccountIds(jsonNode.get("linkedInAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .xAccountIds(jsonNode.get("xAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .snapchatAccountIds(jsonNode.get("snapchatAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .tikTokAccountIds(jsonNode.get("tikTokAccountIds").findValuesAsText("id").stream().map(Long::parseLong).toList())
                    .build();

            Suit suit = findSuitById(suitId);
            /*Employee employee = userService.getEmployeeByUsername(employeeDetails.getUsername());
            if (!suit.getEmployees().contains(employee)){
                return ResponseEntity.status(401).body("Unauthorized to post to this suit");
            }*/

            FileData fileData = fileService.processUploadedFile(videoFile, videoFile.getContentType());
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

          /*  Reel reel = new Reel(
                    postSendDTO.getTitle(),
                    postSendDTO.getContent(),
                    new Date(),
                    postSendDTO.getScheduledAt(),
                    new Date(),
                    //employee,
                    null,
                    facebookPages,
                    instagramAccounts,
                    linkedInAccounts,
                    xAccounts,
                    snapchatAccounts,
                    tikTokAccounts,
                    fileData
            );*/

            int threadNumber = facebookPages.size() +
                    instagramAccounts.size() +
                    xAccounts.size() +
                    linkedInAccounts.size() +
                    snapchatAccounts.size() +
                    tikTokAccounts.size();

            int maxThreads = Math.min(threadNumber, 20);

            /*File tempVideoFile = File.createTempFile("upload_" + videoFile.getOriginalFilename(), ".tmp");
            videoFile.transferTo(tempVideoFile);*/
            File savedVideoFile = fileService.getFileFromFileData(fileData);
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

            List<Callable<PlatformPostResult>> tasks = new ArrayList<>();
            // Add tasks for each platform
            if (!facebookPages.isEmpty()) {
                for (FacebookPage facebookPage : facebookPages) {
                    tasks.add(() -> facebookService.createFacebookReel(
                            savedVideoFile,
                            postSendDTO.getTitle(),
                            postSendDTO.getContent(),
                            postSendDTO.getScheduledAt(),
                            facebookPage.getId()
                    ));
                }
            }
            /*if (!instagramAccounts.isEmpty()) {
                tasks.add(() -> instagramService.postToInstagram(post, instagramAccounts));
            }
            if (!xAccounts.isEmpty()) {
                tasks.add(() -> xService.postToX(post, xAccounts));
            }
            if (!linkedInAccounts.isEmpty()) {
                tasks.add(() -> linkedInService.postToLinkedIn(post, linkedInAccounts));
            }*/
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

            /*List<Postable> posts= suit.getPosts();
            posts.add(postableRepository.save(reel));
            suit.setPosts(posts);
            suitRepository.save(suit);
            Map<String, Object> response = new HashMap<>();
            response.put("post", reel);
            response.put("success", successLogs);
            response.put("errors", errors);
            return ResponseEntity.ok(response);*/
            return ResponseEntity.ok("Reel posted successfully");
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
