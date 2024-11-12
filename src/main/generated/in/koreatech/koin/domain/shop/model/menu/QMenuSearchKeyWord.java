package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMenuSearchKeyWord is a Querydsl query type for MenuSearchKeyWord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuSearchKeyWord extends EntityPathBase<MenuSearchKeyWord> {

    private static final long serialVersionUID = -1528854348L;

    public static final QMenuSearchKeyWord menuSearchKeyWord = new QMenuSearchKeyWord("menuSearchKeyWord");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath keyword = createString("keyword");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMenuSearchKeyWord(String variable) {
        super(MenuSearchKeyWord.class, forVariable(variable));
    }

    public QMenuSearchKeyWord(Path<? extends MenuSearchKeyWord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMenuSearchKeyWord(PathMetadata metadata) {
        super(MenuSearchKeyWord.class, metadata);
    }

}

