package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTO;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Suit.SuitRepository;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SuitServiceImpl implements SuitService {

    private final SuitRepository suitRepository;
    private final UserService userService;
    private final FileService fileService;
    private final SuitDTOMapper suitDTOMapper;

    public SuitServiceImpl(SuitRepository suitRepository, UserService userService, FileService fileService, SuitDTOMapper suitDTOMapper) {
        this.suitRepository = suitRepository;
        this.userService = userService;
        this.fileService = fileService;
        this.suitDTOMapper = suitDTOMapper;
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
    public SuitDTO addFacebookPageToSuit(Long suitId, FacebookPage facebookPage) {
        Suit suit = findSuitById(suitId);
        List<FacebookPage> facebookPages = suit.getFacebookPages();
        facebookPages.add(facebookPage);
        suit.setFacebookPages(facebookPages);
        suitRepository.save(suit);
        return suitDTOMapper.apply(suit);
    }

    @Override
    public SuitDTO removeFacebookPageFromSuit(Long suitId, Long pageId) {
        Suit suit = findSuitById(suitId);
        List<FacebookPage> facebookPages = suit.getFacebookPages();
        facebookPages.removeIf(facebookPage -> facebookPage.getId().equals(pageId));
        suit.setFacebookPages(facebookPages);
        suitRepository.save(suit);
        return suitDTOMapper.apply(suit);
    }

    @Override
    public Suit findSuitById(Long suitId) throws ResourceNotFoundException {
        return suitRepository.findById(suitId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Suit with Id : %s was not found",suitId))
        );
    }

}
