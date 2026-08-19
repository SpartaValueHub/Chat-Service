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
