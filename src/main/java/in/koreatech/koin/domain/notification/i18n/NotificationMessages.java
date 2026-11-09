package in.koreatech.koin.domain.notification.i18n;

import java.util.List;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.user.model.User;

/** Static, server-owned notification copy. Never pass arbitrary user messages to translateKnown. */
public final class NotificationMessages {
    private NotificationMessages() {}

    public static String format(User user, String koreanTemplate, Object... arguments) {
        String language = user == null ? null : user.getPreferredLanguage();
        Entry entry = ENTRIES.stream().filter(value -> value.templates().get(0).equals(koreanTemplate))
            .findFirst().orElse(null);
        String template = entry == null ? koreanTemplate : entry.templates().get(index(language));
        return template.formatted(arguments);
    }

    /** Exact fixed copy only. Dynamic values must be passed separately to format. */
    public static String translateKnown(User user, String text) {
        if (text == null) {
            return null;
        }
        return ENTRIES.stream()
            .filter(entry -> !entry.templates().get(0).contains("%s") && entry.templates().get(0).equals(text))
            .findFirst()
            .map(entry -> entry.templates().get(index(user == null ? null : user.getPreferredLanguage())))
            .orElse(text);
    }

    /** Legacy snapshots have no template metadata. Translate only an exact, typed reconstruction. */
    public static String teamMessage(User user,
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType type,
        String recruitmentTitle, String original) {
        if (type == null || original == null || recruitmentTitle == null) {
            return original;
        }
        List<String> templates = switch (type) {
            case NEW_APPLICATION -> List.of("%s에 새로운 지원자가 있어요.");
            case APPLICATION_ACCEPTED -> List.of("%s 지원이 승인되었어요.");
            case APPLICATION_REJECTED -> List.of("%s 지원이 거절되었어요.",
                "지원하신 %s 모집이 마감되어 지원이 거절되었어요.",
                "지원하신 %s 모집이 삭제되어 지원이 취소되었어요.");
            case RECRUITMENT_CLOSED -> List.of("%s 모집이 마감되었어요.");
            case RECRUITMENT_DELETED -> List.of("%s 모집글이 삭제되었어요.");
            case NEW_CHAT_MESSAGE -> List.of();
        };
        return templates.stream()
            .filter(template -> template.formatted(recruitmentTitle).equals(original))
            .findFirst()
            .map(template -> format(user, template, recruitmentTitle))
            .orElse(original);
    }

    private static int index(String language) {
        return switch (SupportedLanguage.fromStoredCode(language)) {
            case KOREAN -> 0;
            case ENGLISH -> 1;
            case SIMPLIFIED_CHINESE -> 2;
            case MONGOLIAN -> 3;
            case UZBEK -> 4;
            case BURMESE -> 5;
            case JAPANESE -> 6;
        };
    }

    private record Entry(List<String> templates) {
        Entry(String... values) {
            this(List.of(values));
        }
    }

    private static final List<Entry> ENTRIES = List.of(
        new Entry(",맛있게 드셨나요?", ", did you enjoy your meal?", "，用餐满意吗？", ", хоол тань таалагдсан уу?", ", taomingiz yoqdimi?", "၊ အစားအစာကို နှစ်သက်ပါသလား။", "のお食事はいかがでしたか？"),
        new Entry("가게에 리뷰를 남겨보세요!", "Leave a review for the shop!", "给店铺留下评价吧！", "Дэлгүүрт сэтгэгдэл үлдээгээрэй!", "Do‘kon haqida sharh qoldiring!", "ဆိုင်အတွက် သုံးသပ်ချက်ရေးပေးပါ။", "お店のレビューを投稿してみましょう！"),
        new Entry(",편안히 이용하셨나요?", ", was your ride comfortable?", "，乘坐舒适吗？", ", тав тухтай зорчсон уу?", ", safaringiz qulay bo‘ldimi?", "၊ ခရီးစဉ် အဆင်ပြေပါသလား။", "のご利用はいかがでしたか？"),
        new Entry("리뷰를 남겨보세요!", "Leave a review!", "留下评价吧！", "Сэтгэгдэл үлдээгээрэй!", "Sharh qoldiring!", "သုံးသပ်ချက်ရေးပေးပါ။", "レビューを投稿してみましょう！"),
        new Entry("팀원 모집", "Team recruitment", "团队招募", "Багийн элсэлт", "Jamoaga qabul", "အဖွဲ့ဝင်ခေါ်ယူခြင်း", "チームメンバー募集"),
        new Entry("%s님의 메시지", "Message from %s", "来自%s的消息", "%s-с ирсэн зурвас", "%sdan xabar", "%s ထံမှ မက်ဆေ့ချ်", "%sさんからのメッセージ"),
        new Entry("주문이 접수되었어요", "Your order has been accepted", "订单已接单", "Таны захиалгыг хүлээн авлаа", "Buyurtmangiz qabul qilindi", "သင့်အော်ဒါကို လက်ခံပြီးပါပြီ", "注文が受け付けられました"),
        new Entry("배달이 시작됐어요", "Your delivery is on its way", "订单正在配送", "Таны хүргэлт гарлаа", "Yetkazib berish boshlandi", "သင့်အော်ဒါကို ပို့ဆောင်နေပါပြီ", "配達が始まりました"),
        new Entry("포장이 완료됐어요", "Your order is ready for pickup", "订单已打包完成", "Таны захиалга авахад бэлэн боллоо", "Buyurtmangiz olib ketishga tayyor", "သင့်အော်ဒါကို လာယူနိုင်ပါပြီ", "持ち帰りの準備ができました"),
        new Entry("주문 수령이 완료됐어요", "Your order has been picked up", "订单已取餐", "Таны захиалгыг хүлээн авлаа", "Buyurtma olib ketildi", "အော်ဒါကို လာယူပြီးပါပြီ", "注文の受け取りが完了しました"),
        new Entry("배달이 완료됐어요", "Your order has been delivered", "订单已送达", "Таны захиалгыг хүргэлээ", "Buyurtma yetkazildi", "အော်ဒါကို ပို့ဆောင်ပြီးပါပြီ", "配達が完了しました"),
        new Entry("주문이 취소됐어요", "Your order has been canceled", "订单已取消", "Таны захиалга цуцлагдлаа", "Buyurtma bekor qilindi", "အော်ဒါကို ပယ်ဖျက်ပြီးပါပြီ", "注文がキャンセルされました"),
        new Entry("%s에서 조리를 시작했어요.", "%s has started preparing your order.", "%s已开始制作您的餐品。", "%s таны захиалгыг бэлтгэж эхэллээ.", "%s buyurtmangizni tayyorlashni boshladi.", "%s သည် သင့်အော်ဒါကို စတင်ပြင်ဆင်နေပါပြီ။", "%sで調理が始まりました。"),
        new Entry("%s에서 주문하신 음식이 배달 중이에요.", "Your order from %s is on its way.", "您在%s的订单正在配送。", "%s-с захиалсан хоолыг хүргэж байна.", "%sdan buyurtmangiz yo‘lda.", "%s မှ သင့်အော်ဒါကို ပို့ဆောင်နေပါပြီ။", "%sで注文した料理を配達中です。"),
        new Entry("%s에서 주문을 수령해 주세요.", "Please pick up your order at %s.", "请到%s取餐。", "Захиалгаа %s-с авна уу.", "Buyurtmangizni %sdan olib keting.", "သင့်အော်ဒါကို %s တွင် လာယူပါ။", "%sで注文をお受け取りください。"),
        new Entry("%s 주문, 맛있게 드세요!", "Enjoy your order from %s!", "请享用%s的餐品！", "%s-с захиалсан хоолоо сайхан зооглоорой!", "%sdan buyurtmangiz yoqimli bo‘lsin!", "%s မှ အစားအစာကို အရသာရှိရှိ သုံးဆောင်ပါ။", "%sの料理をお楽しみください！"),
        new Entry("%s 주문의 자세한 내용은 주문 내역을 확인해 주세요.", "Check your order history for details of your %s order.", "请在订单记录中查看%s订单的详情。", "%s захиалгын дэлгэрэнгүйг захиалгын түүхээс харна уу.", "%s buyurtmasi tafsilotlarini buyurtmalar tarixidan tekshiring.", "%s အော်ဒါအသေးစိတ်ကို အော်ဒါမှတ်တမ်းတွင် ကြည့်ပါ။", "%sの注文の詳細は注文履歴をご確認ください。"),
        new Entry("예상 도착", "Estimated arrival", "预计送达", "Хүрэх тооцоолсон цаг", "Taxminiy yetib kelish", "ခန့်မှန်းရောက်ရှိချိန်", "到着予定"),
        new Entry("포장 완료 예정", "Estimated pickup time", "预计可取餐时间", "Авахад бэлэн болох цаг", "Taxminiy tayyor bo‘lish vaqti", "ခန့်မှန်းလာယူနိုင်ချိန်", "持ち帰り準備完了予定"),
        new Entry("[코인동아리] %s 모집 공고가 갱신되었어요!", "[KOIN Clubs] %s recruitment has been updated!", "[KOIN社团] %s的招募公告已更新！", "[KOIN клуб] %s элсэлтийн зар шинэчлэгдлээ!", "[KOIN klublari] %s qabul e’loni yangilandi!", "[KOIN Clubs] %s ခေါ်ယူရေးကြေညာချက်ကို ပြင်ဆင်ပြီးပါပြီ။", "[KOINサークル] %sの募集情報が更新されました！"),
        new Entry("%s에 모집 정보가 새로 업데이트되었어요.\n행사 내용 둘러보기", "%s has new recruitment information.\nView event details", "%s的招募信息已更新。\n查看活动详情", "%s элсэлтийн мэдээллээ шинэчиллээ.\nҮйл ажиллагааны мэдээллийг үзэх", "%s qabul ma’lumoti yangilandi.\nTadbir tafsilotlarini ko‘rish", "%s ခေါ်ယူရေးအချက်အလက်ကို ပြင်ဆင်ပြီးပါပြီ။\nပွဲအသေးစိတ်ကြည့်ရန်", "%sの募集情報が更新されました。\nイベント詳細を見る"),
        new Entry("[코인 동아리] %s에 내일 예정된 행사가 있어요!", "[KOIN Clubs] %s has an event tomorrow!", "[KOIN社团] %s明天有活动！", "[KOIN клуб] %s маргааш арга хэмжээтэй!", "[KOIN klublari] %s ertaga tadbir o‘tkazadi!", "[KOIN Clubs] %s တွင် မနက်ဖြန်ပွဲရှိပါသည်။", "[KOINサークル] %sで明日イベントがあります！"),
        new Entry("%s - %s의 행사가 %s에 진행 될 예정이에요.\n행사 내용 둘러보기", "%s - The event by %s is scheduled for %s.\nView event details", "%s - %s的活动将于%s举行。\n查看活动详情", "%s - %s-ийн арга хэмжээ %s-д болно.\nДэлгэрэнгүй үзэх", "%s - %s tadbiri %sda o‘tkaziladi.\nTafsilotlarni ko‘rish", "%s - %s ၏ပွဲကို %s တွင် ကျင်းပပါမည်။\nအသေးစိတ်ကြည့်ရန်", "%s - %sのイベントは%sに開催予定です。\n詳細を見る"),
        new Entry("[코인 동아리] 오늘 %s 행사가 한 시간 남았어요!", "[KOIN Clubs] %s has an event in one hour!", "[KOIN社团] %s的活动一小时后开始！", "[KOIN клуб] %s-ийн арга хэмжээ нэг цагийн дараа эхэлнэ!", "[KOIN klublari] %s tadbiri bir soatdan keyin boshlanadi!", "[KOIN Clubs] %s ပွဲသည် တစ်နာရီအကြာတွင် စတင်ပါမည်။", "[KOINサークル] %sのイベント開始まであと1時間です！"),
        new Entry("%s - %s의 행사가 1시간 뒤에 시작 할 예정이에요.\n행사 내용 둘러보기", "%s - The event by %s starts in one hour.\nView event details", "%s - %s的活动将在一小时后开始。\n查看活动详情", "%s - %s-ийн арга хэмжээ нэг цагийн дараа эхэлнэ.\nДэлгэрэнгүй үзэх", "%s - %s tadbiri bir soatdan keyin boshlanadi.\nTafsilotlarni ko‘rish", "%s - %s ၏ပွဲသည် တစ်နာရီအကြာတွင် စတင်ပါမည်။\nအသေးစိတ်ကြည့်ရန်", "%s - %sのイベントは1時間後に始まります。\n詳細を見る"),
        new Entry("%s의 이벤트가 추가되었어요 🎉", "%s has added an event 🎉", "%s新增了活动 🎉", "%s шинэ арга хэмжээ нэмлээ 🎉", "%s yangi tadbir qo‘shdi 🎉", "%s က ပွဲအသစ်တစ်ခု ထည့်ထားပါသည် 🎉", "%sのイベントが追加されました 🎉"),
        new Entry("%s 품절됐어요 😢", "%s is sold out 😢", "%s已售罄 😢", "%s дууссан байна 😢", "%s tugab qoldi 😢", "%s ကုန်သွားပါပြီ 😢", "%sは売り切れました 😢"),
        new Entry("다른 코너 메뉴도 확인해보세요", "Check the menus at other counters", "请查看其他窗口的菜单", "Бусад лангууны цэсийг үзээрэй", "Boshqa bo‘limlar menyusini ham ko‘ring", "အခြားကောင်တာများ၏ မီနူးကို ကြည့်ပါ", "他のコーナーのメニューもご確認ください"),
        new Entry("식단 사진이 업로드 됐어요!", "A meal photo has been uploaded!", "餐食照片已上传！", "Хоолны зураг нэмэгдлээ!", "Taom rasmi yuklandi!", "အစားအစာဓာတ်ပုံကို တင်ပြီးပါပြီ။", "食事の写真がアップロードされました！"),
        new Entry("사진 보러 가기 😋", "View the photo 😋", "查看照片 😋", "Зургийг үзэх 😋", "Rasmni ko‘rish 😋", "ဓာတ်ပုံကြည့်ရန် 😋", "写真を見る 😋"),
        new Entry("방금 등록된 %s 공지를 확인해보세요!", "Check the new notice for %s!", "请查看刚发布的%s公告！", "Шинээр нийтэлсэн %s зарыг үзээрэй!", "%s bo‘yicha yangi e’lonni tekshiring!", "အသစ်တင်ထားသော %s ကြေညာချက်ကို ကြည့်ပါ။", "新しく掲載された%sのお知らせをご確認ください！"),
        new Entry("방금 등록된 %s 분실물 게시물을 확인해보세요!", "Check the new lost-item post for %s!", "请查看刚发布的%s失物帖子！", "Шинээр нийтэлсэн %s гээсэн эд зүйлийн зарыг үзээрэй!", "%s bo‘yicha yangi yo‘qolgan buyum e’lonini tekshiring!", "အသစ်တင်ထားသော %s ပျောက်ဆုံးပစ္စည်းပို့စ်ကို ကြည့်ပါ။", "新しく掲載された%sの落とし物投稿をご確認ください！"),
        new Entry("%s에 새로운 지원자가 있어요.", "%s has a new applicant.", "%s有新的申请者。", "%s-д шинэ хүсэлт ирлээ.", "%sga yangi ariza keldi.", "%s တွင် လျှောက်ထားသူအသစ်ရှိပါသည်။", "%sに新しい応募者がいます。"),
        new Entry("%s 지원이 승인되었어요.", "Your application to %s was accepted.", "您对%s的申请已获批准。", "%s-д гаргасан хүсэлтийг зөвшөөрлөө.", "%sga arizangiz qabul qilindi.", "%s အတွက် သင့်လျှောက်လွှာကို လက်ခံပြီးပါပြီ။", "%sへの応募が承認されました。"),
        new Entry("%s 지원이 거절되었어요.", "Your application to %s was declined.", "您对%s的申请未获批准。", "%s-д гаргасан хүсэлт татгалзагдлаа.", "%sga arizangiz rad etildi.", "%s အတွက် သင့်လျှောက်လွှာကို ငြင်းပယ်လိုက်ပါသည်။", "%sへの応募が見送られました。"),
        new Entry("%s 모집이 마감되었어요.", "Recruitment for %s has closed.", "%s的招募已结束。", "%s-ийн элсэлт хаагдлаа.", "%s uchun qabul yopildi.", "%s ခေါ်ယူရေးကို ပိတ်လိုက်ပါပြီ။", "%sの募集が締め切られました。"),
        new Entry("%s 모집글이 삭제되었어요.", "The recruitment post for %s was deleted.", "%s的招募帖子已删除。", "%s-ийн элсэлтийн зар устгагдлаа.", "%s qabul e’loni o‘chirildi.", "%s ခေါ်ယူရေးပို့စ်ကို ဖျက်လိုက်ပါပြီ။", "%sの募集投稿が削除されました。"),
        new Entry("지원하신 %s 모집이 마감되어 지원이 거절되었어요.", "Your application to %s was declined because recruitment closed.", "由于%s招募已结束，您的申请未获批准。", "%s-ийн элсэлт хаагдсан тул таны хүсэлт татгалзагдлаа.", "%s uchun qabul yopilgani sabab arizangiz rad etildi.", "%s ခေါ်ယူရေးပိတ်သွားသဖြင့် သင့်လျှောက်လွှာကို ငြင်းပယ်လိုက်ပါသည်။", "%sの募集が締め切られたため、応募が見送られました。"),
        new Entry("지원하신 %s 모집이 삭제되어 지원이 취소되었어요.", "Your application to %s was canceled because the recruitment post was deleted.", "由于%s招募帖子已删除，您的申请已取消。", "%s-ийн зар устгагдсан тул таны хүсэлт цуцлагдлаа.", "%s e’loni o‘chirilgani sabab arizangiz bekor qilindi.", "%s ခေါ်ယူရေးပို့စ်ဖျက်လိုက်သဖြင့် သင့်လျှောက်လွှာကို ပယ်ဖျက်လိုက်ပါသည်။", "%sの募集投稿が削除されたため、応募がキャンセルされました。"),
        new Entry("%s 새 메시지가 도착했어요.", "%s has a new message.", "%s有新消息。", "%s-д шинэ зурвас ирлээ.", "%sga yangi xabar keldi.", "%s တွင် မက်ဆေ့ချ်အသစ်ရှိပါသည်။", "%sに新しいメッセージが届きました。"),
        new Entry("콜벤팟 %s님의 메시지", "Callvan: message from %s", "拼车：来自%s的消息", "Хамтын унаа: %s-с зурвас", "Callvan: %sdan xabar", "Callvan: %s ထံမှ မက်ဆေ့ချ်", "相乗り：%sさんからのメッセージ"),
        new Entry("콜벤팟 새 참여자", "New Callvan participant", "拼车新成员", "Хамтын унаанд шинэ оролцогч", "Callvanga yangi ishtirokchi", "Callvan ပါဝင်သူအသစ်", "相乗りの新しい参加者"),
        new Entry("콜벤팟 인원 모집 완료", "Callvan group is full", "拼车人数已满", "Хамтын унааны бүртгэл дүүрлээ", "Callvan guruhi to‘ldi", "Callvan အဖွဲ့လူပြည့်ပါပြီ", "相乗りの募集人数が集まりました"),
        new Entry("콜벤팟 출발 30분 전", "Callvan departs in 30 minutes", "拼车30分钟后出发", "Хамтын унаа 30 минутын дараа хөдөлнө", "Callvan 30 daqiqadan keyin jo‘naydi", "Callvan သည် မိနစ် ၃၀ အကြာတွင် ထွက်ခွာပါမည်", "相乗りの出発まで30分です"),
        new Entry("콜벤팟 이용 안내", "Callvan service notice", "拼车服务通知", "Хамтын унааны үйлчилгээний мэдэгдэл", "Callvan xizmati xabarnomasi", "Callvan ဝန်ဆောင်မှုအသိပေးချက်", "相乗りサービスのお知らせ"),
        new Entry("%s님이 콜벤팟에 참여했어요", "%s joined the Callvan group", "%s加入了拼车群组", "%s хамтын унаанд нэгдлээ", "%s Callvan guruhiga qo‘shildi", "%s သည် Callvan အဖွဲ့သို့ ဝင်လာပါသည်", "%sさんが相乗りに参加しました"),
        new Entry("%s -> %s 콜벤팟이 30분 뒤 출발해요", "Callvan from %s to %s departs in 30 minutes", "从%s到%s的拼车将在30分钟后出发", "%s-с %s хүрэх хамтын унаа 30 минутын дараа хөдөлнө", "%sdan %sga Callvan 30 daqiqadan keyin jo‘naydi", "%s မှ %s သို့ Callvan သည် မိနစ် ၃၀ အကြာတွင် ထွက်ခွာပါမည်", "%sから%sへの相乗りは30分後に出発します"),
        new Entry("해당 콜벤팟 인원이 모두 모집되었습니다. 콜벤을 예약하세요", "The Callvan group is full. Please book the ride.", "拼车人数已满，请预约车辆。", "Хамтын унааны бүртгэл дүүрлээ. Унаагаа захиална уу.", "Callvan guruhi to‘ldi. Mashinani band qiling.", "Callvan အဖွဲ့လူပြည့်ပါပြီ။ ယာဉ်ကို ကြိုတင်မှာယူပါ။", "相乗りの人数が集まりました。車両を予約してください"),
        new Entry("%s님이 사진을 보냈습니다.", "%s sent a photo.", "%s发送了一张照片。", "%s зураг илгээлээ.", "%s rasm yubordi.", "%s က ဓာတ်ပုံပို့လိုက်ပါသည်။", "%sさんが写真を送信しました。"),
        new Entry("콜벤팟 이용 과정에서 신고가 접수되어 운영 검토 후 주의 안내가 전달되었습니다. 이후 동일한 문제가 반복될 경우 콜벤 기능 이용이 제한될 수 있습니다.", "Following a report and review, you have received a Callvan warning. Repeated issues may restrict your access.", "经举报和审核，现向您发出拼车服务警告。若同类问题再次发生，可能限制您的使用权限。", "Гомдлыг хянасны дараа хамтын унааны анхааруулга өглөө. Асуудал давтагдвал ашиглах эрх хязгаарлагдаж болно.", "Shikoyat tekshirilgach, Callvan bo‘yicha ogohlantirish berildi. Muammo takrorlansa, foydalanish cheklanishi mumkin.", "တိုင်ကြားချက်ကို စိစစ်ပြီးနောက် Callvan သတိပေးချက် ထုတ်ပြန်ပါသည်။ ထပ်မံဖြစ်ပါက အသုံးပြုခွင့် ကန့်သတ်နိုင်ပါသည်။", "通報内容の確認後、相乗りサービスの利用について注意をお伝えしています。同様の問題が繰り返された場合、利用が制限されることがあります。"),
        new Entry("콜벤팟 이용 과정에서 신고가 접수되어 운영 검토 후 14일간 콜벤 기능 이용이 제한되었습니다.", "Following a report and review, your Callvan access has been restricted for 14 days.", "经举报和审核，您的拼车服务使用权限已被限制14天。", "Гомдлыг хянасны дараа хамтын унаа ашиглах эрхийг 14 хоногоор хязгаарлалаа.", "Shikoyat tekshirilgach, Callvandan foydalanishingiz 14 kunga cheklandi.", "တိုင်ကြားချက်ကို စိစစ်ပြီးနောက် Callvan အသုံးပြုခွင့်ကို ၁၄ ရက် ကန့်သတ်လိုက်ပါသည်။", "通報内容の確認後、相乗りサービスの利用を14日間制限しました。"),
        new Entry("콜벤팟 이용 과정에서 신고가 접수되어 운영 검토 후 콜벤 기능 이용이 영구적으로 제한되었습니다.", "Following a report and review, your Callvan access has been permanently restricted.", "经举报和审核，您的拼车服务使用权限已被永久限制。", "Гомдлыг хянасны дараа хамтын унаа ашиглах эрхийг бүрмөсөн хязгаарлалаа.", "Shikoyat tekshirilgach, Callvandan foydalanishingiz doimiy cheklandi.", "တိုင်ကြားချက်ကို စိစစ်ပြီးနောက် Callvan အသုံးပြုခွင့်ကို အပြီးတိုင် ကန့်သတ်လိုက်ပါသည်။", "通報内容の確認後、相乗りサービスの利用を無期限で制限しました。")
    );
}
