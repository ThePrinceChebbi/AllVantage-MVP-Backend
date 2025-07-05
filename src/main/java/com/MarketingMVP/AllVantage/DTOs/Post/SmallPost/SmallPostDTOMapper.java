package com.MarketingMVP.AllVantage.DTOs.Post.SmallPost;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Postable.Post.Post;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.Postable.Reel.Reel;
import com.MarketingMVP.AllVantage.Entities.Postable.Story.Story;

import java.util.Objects;
import java.util.function.Function;

public class SmallPostDTOMapper implements Function<Postable, SmallPostDTO> {

    @Override
    public SmallPostDTO apply(Postable postable) {
        if (postable == null) {
            return null;
        }

        FileData thumbnail = null;

        if (postable instanceof Post post) {
            if (!post.getFacebookPosts().isEmpty()) {
                thumbnail = post.getFacebookPosts().get(0).getFacebookMediaList().get(0).getFile();
            } else if (!post.getInstagramPosts().isEmpty()) {
                thumbnail = post.getInstagramPosts().get(0).getInstagramMediaList().get(0).getFile();
            } else if (!post.getLinkedinPosts().isEmpty()) {
                thumbnail = post.getLinkedinPosts().get(0).getLinkedinMediaList().get(0).getFile();
            }
        } else if (postable instanceof Reel reel) {
            if (!reel.getFacebookReels().isEmpty()) {
                thumbnail = reel.getFacebookReels().get(0).getFile();
            } else if (!reel.getInstagramReels().isEmpty()) {
                thumbnail = reel.getInstagramReels().get(0).getInstagramMedia().getFile();
            } else if (!reel.getLinkedinReels().isEmpty()) {
                thumbnail = reel.getLinkedinReels().get(0).getLinkedinMedia().getFile();
            }
        } else if (postable instanceof Story story) {
            if (!story.getFacebookStories().isEmpty()) {
                thumbnail = story.getFacebookStories().get(0).getFacebookMedia().getFile();
            } else if (!story.getInstagramStories().isEmpty()) {
                thumbnail = story.getInstagramStories().get(0).getInstagramMedia().getFile();
            }
        }

        String creatorUsername;
        String thumbnailUrl;
        if (postable.getEmployee() == null) {
            creatorUsername = "ADMIN";
        } else {
            creatorUsername = postable.getEmployee().getUsername();
        }
        if (thumbnail != null && Objects.equals(thumbnail.getType(), "image")) {
            thumbnailUrl = "http://localhost:8080/api/v1/files/"+ thumbnail.getId();
        } else {
            thumbnailUrl = "http://localhost:8080/api/v1/files/357";
        }

        return SmallPostDTO.builder()
                .id(postable.getId())
                .title(postable.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .creatorUsername(creatorUsername)
                .date(String.valueOf(postable.getCreatedAt()))
                .build();
    }
}

