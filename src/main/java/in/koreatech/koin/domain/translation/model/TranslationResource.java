package in.koreatech.koin.domain.translation.model;

public record TranslationResource(TranslationType type, String resourceId, int parentId, int roomId) {
    public static TranslationResource article(TranslationType type, Integer id) {
        return new TranslationResource(type, id.toString(), 0, 0);
    }

    public static TranslationResource message(TranslationType type, Integer parentId, Integer roomId, String id) {
        return new TranslationResource(type, id, parentId, roomId);
    }
}
