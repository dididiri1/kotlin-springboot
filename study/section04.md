# 섹션4. 첫 번째 요구사항 추가하기 - 책의 분야

## 21강. 책의 분야 추가하기

### 3 첫 번쩨 요구사항 추가하기 - 책의 분야
1. type, Status 등을 서버에서 관리하는 방법들을 살펴보고 장단점을 이해한다.
2. Test Fixture의 필용성을 느끼고 구성하는 방버을 알아본다.
3. Kotlin에서 Enum + JPA + Spring Boot를 활용하는 방법을 알아본다.

## 요구사항1 확인

### 책 등록 요수사항 추가
- 책을 등록할 때에 '분야'를 선택해야 한다.
    - 분야에는 5가지 분야가 있다 - 컴퓨터 / 경제 / 사회 / 언어 / 과학

#### Test Fixture - 정적 메소드 만들기
- 이런 패턴을 어려운 말로 Object Model 패턴이라고도 한다.
```
import java.lang.IllegalArgumentException
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
class Book(
    val name: String,

    val type: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    companion object {
        fun fixture(
            name: String = "책 이름",
            type: String = "COMPUTER",
            id: Long? = null,
        ): Book {
            return Book(
                name = name,
                type = type,
                id = id,
            )
        }
    }
}
```

## 22강. Enum Class를 활용해 책의 분야 리팩토링 하기

### type: String의 단점 정리
1. 현재 검증이 되고 있지 않으며, 검증 코드를 추가 작성하기 번거롭다.
2. 코드만 보았을 때 어떤 값이 DB에 있는지 알 수 없다.
3. type과 관련한 새로운 로직을 작성할 때 번거롭다.

### 이러한 단점을 어떻게 하결할수 있을까?!
- Enum Class를 활용하자!

```
enum class BookType {

    COMPUTER,
    ECONOMY,
    SOCIETY,
    LANGUAGE,
    SCIENCE,
    
}
```

```

import java.lang.IllegalArgumentException
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
class Book(
    val name: String,

    val type: BookType,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    companion object {
        fun fixture(
            name: String = "책 이름",
            type: BookType = BookType.COMPUTER,
            id: Long? = null,
        ): Book {
            return Book(
                name = name,
                type = type,
                id = id,
            )
        }
    }
}
```

### 위에 Enum은 숫자로 DB에 저장되는 문제가 있다.
1. 기존 Enum의 순서가 바뀌면 아주 큰일이 난다.
2. 기존 Enum 타입의 삭제, 새로운 Enum 타입의 추가가 제한적이다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/22_01.png?raw=true)

### 정리
1. Type을 문자열로 관리 할때는 몇 가지 단점이 존재한다.
2. Enum Class를 활용하면 손쉽게 단점을 제거할 수 있다.
3. Enum Class를 Entity에 사용할 때는 @Enumerated(EnumType.String) 을 잘 활용해 주어야 한다.

## 23강. Boolean에도 Enum 활용하기 - 책 반납 로직 수정

### 새로운 요구사항 : 휴면 여부를 관리해 주세요.
- isActive가 true이면 휴면이 아닌 유저
- isActive가 false이면 휴면인 유저

### 한 달 후, 새로운 요구사항 : 유저의 탈퇴 여부를 soft하게 관리 헤주세요.
> soft 방식 : 실제 데이터베이스에는 데이터가 남아있지만 시스템상으로는 삭제된 방식

### flag 필드 방식 - Boolean 필드 (FLAG)
```
@Entity
class User(
  val name: String,
  val isActive: Boolean,
  val isDeleted: Boolean,
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
)
```

### Boolean이 2개가 되면 문제가 생긴다!
#### 문제 1. Boolean이 2개 있기 때문에 코드가 이해하기 어려워진다.
- 한 객체가 여러 상태를 표현할 수록 이해하기 어렵다.
- 현재 경우의 수는 2^2, 즉 4가지이다.

#### 문제 2. Boolean 2개로 표현되는 4가지 상태가 모두 유의미하지 않다.
- (isActive, isDeleted)는 총 4가지 경우가 있다.
    - (false, false) - 휴면 상태인 유저
    - (false, true) - 휴면이면서 탈퇴한 유저일 수는 없다.
    - (true, false) - 활성화된 유저이다.
    - (true, true) - 탈퇴한 유저이다.
- 2번째 경우는 DB에 존재할 수 없는 조합이고, 이런 경우가 '코드'에서 가능한 것은 유지보수를 어렵게 만든다.


### 해결책
#### Enum을 도입하면 해결 할 수 있다! 이렇케 Enum을 활용하게 되면
1. 필드 1개로 여러 상태를 표현할 수 있기 때문에 코드의 이해가 쉬워지고
2. 정확하게 유의미한 상태만 나타낼 수 있기 때문에 코드의 유지보수가 용이해진다.


```
@Entity
class User(
  val name: String,
  @Enumerated(STRING)
  val status: UserStatus,
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
)
```
```
enum class UserStatus {
  ACTIVE,
IN_ACTIVE, }
```

### UserLoanHistory - Enum 변경

#### 변경 전
```
import com.group.libraryapp.domain.user.User
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class UserLoanHistory(

    @ManyToOne
    val user: User,

    val bookName: String,

    var isReturn: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun doReturn() {
        this.isReturn = true
    }

}
```
#### 변경 후
```
enum class UserLoanStatus {

    RETURNED, // 반납 되어 있는 상태
    LOANED, // 대출 중인 상태
}
```

```
import com.group.libraryapp.domain.user.User
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class UserLoanHistory(

    @ManyToOne
    val user: User,

    val bookName: String,

    var status: UserLoanStatus = UserLoanStatus.LOANED,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun doReturn() {
        this.status = UserLoanStatus.RETURNED
    }

    companion object {
        fun fixture(
            user: User,
            bookName: String = "이상한 나라의 엘리스",
            status: UserLoanStatus = UserLoanStatus.LOANED,
            id: Long? = null,
        ): UserLoanHistory {
            return UserLoanHistory(
                user = user,
                bookName = bookName,
                status = status,
                id = id
            )
        }
    }
}
```

```
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    fun loanBookTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("홍길동", null))
        val request = BookLoanRequest("홍길동", "이상한 나라의 엘리스")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("이상한 나라의 엘리스")
        assertThat(results[0].user.id).isEqualTo(saveUser.id)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("책이 진작 대출되어 있다면, 신규 대출이 실패 한다")
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("홍길동", null))

        userLoanHistoryRepository.save(UserLoanHistory.fixture(saveUser, "이상한 나라의 엘리스"))
        val request = BookLoanRequest("홍길동", "이상한 나라의 엘리스")

        // when then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다.")
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    fun returnBookTest() {
        //given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(saveUser, "이상한 나라의 엘리스"))
        val request = BookReturnRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)

    }
}
```

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)