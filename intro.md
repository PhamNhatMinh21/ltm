# T48 — Game State Synchronization: Authoritative Server

## 1. Giới thiệu đề tài

**T48 — Game State Synchronization: Authoritative Server** tập trung xây dựng một bản **demo trò chơi nhiều người chơi (multiplayer)** theo kiến trúc **authoritative server**: máy chủ là nguồn dữ liệu đáng tin cậy và có quyền quyết định cuối cùng đối với trạng thái trò chơi.

Trong mô hình này, nhiều người chơi tham gia cùng một thế giới. Client gửi thao tác hoặc cập nhật lên server; server tiếp nhận, kiểm tra, cập nhật trạng thái hợp lệ và đồng bộ kết quả tới những client liên quan. Không client nào được tự ý quyết định trạng thái chung của trò chơi.

Dự án sử dụng:

- **Minestom (Java):** framework/thư viện để xây dựng Minecraft server và triển khai game logic phía server.
- **Minecraft Java Edition:** client có sẵn để người chơi kết nối, di chuyển và tương tác.
- **Java:** ngôn ngữ triển khai máy chủ.

**Phạm vi bài tập chỉ là demo multiplayer với authoritative server architecture.** Không phát triển một minigame riêng, hệ thống tính điểm, nhiệm vụ, checkpoint hay cơ chế thắng/thua.

## 2. Mục tiêu

1. Cho phép ít nhất **hai Minecraft client** cùng kết nối vào một Minestom server.
2. Duy trì **trạng thái chung** của người chơi và thế giới tại server.
3. Đồng bộ các thay đổi hợp lệ để những người chơi liên quan quan sát được cùng một kết quả.
4. Minh họa rằng **server có quyền chấp nhận hoặc từ chối** cập nhật/hành động từ client.
5. Thực hiện **state correction** khi client gửi trạng thái không hợp lệ, trong phạm vi Minecraft client vanilla cho phép.
6. Ghi log để chứng minh luồng xử lý và quyết định cuối cùng thuộc về server.

## 3. Kiến trúc hệ thống

```text
  Minecraft Client A                  Minecraft Client B
         |                                   |
         |   Movement / Interaction         |
         +----------------+------------------+
                          |
                          v
                +---------------------+
                |   Minestom Server   |
                | Connection / Event  |
                +----------+----------+
                           |
                           v
                +---------------------+
                |  Server Validation  |
                | Check rules & state |
                +----------+----------+
                           |
                    Accept / Reject
                           |
                           v
                +---------------------+
                | Authoritative State |
                | Players + World     |
                +----------+----------+
                           |
                           v
                +---------------------+
                |  State Sync /       |
                |  Client Correction  |
                +----------+----------+
                           |
                    +------+------+
                    |             |
                    v             v
                 Client A      Client B
```

### Vai trò của client

- Nhận input của người chơi và gửi thông tin/hành động qua giao thức Minecraft.
- Hiển thị thế giới, người chơi khác và các thay đổi server cung cấp.
- Có thể hiển thị chuyển động cục bộ để phản hồi nhanh, nhưng **không quyết định trạng thái chuẩn**.

### Vai trò của server

- Quản lý phiên kết nối và một thế giới chung (`Instance`).
- Nắm giữ trạng thái được công nhận của người chơi và thế giới.
- Xác thực những cập nhật/hành động mà bài demo cần kiểm soát.
- Áp dụng thay đổi hợp lệ và đồng bộ cho client.
- Từ chối hoặc sửa các thay đổi không hợp lệ.

## 4. Trạng thái được server quản lý

| Dữ liệu | Ví dụ | Quyền quyết định |
|---|---|---|
| Player state | ID, vị trí được chấp nhận, hướng nhìn, instance | Server |
| World state | Block tại tọa độ cụ thể, thay đổi của thế giới | Server |
| Interaction state | Yêu cầu tương tác nào được cho phép | Server |
| Connection state | Người chơi tham gia hoặc rời server | Server |

Minestom có sẵn nhiều cơ chế gửi cập nhật entity và block qua giao thức Minecraft. Dự án nên **tận dụng các cơ chế đồng bộ có sẵn**, thay vì tự xây lại toàn bộ hệ thống mạng.

## 5. Nội dung demo multiplayer

Demo sử dụng **một thế giới Minecraft đơn giản** và **hai hoặc nhiều người chơi**. Không có luật chơi hay mục tiêu thi đấu riêng.

### Demo 1 — Nhiều người chơi trong cùng thế giới

- Client A và Client B cùng kết nối đến Minestom server.
- Cả hai xuất hiện trong cùng một instance.
- Khi A di chuyển, B có thể quan sát vị trí/cập nhật của A; ngược lại cũng vậy.
- Khi một người chơi ngắt kết nối, server cập nhật danh sách entity cho những client còn lại.

**Mục đích:** Chứng minh kết nối multiplayer và việc server phân phối trạng thái giữa các client.

### Demo 2 — Đồng bộ thay đổi thế giới

- Người chơi gửi yêu cầu tương tác với một block trong khu vực demo.
- Server kiểm tra điều kiện tương tác (ví dụ: khoảng cách hoặc vị trí hợp lệ).
- Nếu hợp lệ, **server thay đổi block trong instance**.
- Cả Client A và B đều nhận và nhìn thấy cùng một thay đổi.
- Nếu không hợp lệ, server giữ nguyên block; client không có quyền tự áp dụng thay đổi lên trạng thái chung.

**Mục đích:** Chứng minh server là nguồn quyết định đối với world state. Logic tương tác và kiểm tra phải được triển khai trong dự án, không mặc định Minestom sẽ tự xây dựng toàn bộ gameplay vanilla.

### Demo 3 — Server validation và state correction

- Server lưu thông tin vị trí đã chấp nhận và các ngữ cảnh di chuyển cần thiết.
- Một cập nhật di chuyển bất thường được gửi trong môi trường kiểm thử có kiểm soát.
- Server đánh giá cập nhật theo quy tắc validation đã xây dựng.
- Nếu không hợp lệ, server từ chối cập nhật, ghi log và điều chỉnh vị trí client về trạng thái hợp lệ khi cần.
- Các client khác không được coi cập nhật bị từ chối là trạng thái hợp lệ cuối cùng.

**Mục đích:** Chứng minh client không có toàn quyền thay đổi trạng thái người chơi.

> **Lưu ý:** Minecraft vanilla không có tính năng sẵn để cố ý gửi packet gian lận. Tình huống bất thường có thể được chứng minh bằng kiểm thử tự động, packet/test harness hoặc một công cụ thử nghiệm riêng; không xem lệnh teleport do server tự thực hiện là bằng chứng phát hiện cheat.

## 6. Luồng xử lý authoritative

Ví dụ với một yêu cầu tương tác:

```text
1. Client A gửi yêu cầu tương tác
2. Minestom server nhận sự kiện
3. Server đọc authoritative state
4. Server kiểm tra điều kiện hành động
5a. Hợp lệ   -> Server cập nhật world state
5b. Không hợp lệ -> Server từ chối, giữ nguyên state
6. Server đồng bộ kết quả cho các client liên quan
7. Ghi log quyết định xử lý
```

Nguyên tắc quan trọng là **client gửi yêu cầu; server quyết định kết quả**. Với vị trí người chơi, client Minecraft vanilla gửi thông tin di chuyển lên server; ứng dụng Minestom cần triển khai các quy tắc kiểm tra tương ứng nếu muốn hạn chế cập nhật bất thường.

## 7. Giới hạn kỹ thuật

- **Authoritative server không đồng nghĩa với chống được mọi loại cheat.** Demo chỉ chứng minh các quy tắc validation được triển khai và kiểm thử.
- **State correction** ở đây là server bác bỏ/sửa trạng thái không hợp lệ (chẳng hạn cập nhật lại vị trí).
- **Full client-side reconciliation** theo thuật toán input history, rewind và replay **không nằm trong phạm vi** vì Minecraft Java vanilla không được thiết kế sẵn như một client tùy chỉnh cho bài toán này.
- Minestom không phải server Minecraft vanilla hoàn chỉnh; cần tự cài đặt logic gameplay hoặc tương tác nào bài demo sử dụng.
- Phải chọn phiên bản Minecraft Java Edition tương thích với phiên bản Minestom của dự án.

## 8. Các thành phần dự kiến

```text
src/main/java/.../
├── Main.java                    # Khởi động Minestom server
├── world/
│   └── WorldManager.java        # Tạo và quản lý instance
├── player/
│   └── PlayerEventHandler.java  # Join / Leave / Move
├── state/
│   └── GameStateManager.java    # Trạng thái chuẩn của demo
├── validation/
│   ├── MovementValidator.java   # Kiểm tra cập nhật di chuyển
│   └── InteractionValidator.java# Kiểm tra tương tác
└── diagnostics/
    └── ServerLogger.java        # Ghi log accepted / rejected
```

Cấu trúc này chỉ là **gợi ý thiết kế**, có thể gộp bớt lớp khi triển khai bản demo nhỏ. Đồng bộ entity/block cơ bản tiếp tục sử dụng khả năng sẵn có của Minestom.

## 9. Tiêu chí hoàn thành

- [ ] Minestom server chạy được và chấp nhận ít nhất hai Minecraft client cùng lúc.
- [ ] Các client tham gia cùng thế giới và nhìn thấy nhau.
- [ ] Thay đổi world state do server chấp nhận được hiển thị nhất quán trên các client.
- [ ] Ít nhất một loại hành động/cập nhật không hợp lệ bị server từ chối.
- [ ] Có minh họa state correction hoặc trạng thái được giữ nguyên sau khi từ chối.
- [ ] Có log hoặc bài test chứng minh server là bên quyết định cuối cùng.

## 10. Kết luận

Dự án sử dụng **Minestom làm authoritative Minecraft server** và **Minecraft Java Edition làm client** để trình bày kiến trúc multiplayer cơ bản. Kết quả cần đạt là nhiều client tương tác trong cùng một thế giới, trạng thái hợp lệ do server quản lý và đồng bộ, còn dữ liệu không hợp lệ có thể bị từ chối hoặc hiệu chỉnh.

**Đây là demo kiến trúc server-authoritative, không phải một minigame hoàn chỉnh.**

## Tài liệu tham khảo

- [Minestom Documentation](https://minestom.net/docs/introduction)
- [Minestom — Your First Server](https://minestom.net/docs/setup/your-first-server)
- [Minestom — Events](https://minestom.net/docs/feature/events)
- [Minestom API — PlayerMoveEvent](https://javadoc.minestom.net/net.minestom.server/net/minestom/server/event/player/PlayerMoveEvent.html)
