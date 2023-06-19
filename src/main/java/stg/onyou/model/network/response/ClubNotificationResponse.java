package stg.onyou.model.network.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stg.onyou.model.enums.ActionType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubNotificationResponse {

    private Long actionId;
    private Long actionerId;
    private String actionerName;
    private Long actioneeId;
    private String actioneeName;
    private ActionType actionType;
    private String message;
    private boolean isRead;
    private boolean isDone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime created;

    @QueryProjection
    public ClubNotificationResponse(Long actionId, Long actionerId, Long actioneeId, ActionType actionType, String message, boolean isRead, boolean isDone, LocalDateTime created){
        this.actionId = actionId;
        this.actionerId = actionerId;
        this.actioneeId = actioneeId;
        this.actionType = actionType;
        this.message = message;
        this.isRead = isRead;
        this.isDone = isDone;
        this.created = created;
    }
}
