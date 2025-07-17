package in.koreatech.koin.domain.order.address.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.Getter;

/**
 * 외부 주소 호출 API 500 오류 지속 발생시 서킷 브레이커 OPEN<br>
 * => MongoDB에 저장된 병천면 주소 정보에서 응답 반환.<br>
 * 해당 주소 정보 매핑 엔티티
 * */
@Getter
@Document(collection = "address_byeongcheon")
public class RoadNameAddressDocument {

    @Id
    @Field("_id")
    private String id;

    @Field("road_address")
    private String roadAddress;

    @Field("jibun_address")
    private String jibunAddress;

    @Field("eng_address")
    private String engAddress;

    @Field("zip_no")
    private String zipNo;

    @Field("bd_nm")
    private String bdNm;

    @Field("si_nm")
    private String siNm;

    @Field("sgg_nm")
    private String sggNm;

    @Field("emd_nm")
    private String emdNm;

    @Field("li_nm")
    private String liNm;

    @Field("rn")
    private String rn;
}
