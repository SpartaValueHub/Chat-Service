# 채팅방 API

## 채팅방 생성

### Summary

상품 게시글상세에서 1:1 채팅방을 만들거나, 같은 게시글·같은 참여자 쌍이면 기존 방을 반환합니다.
요청 본문의 상품·판매자 스냅샷은 Chat 로컬 Read Model에 upsert합니다.

### Method · Path

`POST /api/v1/chat/rooms`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다. (구매자 UUID)

### Request

Header

| 필드          | 타입   | 필수 | 제약             |
| ------------- | ------ | ---- | ---------------- |
| X-Member-Uuid | string | O    | 구매자 회원 UUID |

Body

| 필드              | 타입   | 필수 | 제약                                                          |
| ----------------- | ------ | ---- | ------------------------------------------------------------- |
| productPostUuid   | string | O    | 상품 게시글상세에서 전달. Chat이 product_post를 조회하지 않음 |
| sellerUuid        | string | O    | 상품 게시글상세의 판매자 UUID. 구매자와 같으면 안 됨          |
| productPostImageUrl | string | O    | 게시글 상세 이미지 |
| productPostName     | string | O    | 게시글명 |
| price             | number | O    | 가격                                                          |
| tradeStatus       | string | O    | 거래 상태. `SELLING` `RESERVED` `SOLD_OUT` |
| sellerNickname      | string | O    | 상품 게시글 상세의 판매자 닉네임 |
| sellerMemberGrade   | string | O    | 판매자 회원 등급. `BRONZE` `SILVER` `GOLD` `PLATINUM` `DIAMOND` |

```json
{
  "productPostUuid": "11111111-1111-4111-8111-111111111111",
  "sellerUuid": "33333333-3333-4333-8333-333333333333",
  "productPostImageUrl": "https://cdn.example.com/products/111.png",
  "productPostName": "중고 노트북",
  "price": 350000,
  "tradeStatus": "SELLING",
  "sellerNickname": "판매자닉",
  "sellerMemberGrade": "GOLD"
}
```

### Response

신규 생성: `201`  
기존 방 재사용: `200`

상대 닉네임·이미지는 포함하지 않습니다. 목록/상세에서 Read Model을 조합합니다.

| 필드            | 타입    |
| --------------- | ------- |
| roomId          | string  |
| productPostUuid | string  |
| buyerUuid       | string  |
| sellerUuid      | string  |
| reused          | boolean |

```json
{
  "roomId": "67a1c2d3e4f5a6b7c8d9e0f1",
  "productPostUuid": "11111111-1111-4111-8111-111111111111",
  "buyerUuid": "22222222-2222-4222-8222-222222222222",
  "sellerUuid": "33333333-3333-4333-8333-333333333333",
  "reused": false
}
```

### Errors

| status | code                        | 의미                             |
| ------ | --------------------------- | -------------------------------- |
| 401    | CHAT_AUTH_MISSING           | X-Member-Uuid 헤더 없음          |
| 400    | INVALID_REQUEST             | 필수 본문 필드 없음              |
| 400    | CANNOT_CHAT_WITH_SELF       | 구매자와 판매자가 동일           |
| 404    | CHAT_USER_PROFILE_NOT_FOUND | Member에서 프로필을 찾을 수 없음 |
| 503    | MEMBER_PROFILE_UNAVAILABLE  | Member 서비스 호출 실패          |

---

## 채팅방 목록

### Summary

로그인한 회원이 참여한 1:1 채팅방을 한 번에 반환합니다. 상품 정보는 Read Model을 조합하고, `lastMessage`는 채팅방 문서 값입니다. 상대는 UUID만 포함합니다. 미읽음 수는 이 단계에서 항상 0입니다.

### Method · Path

`GET /api/v1/chat/rooms`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약           |
| ------------- | ------ | ---- | -------------- |
| X-Member-Uuid | string | O    | 회원 UUID      |

Query·Body 없음. 페이징 없음.

### Response

`200`

| 필드 | 타입 |
| ---- | ---- |
| rooms | array |
| rooms[].roomId | string |
| rooms[].productPost.productPostUuid | string |
| rooms[].productPost.productPostImageUrl | string |
| rooms[].productPost.productPostName | string |
| rooms[].productPost.price | number |
| rooms[].productPost.tradeStatus | string |
| rooms[].counterpart.memberUuid | string |
| rooms[].lastMessage | object \| null |
| rooms[].lastMessage.content | string |
| rooms[].lastMessage.createdAt | string (ISO-8601) |
| rooms[].unreadCount | number |
| rooms[].updatedAt | string (ISO-8601) |

정렬: `lastMessage.createdAt` 내림차순, 없으면 `updatedAt` 내림차순.  
방이 없으면 `rooms`는 빈 배열입니다. 상품 스냅샷이 없으면 상품 필드는 `null`이고 목록 전체는 실패하지 않습니다.

```json
{
  "rooms": [
    {
      "roomId": "67a1c2d3e4f5a6b7c8d9e0f1",
      "productPost": {
        "productPostUuid": "11111111-1111-4111-8111-111111111111",
        "productPostImageUrl": "https://cdn.example.com/products/111.png",
        "productPostName": "중고 노트북",
        "price": 350000,
        "tradeStatus": "SELLING"
      },
      "counterpart": {
        "memberUuid": "33333333-3333-4333-8333-333333333333"
      },
      "lastMessage": {
        "content": "안녕하세요",
        "createdAt": "2026-08-19T04:00:00Z"
      },
      "unreadCount": 0,
      "updatedAt": "2026-08-19T04:00:00Z"
    }
  ]
}
```

### Errors

| status | code              | 의미                    |
| ------ | ----------------- | ----------------------- |
| 401    | CHAT_AUTH_MISSING | X-Member-Uuid 헤더 없음 |

---

## 채팅방 상세

### Summary

입장 시 방 상단과 말풍선용 상대 프로필을 그립니다. 상품 스냅샷과 **물건을 올린 판매자** 닉네임, 그리고 **대화 상대** 닉네임·프로필 이미지를 반환합니다. 판매자 회원 등급은 포함하지 않습니다. 지난 메시지는 이력 API를 사용합니다.

`productPostUuid`는 이미지·제목·가격 클릭 시 상품 상세로 돌아가기 위한 식별자입니다.  
말풍선 왼쪽의 닉네임·사진은 `counterpart`입니다. 메시지마다 Member를 호출하지 않습니다.

### Method · Path

`GET /api/v1/chat/rooms/{roomId}`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드   | 타입   | 필수 | 제약          |
| ------ | ------ | ---- | ------------- |
| roomId | string | O    | Mongo 방 문서 ID |

### Response

`200`

| 필드 | 타입 |
| ---- | ---- |
| roomId | string |
| productPost.productPostUuid | string |
| productPost.productPostImageUrl | string |
| productPost.productPostName | string |
| productPost.price | number |
| productPost.tradeStatus | string |
| seller.memberUuid | string |
| seller.nickname | string |
| counterpart.memberUuid | string |
| counterpart.nickname | string |
| counterpart.profileImageUrl | string |

`seller`는 상품을 올린 사람입니다. 구매자·판매자 모두 같은 닉네임을 봅니다.  
`counterpart`는 1:1에서 나 아닌 참여자입니다. 왼쪽 말풍선은 이 프로필을 씁니다. 내가 판매자면 `seller`와 `counterpart`가 다릅니다.  
상품 스냅샷이 없으면 `productPost`는 UUID만 있고 나머지 필드는 `null`입니다.

```json
{
  "roomId": "67a1c2d3e4f5a6b7c8d9e0f1",
  "productPost": {
    "productPostUuid": "11111111-1111-4111-8111-111111111111",
    "productPostImageUrl": "https://cdn.example.com/products/111.png",
    "productPostName": "버버리 레더 포켓 미니 토트백",
    "price": 1500000,
    "tradeStatus": "RESERVED"
  },
  "seller": {
    "memberUuid": "33333333-3333-4333-8333-333333333333",
    "nickname": "숭남농홍길동"
  },
  "counterpart": {
    "memberUuid": "33333333-3333-4333-8333-333333333333",
    "nickname": "숭남농홍길동",
    "profileImageUrl": "https://cdn.example.com/profiles/333.png"
  }
}
```

### Errors

| status | code                    | 의미                         |
| ------ | ----------------------- | ---------------------------- |
| 401    | CHAT_AUTH_MISSING       | X-Member-Uuid 헤더 없음      |
| 400    | INVALID_REQUEST         | roomId 없음                  |
| 404    | CHAT_ROOM_NOT_FOUND     | 채팅방 없음                  |
| 403    | CHAT_ROOM_ACCESS_DENIED | 참여자가 아님                |
| 404    | CHAT_USER_PROFILE_NOT_FOUND | 판매자·상대 프로필을 찾을 수 없음 |
| 503    | MEMBER_PROFILE_UNAVAILABLE  | Member 서비스 호출 실패      |

---

## 채팅방 메시지 이력

### Summary

입장 시 지난 메시지를 REST로 가져옵니다. 새 말의 실시간 수신은 STOMP를 사용합니다. `chat_messages`를 `createdAt` 기준 최신부터 읽고, 응답은 오래된 순입니다.

### Method · Path

`GET /api/v1/chat/rooms/{roomId}/messages`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드   | 타입   | 필수 | 제약          |
| ------ | ------ | ---- | ------------- |
| roomId | string | O    | Mongo 방 문서 ID |

Query

| 필드   | 타입   | 필수 | 제약 |
| ------ | ------ | ---- | ---- |
| before | string | X    | 이 messageId보다 오래된 페이지. 없으면 최신 페이지 |
| limit  | number | X    | 기본 50, 최대 100 |

### Response

`200`

메시지가 없으면 `messages`는 빈 배열입니다. 말풍선 왼쪽/오른쪽은 `senderUuid`와 내 UUID를 비교합니다.

| 필드 | 타입 |
| ---- | ---- |
| messages | array |
| messages[].messageId | string |
| messages[].senderUuid | string |
| messages[].messageType | string |
| messages[].content | string |
| messages[].metadata | object \| null |
| messages[].metadata.fileSize | string |
| messages[].metadata.imageWidth | number |
| messages[].metadata.imageHeight | number |
| messages[].metadata.reservationId | string |
| messages[].metadata.meetAt | string (ISO-8601) |
| messages[].metadata.price | number |
| messages[].metadata.placeName | string |
| messages[].createdAt | string (ISO-8601) |

`messageType`: `TEXT` `IMAGE` `RESERVATION`. TEXT는 `metadata`가 `null`입니다.

```json
{
  "messages": [
    {
      "messageId": "67b1c2d3e4f5a6b7c8d9e0f1",
      "senderUuid": "22222222-2222-4222-8222-222222222222",
      "messageType": "TEXT",
      "content": "안녕하세요",
      "metadata": null,
      "createdAt": "2026-08-19T01:00:00Z"
    },
    {
      "messageId": "67b1c2d3e4f5a6b7c8d9e0f2",
      "senderUuid": "33333333-3333-4333-8333-333333333333",
      "messageType": "RESERVATION",
      "content": "거래가 예약되었습니다",
      "metadata": {
        "fileSize": null,
        "imageWidth": null,
        "imageHeight": null,
        "reservationId": "res-1",
        "meetAt": "2026-08-31T10:10:00Z",
        "price": 1500000,
        "placeName": "해동병원 앞"
      },
      "createdAt": "2026-08-19T02:00:00Z"
    }
  ]
}
```

위로 더 불러올 때는 현재 페이지에서 가장 오래된 `messageId`를 `before`로 넣습니다. 개수가 `limit`보다 작으면 더 없습니다.

### Errors

| status | code                    | 의미                    |
| ------ | ----------------------- | ----------------------- |
| 401    | CHAT_AUTH_MISSING       | X-Member-Uuid 헤더 없음 |
| 400    | INVALID_REQUEST         | roomId/before/limit 오류 |
| 404    | CHAT_ROOM_NOT_FOUND     | 채팅방 없음             |
| 403    | CHAT_ROOM_ACCESS_DENIED | 참여자가 아님           |

---

## 채팅 메시지 전송 (STOMP)

### Summary

상세 화면에서 말을 보내면 **먼저 Mongo에 저장**한 뒤, 그 방 topic 구독자에게 말풍선을 뿌립니다. 같은 저장 직후, 참여자 개인 큐로 목록 한 줄 미리보기도 푸시합니다. 상대가 구독하지 않아도 저장됩니다. 다시 들어오면 이력 REST로 보입니다.

목록 미리보기용 `chat_rooms.last_message`는 `$set`으로만 갱신합니다. 목록 HTTP를 다시 치지 않아도, `/user/queue/chat-list`를 구독한 소켓은 그 방 한 줄만 패치하면 됩니다.

### Handshake

`GET /ws-chat` (native WebSocket) 또는 SockJS `GET /ws-chat`

Gateway 경유: `/chat-service/ws-chat`

CONNECT 헤더 또는 핸드셰이크 `X-Member-Uuid` (SockJS는 쿼리 `X-Member-Uuid`도 가능)

STOMP prefix

| 방향 | prefix | 예 |
|------|--------|----|
| 클라이언트 → 서버 | `/app` | `/app/chat.{roomId}` |
| 서버 → 클라이언트 | `/topic` | `/topic/chat.{roomId}` |
| 목록 미리보기 (해당 회원) | `/user/queue` | `/user/queue/chat-list` |
| 에러 (해당 세션) | `/user/queue` | `/user/queue/errors` |

### Send

목적지: `/app/chat.{roomId}`

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| messageType | string | X | 기본 `TEXT`. 이 단계 `TEXT` `IMAGE` |
| content | string | O | TEXT=본문, IMAGE=이미지 URL |
| metadata | object | X | IMAGE일 때 용량·가로·세로. TEXT는 null |

```json
{
  "messageType": "TEXT",
  "content": "안녕하세요",
  "metadata": null
}
```

이미지 (업로드는 다른 서비스, Chat은 URL만)

```json
{
  "messageType": "IMAGE",
  "content": "https://cdn.example.com/chat/bag.png",
  "metadata": {
    "fileSize": "2.4MB",
    "imageWidth": 800,
    "imageHeight": 600
  }
}
```

### Broadcast

구독: `/topic/chat.{roomId}`

저장이 끝난 문서와 같은 형태입니다. 말풍선 왼쪽/오른쪽은 `senderUuid`와 내 UUID를 비교합니다.

```json
{
  "messageId": "67b1c2d3e4f5a6b7c8d9e0f3",
  "roomId": "aaaaaaaaaaaaaaaaaaaaaaaa",
  "senderUuid": "22222222-2222-4222-8222-222222222222",
  "messageType": "TEXT",
  "content": "안녕하세요",
  "metadata": null,
  "createdAt": "2026-08-20T05:00:00Z"
}
```

TEXT의 `last_message.content`는 본문, IMAGE는 `사진`입니다.

### List preview

구독: `/user/queue/chat-list`

같은 `/ws-chat` 연결입니다. 전체 목록 화면과, 방 안 왼쪽 목록이 이 큐 하나로 한 줄을 패치합니다. 방마다 `/topic/chat.{roomId}`를 구독하지 않습니다. 방 topic 페이로드는 바꾸지 않습니다.

참여자 UUID마다 `convertAndSendToUser` 합니다. 소켓이 없거나 이 큐를 구독하지 않으면 그 순간에는 도착하지 않고, 나중에 `GET /api/v1/chat/rooms`가 `last_message`를 읽습니다.

`unreadCount`는 이 단계에서 항상 `0`입니다. `lastRead`·뱃지는 후속입니다.

```json
{
  "roomId": "aaaaaaaaaaaaaaaaaaaaaaaa",
  "lastMessage": {
    "content": "안녕하세요",
    "createdAt": "2026-08-20T05:00:00Z"
  },
  "unreadCount": 0,
  "updatedAt": "2026-08-20T05:00:00Z"
}
```

IMAGE 미리보기의 `lastMessage.content`는 `사진`입니다. `updatedAt`은 저장된 메시지 시각과 같습니다.

### Errors

클라이언트는 `/user/queue/errors`를 구독합니다.

| status | code | 의미 |
|--------|------|------|
| 401 | CHAT_AUTH_MISSING | X-Member-Uuid 없음 |
| 400 | INVALID_REQUEST | 본문/타입 오류 |
| 404 | CHAT_ROOM_NOT_FOUND | 채팅방 없음 |
| 403 | CHAT_ROOM_ACCESS_DENIED | 참여자가 아님 |


