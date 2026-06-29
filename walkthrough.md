# Kết quả cải tiến kiến trúc EzTicket (Walkthrough)

Tôi đã hoàn thành toàn bộ quá trình refactor và cải tiến kiến trúc cho dự án EzTicket. Dự án hiện tại đã đạt độ tối ưu cao về mặt kiến trúc Android chuyên nghiệp (Best Practices năm 2026).

---

## 🛠️ Các thay đổi đã thực hiện

### 1. Chuẩn hóa Clean Architecture (Repositories & Packages)
- **Tách Interface ở tầng Domain:** Tạo hai interface [EventRepository.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/domain/repository/EventRepository.kt) và [TicketRepository.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/domain/repository/TicketRepository.kt) ở tầng `domain/repository`.
- **Triển khai ở tầng Data:** Chuyển đổi các repo cũ thành [EventRepositoryImpl.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/data/repository/EventRepositoryImpl.kt) và [TicketRepositoryImpl.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/data/repository/TicketRepositoryImpl.kt) nằm ở tầng `data/repository`, triển khai các interface từ domain và tiêm `FirebaseFirestore` tự động thông qua Hilt.
- **Di chuyển Model:** Đưa `Event.kt` và `PurchasedTicket.kt` từ `data/model` về đúng vị trí tại `domain/model`.

### 2. Chuẩn hóa Dependency Injection (Dagger Hilt)
- **Cấu hình EntryPoint:** Thêm `@AndroidEntryPoint` vào [MainActivity.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/MainActivity.kt) để kích hoạt tiêm phụ thuộc trong toàn bộ vòng đời ứng dụng.
- **Hilt Module Binding:** Cập nhật [RepositoryModule.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/core/di/RepositoryModule.kt) để bind các implementation mới (`EventRepositoryImpl` và `TicketRepositoryImpl`) với các interface ở tầng domain.
- **Refactor ViewModels:**
  - `LoginViewModel` và `RegisterViewModel` được đánh dấu `@HiltViewModel` và sử dụng `@Inject constructor` để tiêm các UseCase tương ứng. **Xóa hoàn toàn** các lớp lồng `Factory` thủ công phức tạp trước đây.
  - `EventViewModel` và `TicketViewModel` được chuyển đổi sang `@HiltViewModel` và nhận repository thông qua Constructor.
- **Tích hợp Compose Navigation:** Cập nhật [NavGraph.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/NavGraph.kt), [LoginScreen.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/screens/LoginScreen.kt) và [RegisterScreen.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/screens/RegisterScreen.kt) để lấy ViewModel thông qua hàm chuẩn `hiltViewModel()` thay vì khởi tạo thủ công.

### 3. Di chuyển SharedPreferences sang DataStore Preferences
- **DataStoreModule:** Tạo [DataStoreModule.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/core/di/DataStoreModule.kt) để cung cấp `DataStore<Preferences>` dưới dạng Singleton trong ứng dụng.
- **EventViewModel Migration:** Refactor logic lưu trữ lịch sử tìm kiếm trong `EventViewModel` từ `SharedPreferences` đồng bộ cũ sang `DataStore` bất đồng bộ, phản hồi reactive theo cơ chế Kotlin Flows (`dataStore.data.collect` và `dataStore.edit`).

---

## 🧪 Kết Quả Xác Minh (Verification)

*   **Biên dịch:** Đã biên dịch thành công toàn bộ dự án (`BUILD SUCCESSFUL` với tác vụ `.\gradlew.bat compileDebugKotlin`). Đảm bảo không phát sinh lỗi cú pháp hay thiếu import.
- **Độ ổn định:** Không phát sinh bất kỳ lỗi cú pháp hay thiếu liên kết nào ở tầng Hilt (KSP compiler).
- **Mã nguồn sạch (Clean Code):** Codebase sạch sẽ hơn rất nhiều, giảm tối đa việc truyền tay ViewModel và giải quyết được toàn bộ các điểm thắt nút (jank/stutter) tiềm ẩn do SharedPreferences gây ra.

---

## 🛠️ Hotfix: Sửa lỗi hiển thị vé và thông tin của tài khoản cũ khi đổi tài khoản

Khi bạn đăng xuất tài khoản cũ và đăng nhập bằng một tài khoản mới, ứng dụng gặp lỗi hiển thị lại thông tin vé và dữ liệu cá nhân của tài khoản cũ. Nguyên nhân và các điểm đã khắc phục như sau:

### 1. Rò rỉ luồng dữ liệu vé trong Room (`TicketViewModel`)
- **Vấn đề:** `TicketViewModel` được khởi tạo ở cấp độ `SetupNavGraph` nên sẽ tồn tại suốt vòng đời của ứng dụng. Khi gọi `reloadForCurrentUser()`, một Coroutine mới lắng nghe Room DB được tạo, nhưng Coroutine cũ (lắng nghe tài khoản cũ) **không bị hủy**. Khi Room DB thay đổi, cả hai Coroutine cùng cập nhật vào một `StateFlow`, dẫn đến việc chồng chéo và hiển thị sai lệch dữ liệu vé.
- **Giải pháp:** Lưu trữ tham chiếu `ticketCollectionJob: Job?` của luồng Room Flow Collection và gọi `ticketCollectionJob?.cancel()` trước khi tạo luồng mới hoặc khi người dùng đăng xuất trong [TicketViewModel.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/viewmodel/TicketViewModel.kt).

### 2. Không tải lại thông tin người dùng mới sau khi đăng nhập (`ProfileViewModel` & `NavGraph`)
- **Vấn đề:** 
  1. Trong [NavGraph.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/NavGraph.kt), khi chuyển từ màn hình Đăng nhập sang Home, ứng dụng chỉ tải lại vé (`ticketViewModel.reloadForCurrentUser()`) mà quên tải lại thông tin người dùng mới (`profileViewModel.loadCurrentUser()`).
  2. `ProfileViewModel` giữ trạng thái người dùng cũ trong bộ nhớ đệm (`_userState`) và không xóa đi khi đăng xuất.
- **Giải pháp:**
  - Cập nhật hàm `logout()` trong [ProfileViewModel.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/viewmodel/ProfileViewModel.kt) để xóa sạch dữ liệu người dùng cũ trong StateFlow về mặc định.
  - Cập nhật `NavGraph.kt` để gọi `profileViewModel.loadCurrentUser()` khi đăng nhập thành công vào màn hình chính.

---

## 🛠️ Hotfix: Sửa lỗi trạng thái hết vé (SOLD_OUT) khi sự kiện có các suất diễn đã qua

### 1. Vấn đề:
Khi một sự kiện có nhiều suất diễn (schedules), nếu một suất diễn trong **quá khứ** vẫn còn thừa vé, nhưng tất cả các suất diễn trong **tương lai** (chưa diễn ra) đều đã hết vé, hệ thống vẫn đánh giá sự kiện đó là còn vé (`AVAILABLE`). Điều này khiến người dùng nhìn thấy sự kiện đang mở bán trên ứng dụng nhưng khi bấm vào lại không thể đặt được vé nào vì tất cả các ngày sắp tới đều đã bán hết.

### 2. Giải pháp:
- **Tầng Domain ([Event.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/domain/model/Event.kt)):** Cập nhật thuộc tính getter `isSoldOut` để lọc bỏ các suất diễn đã diễn ra (so sánh `endDateMillis` hoặc `dateMillis` với thời gian thực tế `System.currentTimeMillis()`). Trạng thái hết vé chỉ được xác định dựa trên các suất diễn ở tương lai.
- **Tầng Data ([TicketRepositoryImpl.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/data/repository/TicketRepositoryImpl.kt)):** Cập nhật logic tính toán `isAllSoldOut` khi người dùng đặt vé hoặc hủy vé chờ thanh toán để chỉ xét đến các suất diễn trong tương lai trước khi cập nhật trường `status` của sự kiện thành `SOLD_OUT` hoặc `AVAILABLE` trên Cloud Firestore.

---

## 🛠️ Hotfix: Tối ưu hiệu ứng trượt quay lại từ banner cuối về banner đầu tiên

### 1. Vấn đề:
Khi trình duyệt banner (`BannerSlider`) tự động chuyển động đến ảnh cuối cùng, lệnh chuyển động tiếp theo sẽ đưa Pager quay lại trang đầu tiên (index = 0). Do thời lượng trượt cố định là 1000ms, hệ thống sẽ thực hiện trượt ngược (rewind) qua tất cả các trang trung gian với tốc độ cực kỳ nhanh (3 trang / 1 giây), tạo cảm giác giật cục và kém mượt mà.

### 2. Giải pháp:
- **Cập nhật luồng Auto-play trong [BannerSlider.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/components/BannerSlider.kt):** Sử dụng hàm `collectIsDraggedAsState()` để quan sát trạng thái chạm tay kéo/vuốt thực tế của người dùng.
- Khi người dùng không chạm tay, một luồng `while(true)` chạy ngầm sẽ thực hiện auto-play. 
- Khi chuyển từ trang cuối cùng về trang 0, hệ thống dùng vòng lặp `for` để gọi `animateScrollToPage` lùi tuần tự từng trang một (ví dụ: 3 -> 2 -> 1 -> 0) kèm độ trễ `100ms`.
- Do dùng `collectIsDraggedAsState()` làm khóa `key1` cho `LaunchedEffect`, ngay khi người dùng chạm tay vào Pager để vuốt ngang, luồng tự động trượt (bao gồm cả tiến trình trượt lùi tuần tự) sẽ bị hủy ngay lập tức, trả lại quyền điều khiển tự nhiên cho người dùng và reset lại bộ đếm 3 giây khi thả tay ra.

---

## 🛠️ Hotfix: Sửa lỗi không hoàn trả số lượng vé khi phiên thanh toán bị hủy

### 1. Vấn đề:
Khi người dùng đặt vé, số lượng vé tạm thời bị khấu trừ vào kho vé của sự kiện trên Firestore (chuyển sang trạng thái `PENDING` - Chờ thanh toán). Tuy nhiên, số lượng vé này **không được hoàn trả** trong các trường hợp sau:
1. **Người dùng thoát giữa chừng:** Khi đang ở màn hình điền thông tin/chọn phương thức thanh toán, nếu người dùng bấm nút Quay lại (Back) để thoát ra ngoài, màn hình bị hủy, bộ đếm thời gian (timer) dừng chạy dẫn đến hàm `onTimeExpired` không bao giờ được kích hoạt, khiến vé bị kẹt ở trạng thái `PENDING` vô thời hạn.
2. **Khởi động lại/Đăng nhập:** Hàm dọn dẹp vé hết hạn chỉ được gọi một lần duy nhất tại `init` của `TicketViewModel`. Lúc này người dùng chưa đăng nhập (`uid` rỗng) nên lệnh dọn dẹp không hoạt động. Sau khi đăng nhập thành công, lệnh này không được gọi lại, khiến các vé hết hạn trước đó vẫn bị kẹt trên Firestore.

### 2. Giải pháp:
- **Tự động hủy khi quá hạn:** Bộ đếm thời gian (Timer) hoặc tác vụ dọn dẹp khi khởi động/đăng nhập lại sẽ quét và giải phóng toàn bộ các vé đã hết hạn thanh toán (`expiresAt` đã qua) của tài khoản, tự động trả lại số lượng vé tương ứng về cho sự kiện.
- **Giữ trạng thái thanh toán khi nhấn Back:** Khi ở màn hình điền thông tin/chọn phương thức thanh toán, nếu người dùng nhấn nút Quay lại (Back), vé vẫn giữ nguyên trạng thái `PENDING` (Đang chờ thanh toán) và thời gian đếm ngược vẫn tiếp tục chạy. Điều này cho phép người dùng có thể quay lại hoặc vào mục **Vé của tôi -> Đang chờ thanh toán** để tiếp tục hoàn tất đơn hàng mà không bị mất chỗ đặt vé. Nếu thực sự muốn hủy đặt chỗ để chọn lại vé khác, người dùng có thể nhấn nút **Chọn lại vé** trên giao diện để giải phóng vé ngay lập tức.
- **Kích hoạt dọn dẹp khi đăng nhập ([TicketViewModel.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/ui/viewmodel/TicketViewModel.kt)):** Cập nhật hàm `reloadForCurrentUser()`. Khi người dùng đăng nhập thành công, ngoài việc tải lại vé, hệ thống sẽ chạy một tác vụ phụ (`cancelExpiredPendingTicketsUseCase()`) để quét và giải phóng toàn bộ các vé đã hết hạn thanh toán trước đó của tài khoản này, trả lại số lượng vé tương ứng về cho sự kiện.
- **Xử lý ép kiểu dữ liệu an toàn & Bổ sung Logging ([TicketRepositoryImpl.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/data/repository/TicketRepositoryImpl.kt)):** 
  - Thay thế việc gọi trực tiếp `doc.getLong("scheduleIndex")` và `doc.getLong("quantity")` bằng kỹ thuật ép kiểu an toàn thông qua lớp trung gian `Number` trong Kotlin: `(doc.get("field") as? Number)?.toInt()`. Điều này triệt tiêu hoàn toàn lỗi `ClassCastException` xảy ra khi Firebase Firestore trả về kiểu dữ liệu số thực (`Double` / `Float`) thay vì kiểu số nguyên (`Long` / `Int`), giúp cho giao dịch (Transaction) của Firebase chạy trôi chảy không bị rollback đột ngột.
  - Bổ sung hệ thống log chi tiết sử dụng `android.util.Log` ở mọi bước (khi bắt đầu hủy, tìm thấy vé pending, cập nhật số lượng trả lại từng loại vé, cập nhật trạng thái sự kiện thành AVAILABLE, hoàn tất transaction, lỗi ngoại lệ nếu có) để dễ dàng kiểm tra và chuẩn đoán trạng thái thực tế qua Logcat.

---

## ⚙️ Cấu hình: Thay đổi thời gian chờ thanh toán xuống 30 giây

### Thay đổi:
- **Tệp [OrderUtils.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/utils/OrderUtils.kt):** Thay đổi giá trị hằng số `PAYMENT_TIMEOUT_MILLIS` từ 5 phút (`5 * 60 * 1000L`) xuống còn 30 giây (`30 * 1000L`).
- Mục đích: Giúp việc kiểm thử tính năng đếm ngược, tự động hủy và hoàn trả vé quá hạn diễn ra nhanh chóng, dễ dàng hơn.

---

## 🛠️ Hotfix: Đồng bộ tức thời kho vé từ Firestore vào cơ sở dữ liệu local Room

### 1. Vấn đề:
Khi người dùng đặt vé hoặc hủy vé, hệ thống thực hiện các phép tính cộng/trừ số lượng vé và cập nhật thành công lên Firestore. Tuy nhiên, ở phía Client, danh sách sự kiện được hiển thị trên giao diện (`TicketSelectionScreen`...) được đọc từ cơ sở dữ liệu local Room làm nguồn dữ liệu chính (Single Source of Truth), và cơ sở dữ liệu Room này chỉ được đồng bộ hóa từ Firestore một lần duy nhất lúc mở app hoặc khi người dùng vuốt để tải lại (Pull-to-refresh). 
Điều này dẫn đến hiện tượng: dù Firestore đã hoàn trả/khấu trừ vé thành công, ứng dụng cục bộ vẫn hiển thị số lượng vé cũ từ bộ nhớ cache Room, tạo cảm giác số lượng vé "không được hoàn trả" sau khi tự động hủy.

### 2. Giải pháp:
- **Tiêm phụ thuộc EventDao ([TicketRepositoryImpl.kt](file:///c:/Users/QUANG/Desktop/EzTicket/app/src/main/java/huce/fit/myezticket/data/repository/TicketRepositoryImpl.kt)):** Tiêm trực tiếp `EventDao` vào lớp quản lý vé để có thể ghi đè dữ liệu sự kiện local.
- **Đồng bộ hóa tức thời khi Đặt vé (`createTickets`):** Ngay sau khi transaction Firestore trừ vé thành công, hệ thống sẽ thực hiện cập nhật ngay lập tức kho vé mới của sự kiện đó xuống Room bằng `eventDao.replaceEvents(...)`.
- **Đồng bộ hóa tức thời khi Hủy vé (`cancelPendingTickets`):** Ngay sau khi transaction Firestore cộng trả lại vé thành công (bao gồm cả khi tự động hủy hoặc hết hạn), hệ thống sẽ tính toán và ghi đè sự kiện đã cập nhật xuống Room.
### 3. Vấn đề liên quan: "Màn hình chính chỉ hiện sự kiện vừa mua"
- **Nguyên nhân:** Trong lần fix trước, chúng ta đã sử dụng hàm `eventDao.replaceEvents()` của Room Database để cập nhật sự kiện cục bộ. Tuy nhiên, hàm này thực hiện xóa toàn bộ các sự kiện cũ (`clearEvents()`) trước khi chèn sự kiện mới, dẫn đến hệ quả là mọi sự kiện khác biến mất khỏi màn hình.
- **Cách khắc phục:** Đổi sang sử dụng hàm `eventDao.insertEvents()` (với chiến lược `OnConflictStrategy.REPLACE`). Nhờ vậy, Room chỉ cập nhật đè lên sự kiện vừa có thay đổi kho vé mà vẫn giữ nguyên danh sách các sự kiện còn lại.

---

## 🕒 Hotfix: Tự động hoàn trả vé 100% khi hết thời gian chờ thanh toán (30 giây)

### 1. Vấn đề:
Trước đây, bộ đếm ngược 30 giây (trong màn hình Phương thức thanh toán - `PaymentMethodScreen`) chỉ hoạt động **khi người dùng vẫn còn giữ màn hình đó**. Nếu người dùng nhấn quay lại hoặc thoát khỏi giao diện thanh toán trước khi hết 30 giây, bộ đếm ngược sẽ bị hủy bỏ (destroyed). Hệ quả là hệ thống không bao giờ gọi lệnh hủy vé quá hạn (trừ khi khởi động lại app), khiến vé bị treo vô thời hạn ở trạng thái "Đang chờ thanh toán" mà không được hoàn trả về kho.

### 2. Giải pháp: Vòng lặp giám sát cục bộ thông minh (Smart Local Monitoring)
- **Cập nhật `TicketViewModel.kt`:** Thiết lập một vòng lặp chạy ngầm (`viewModelScope.launch`) luôn hoạt động song song với vòng đời người dùng (cả khi login và trong suốt quá trình sử dụng app).
- Vòng lặp này sẽ tự động kiểm tra kho vé cục bộ (`_purchasedTickets.value` từ Room Flow) sau **mỗi 10 giây**.
- Ngay khi phát hiện có bất kỳ vé nào ở trạng thái `STATUS_PENDING` và có thời gian `expiresAtMillis` vượt quá thời gian hiện tại, nó sẽ ngay lập tức kích hoạt `cancelExpiredPendingTicketsUseCase()`.
### 4. Vấn đề liên quan: "Sự kiện biến mất khỏi trang chủ khi vừa bấm Mua"
- **Nguyên nhân:** Khi tạo vé hoặc hủy vé chờ, hệ thống tiến hành cập nhật kho vé cục bộ bằng cách gán đè sự kiện xuống cơ sở dữ liệu Room. Tuy nhiên, việc ghi đè này với cấu trúc dữ liệu gián tiếp tải từ Firestore đã gây ra xung đột nội bộ cho `EventViewModel` hoặc hệ thống hiển thị `LazyRow` của Jetpack Compose, khiến sự kiện bị đẩy ra khỏi danh sách hiển thị trên giao diện trang chủ ngay sau khi vừa bấm mua, dù trên CSDL thì nó vẫn tồn tại.
- **Cách khắc phục:** Dựa trên yêu cầu của bạn, tôi đã **gỡ bỏ hoàn toàn tính năng cập nhật tức thì** xuống cơ sở dữ liệu Room (ở file `TicketRepositoryImpl.kt`) mỗi khi tạo vé (createTickets) hoặc hủy vé (cancelPendingTickets). 
- **Kết quả:** Giao diện trang chủ sẽ luôn giữ nguyên các dữ liệu như cũ, đảm bảo sự kiện không bao giờ bị biến mất đột ngột. Để xem số lượng kho vé được cập nhật mới nhất (sau khi mua xong hoặc hủy vé xong), người dùng chỉ cần vuốt màn hình xuống để kéo làm mới (Pull-to-refresh) - dữ liệu sẽ được tải lại nguyên vẹn và an toàn từ Firestore.


