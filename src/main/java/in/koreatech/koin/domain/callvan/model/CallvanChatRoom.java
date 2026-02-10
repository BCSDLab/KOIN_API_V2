package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "callvan_chat_room")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callvan_post_id", nullable = false, unique = true)
    private CallvanPost post;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private CallvanChatRoom(
            CallvanPost post,
            String roomName,
            Boolean isDeleted) {
        this.post = post;
        this.roomName = roomName;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public void determineCallvanPost(CallvanPost callvanPost) {
        this.post = callvanPost;
        post.updateChatRoom(this);
    }
}
