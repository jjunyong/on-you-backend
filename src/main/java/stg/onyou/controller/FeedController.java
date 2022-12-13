package stg.onyou.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stg.onyou.exception.CustomException;
import stg.onyou.exception.ErrorCode;
import stg.onyou.model.Reason;
import stg.onyou.model.entity.*;
import stg.onyou.model.network.request.FeedSearch;
import stg.onyou.model.network.Header;
import stg.onyou.model.network.request.FeedCreateRequest;
import stg.onyou.model.network.request.FeedUpdateRequest;
import stg.onyou.model.network.response.ClubPageResponse;
import stg.onyou.model.network.response.CommentResponse;
import stg.onyou.model.network.response.FeedPageResponse;
import stg.onyou.model.network.response.FeedResponse;
import stg.onyou.repository.UserRepository;
import stg.onyou.service.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Api(tags = {"Feed API Controller"})
@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final ClubService clubService;
    private final AwsS3Service awsS3Service;
    private final UserService userService;
    //    private final CommentService commentService;
    private final LikesService likesService;
    private final ReportService reportService;
    private final HashtagService hashtagService;

    @GetMapping("/api/feeds")
    public FeedPageResponse selectFeedList(
            @RequestParam(required = false) String cursor,
            @PageableDefault(sort="created", size = 7) Pageable pageable,
            HttpServletRequest httpServletRequest) {

        Long userId = userService.getUserId(httpServletRequest);
        return feedService.selectFeedList(pageable, cursor, userId);

    }

    @GetMapping("/api/clubs/{clubId}/feeds")
    public FeedPageResponse selectFeedListByClub(
            @PathVariable Long clubId,
            @RequestParam(required = false) String cursor,
            @PageableDefault(sort="created", size = 7) Pageable pageable,
            HttpServletRequest httpServletRequest) {

        Long userId = userService.getUserId(httpServletRequest);
        return feedService.selectFeedListByClub(pageable, cursor, userId, clubId);

    }

    @PostMapping("/api/feeds")
    public Header<Object> createFeed(@RequestPart(value = "file") List<MultipartFile> multipartFiles,
                                     @RequestPart(value = "feedCreateRequest") FeedCreateRequest request,
                                     HttpServletRequest httpServletRequest
    ) {

        Long userId = userService.getUserId(httpServletRequest);
        List<FeedImage> feedImages = new ArrayList<>();
        Feed feed = feedService.createFeed(request, userId, feedImages);
        for (MultipartFile multipartFile : multipartFiles) {
            String url = awsS3Service.uploadFile(multipartFile);
            feedService.addFeedImage(feed, url);
        }
        List<FeedHashtag> feedHashtagList = hashtagService.addHashtagToFeed(feed);
        feed.setFeedHashtags(feedHashtagList);
        feedService.upload(feed);

        return Header.OK();
    }

    @GetMapping("/api/feeds/{id}")
    public Header<FeedResponse> selectFeed(@PathVariable Long id,
                                           HttpServletRequest httpServletRequest) {
        Feed feed = feedService.findById(id);
        Long userId = userService.getUserId(httpServletRequest);
        String userName = feed.getUser().getName();
        Long feedId = feed.getId();
        Long clubId = feed.getClub().getId();
        String clubName = feed.getClub().getName();
        String content = feed.getContent();
        List<String> hashtags = feedService.getHashtags(feed);
        List<String> imageUrls = feed.getFeedImages().stream().map(FeedImage::getUrl).collect(Collectors.toList());
        boolean likeYn = likesService.isLikes(userId, feed.getId());
        int likesCount = feed.getLikes().size();
        int commentCount = feed.getComments().size();
        FeedResponse feedResponse = FeedResponse.builder()
                .userId(feed.getUser().getId())
                .id(feedId)
                .clubId(clubId)
                .clubName(clubName)
                .userName(userName)
                .content(content)
//                .imageUrls(imageUrls)
//                .likeYn(likeYn)
//                .likesCount(likesCount)
                .commentCount(commentCount)
//                .hashtags(hashtags)
                .created(feed.getCreated())
                .updated(feed.getUpdated())
                .build();

        return Header.OK(feedResponse);
    }

    @PutMapping("/api/feeds/{id}")
    public Header<Object> updateFeed(@PathVariable Long id,
                                     @RequestBody FeedUpdateRequest request) {
        if (request.getAccess() == null || request.getContent() == null) {
            throw new CustomException(ErrorCode.FEED_UPDATE_ERROR);
        } else {
            feedService.updateFeed(id, request);
        }
        return Header.OK();
    }

    @DeleteMapping("/api/feeds/{id}")
    public Header<Object> deleteFeedBy(@PathVariable Long id, HttpServletRequest httpServletRequest) {
//        Long userId = userService.getUserId(httpServletRequest);
        feedService.deleteById(id);
//        Feed feed = feedService.findById(id);
//        awsS3Service.deleteFile(feed.getFeedImages());
        return Header.OK("Feed 삭제 완료");
    }

    /**
     * FEED 신고
     */
    @PutMapping("/api/feeds/{id}/report")
    public Header<String> reportFeed(@PathVariable Long id,
                                     @RequestParam("reason") Reason reason, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        String result = reportService.feedReport(userId, id, reason);
        return Header.OK(result);
    }


    /**
     * FEED 에 댓글 추가
     */
    @PostMapping("/api/feeds/{id}/comment")
    public Header<Object> commentFeed(@PathVariable Long id,
                                      @RequestBody Map<String, String> comment,
                                      HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        String content = comment.get("content");
        feedService.commentFeed(userId, id, content);
        return Header.OK();
    }

    @GetMapping("/api/feeds/{id}/comments")
    public Header<List<CommentResponse>> getCommentList(@PathVariable Long id) {
        return Header.OK(feedService.getComments(id));
    }

    @PostMapping("/api/feeds/{id}/likes")
    public Header<Object> likeFeed(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        likesService.addLikes(userId, id);
        return Header.OK();
    }

    @GetMapping("/api/feeds/{id}/likes")
    public Header<Integer> getFeedLikesCount(@PathVariable Long id) {
        return Header.OK(feedService.getLikesCount(id));
    }

}