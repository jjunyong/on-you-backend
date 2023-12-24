package stg.onyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stg.onyou.exception.CustomException;
import stg.onyou.exception.ErrorCode;
import stg.onyou.model.entity.Board;
import stg.onyou.model.enums.ReportReason;
import stg.onyou.model.entity.Feed;
import stg.onyou.model.entity.Report;
import stg.onyou.model.entity.User;
import stg.onyou.repository.BoardRepository;
import stg.onyou.repository.FeedRepository;
import stg.onyou.repository.ReportRepository;
import stg.onyou.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public String reportFeed(Long userId, Long feedId, ReportReason reason) {

        Report findReport = reportRepository.findReportByFeedIdAndUserId(feedId, userId);
        if (findReport == null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));

            feed.setReportCount(feed.getReportCount() + 1);

            Report report = Report.builder()
                    .feed(feed)
                    .user(user)
                    .reason(reason)
                    .created(LocalDateTime.now())
                    .build();

            reportRepository.save(report);
            return "신고가 접수되었습니다.";
        } else {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }

    }

    @Transactional
    public String reportBoard(Long userId, Long feedId, ReportReason reason) {

        Report findReport = reportRepository.findReportByBoardIdAndUserId(feedId, userId);
        if (findReport == null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Board board = boardRepository.findById(feedId).orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

            board.setReportCount(board.getReportCount() + 1);

            Report report = Report.builder()
                    .board(board)
                    .user(user)
                    .reason(reason)
                    .created(LocalDateTime.now())
                    .build();

            reportRepository.save(report);
            return "신고가 접수되었습니다.";
        } else {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }

    }


}
