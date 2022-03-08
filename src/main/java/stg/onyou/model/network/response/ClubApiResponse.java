package stg.onyou.model.network.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stg.onyou.model.entity.User;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubApiResponse {

    private Integer id;
    private String name;
    private String information;
    private String announcement;
    private String organizationName;
    private List<UserApiResponse> members;
    private int maxNumber;
    private int recruitNumber;
    private String thumbnail;
    private String recruitStatus; //BEGIN, RECRUITING, CLOSED
    private String applyStatus; //AVAILABLE, APPLIED, APPROVED
    private String creatorName;
    private String categoryName;
}

