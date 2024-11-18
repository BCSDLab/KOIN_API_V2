package in.koreatech.koin.domain.owner.model.dto;

public record OwnerRegisterEvent(
        String ownerName,
        String account,
        Integer ownerId
) {

}
