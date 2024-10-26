
# 섹션5. 두 번째 요구사항 추가하기 - 도서 대출 현황

## 25강. 유저 대출 현황 보여주기 - 프로덕션 코드 개발
1. join 쿼리의 종류와 차이점을 이해한다.
2. JPA N + 1 문제가 무엇이고 발생하는 원인을 이해한다.
3. N + 1 문제를 해결하기 위한 방법을 이해하고 할용할 수 있다.
4. 새로운 API를 만들 떄 생길 수 았는 고민 포인트를 이해하고 적절한 감을 잡을 수 있다.

### 요구사항 2 확인
#### 유저 대출 현황 화면
- 유저 대출 현황을 보여준다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/25_01.png?raw=true)

- 과거에 대출했던 기록과 현재 대출 중인 기록을 보여준다.
- 아무런 기록이 없는 유저도 화면에 보여져야 한다.
  이 요구사항을 달성하기 위한 클라이언트는 먼저 개발이 끝나 있는 상황이다.
  클라이언트가 원하는 스펙에 맞춰 API를 만들어 주는 것이다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/25_02.png?raw=true)

### Controller를 찾을 수 있는 몇 가지 방법

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/25_03.png?raw=true)

#### 1. IntelliJ 전체 검색
[전체 검색 단축키]
MAC : Command + Shift + F
Windows / Linux : Ctrl + Shift + F

#### 2. API full URL을 모아두는 Kotlin File 사용

```
@RestController
class UserController(

    private val userService: UserService,
) {

    @PostMapping(USER)
    fun saveUser(@RequestBody request: UserCreateRequest) {
        userService.saveUser(request)
    }

    @GetMapping("/user")
    fun getUser() : List<UserResponse> {
        return userService.getUsers()
    }
    
    ```
}
```

- UrlConstants.kt
```
package com.group.libraryapp.controller

const val USER = "/user"
```


#### 3. IntelliJ 유료 버전의 Endpoints 사용색

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/25_04.png?raw=true)

```
@RestController
class UserController(

    private val userService: UserService,
) {

    @GetMapping("/user/loan")
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userService.getUserLoanHistories()
    }
    
    ```
}
```

```
@Service
class UserService(

    private val userRepository: UserRepository,
  
) {

    ```

    @Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAll().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map { history ->
                    BookHistoryResponse(
                        name = history.bookName,
                        isReturn = history.status == UserLoanStatus.RETURNED
                    )
                }
            )
        }
    }

}
```

```
package com.group.libraryapp.dto.user.response

class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
)

data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
)
```

## 26강. 유저 대출 현황 보여주기 - 테스트 코드 개발
### 무엇을 검증해야 할까?!
1. 사용자가 지금까지 한 번도 책을 빌리지 않은 경우 API 응답에 잘 포함되어 있어야 한다.
2. 사용자가 책을 빌리고 아직 반납하지 않은 경우 isReturn 값이 false로 잘 들어 있어야 한다.
3. 사용자가 책을 빌리고 반납한 경우 isRetrun 값이 true로 잘 들어 있어야 한다.
4. 사용자가 책을 여려권 빌렸는데, 반납을 한 책도 있고 하지 않은 책도 있는 경우  
   중첩된 리스트에 여러 권이 정상적으로 들어가 있어야 한다.

```
@SpringBootTest
class UserServiceTest @Autowired constructor(

    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    
    ```
    
    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다.")
    fun getUserLoanHistoriesTest() {
        // given
        userRepository.save(User("A", null))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()

    }

    @Test
    @DisplayName("대출 기록이 많은 유저의 응답이 정상 동작한다")
    fun getUserLoanHistoriesTest2() {
        // given
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED),
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
            .containsExactlyInAnyOrder("책1", "책2", "책3")
        assertThat(results[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)

    }
}
```

## 27강. N+1 문제와 N+1 문제가 발생하는 이유
### N + 1 문제를 해결하는 방법
SQL의 join query를 알아야 한다!


## 28강. SQL join에 대해 알아보자

- 쿼리 한 번으로 두 테이블의 결과를 한 번에 보는 법!
```
select * from user 
join user_loan_history
on user.id = user_loan_history.user_id
```

- join을 사용할 떄 '별칭'을 줄 수도 있다.
```
select * from user u 
join user_loan_history ulh
on u.id = ulh.user_id
```

- left join
```
select * from user u
left join user_loan_history ulh
on u.id = ulh.user_id
```

## 29강. N+1 문제를 해결하는 방법! fetch join

### N+1 발생
```
interface UserRepository : JpaRepository<User, Long> {
    
    ```
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.userLoanHistories")
    fun findAllWithHistories(): List<User>
    
    ```
}
```

```
Hibernate: 
    select
        distinct user0_.id as id1_1_,
        user0_.age as age2_1_,
        user0_.name as name3_1_ 
    from
        user user0_ 
    left outer join
        user_loan_history userloanhi1_ 
            on user0_.id=userloanhi1_.user_id
Hibernate: 
    select
        userloanhi0_.user_id as user_id4_2_0_,
        userloanhi0_.id as id1_2_0_,
        userloanhi0_.id as id1_2_1_,
        userloanhi0_.book_name as book_nam2_2_1_,
        userloanhi0_.status as status3_2_1_,
        userloanhi0_.user_id as user_id4_2_1_ 
    from
        user_loan_history userloanhi0_ 
    where
        userloanhi0_.user_id=?
Hibernate: 
    select
        user0_.id as id1_1_,
        user0_.age as age2_1_,
        user0_.name as name3_1_ 
    from
        user user0_
Hibernate: 
    select
        userloanhi0_.user_id as user_id4_2_0_,
        userloanhi0_.id as id1_2_0_,
        userloanhi0_.id as id1_2_1_,
        userloanhi0_.book_name as book_nam2_2_1_,
        userloanhi0_.status as status3_2_1_,
        userloanhi0_.user_id as user_id4_2_1_ 
    from
        user_loan_history userloanhi0_ 
    where
        userloanhi0_.user_id=?
```
#### fetch join
```
interface UserRepository : JpaRepository<User, Long> {
    
    ```
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userLoanHistories")
    fun findAllWithHistories(): List<User>
    
    ```
}
```
```
Hibernate: 
    select
        distinct user0_.id as id1_1_0_,
        userloanhi1_.id as id1_2_1_,
        user0_.age as age2_1_0_,
        user0_.name as name3_1_0_,
        userloanhi1_.book_name as book_nam2_2_1_,
        userloanhi1_.status as status3_2_1_,
        userloanhi1_.user_id as user_id4_2_1_,
        userloanhi1_.user_id as user_id4_2_0__,
        userloanhi1_.id as id1_2_0__ 
    from
        user user0_ 
    left outer join
        user_loan_history userloanhi1_ 
            on user0_.id=userloanhi1_.user_id
Hibernate: 
    select
        user0_.id as id1_1_,
        user0_.age as age2_1_,
        user0_.name as name3_1_ 
    from
        user user0_
Hibernate: 
    select
        userloanhi0_.user_id as user_id4_2_0_,
        userloanhi0_.id as id1_2_0_,
        userloanhi0_.id as id1_2_1_,
        userloanhi0_.book_name as book_nam2_2_1_,
        userloanhi0_.status as status3_2_1_,
        userloanhi0_.user_id as user_id4_2_1_ 
    from
        user_loan_history userloanhi0_ 
    where
        userloanhi0_.user_id=?
```

## 30강. 조금 더 깔끔한 코드로 변경하기
### DTO의 정적 팩토리 메소드를 써서 리팩토링

```
    @Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAllWithHistories().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map { history ->
                    BookHistoryResponse(
                        name = history.bookName,
                        isReturn = history.status == UserLoanStatus.RETURNED
                    )
                }
            )
        }
    }
```
```
class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
)

data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
) {
    companion object {
        fun of(history: UserLoanHistory): BookHistoryResponse {
            return BookHistoryResponse(
                name = history.bookName,
                isReturn = history.status == UserLoanStatus.RETURNED
            )
        }
    }
}
```

#### 리펙토링 1
```
@Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAllWithHistories().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map { history ->
                    BookHistoryResponse.of(history)
                }
            )
        }
    }
```

#### 리펙토링 2
```
    @Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAllWithHistories().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map(BookHistoryResponse::of)
            )
        }
    }
```
```
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

    val isReturn: Boolean
        get() = this.status == UserLoanStatus.RETURNED
        
    ```    
```

```
    companion object {
        fun of(history: UserLoanHistory): BookHistoryResponse {
            return BookHistoryResponse(
                name = history.bookName,
                isReturn = history.isReturn
            )
        }
    }
```

#### 나머지 부분도 정석 팩토리 메소드로 리펙토
```
    @Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAllWithHistories().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map(BookHistoryResponse::of)
            )
        }
    }
```

```
class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
) {
    companion object {
        fun of(user: User): UserLoanHistoryResponse {
            return UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map(BookHistoryResponse::of)
            )
        }
    }
}

```

```
    @Transactional(readOnly = true)
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAllWithHistories().map(UserLoanHistoryResponse::of)
        
    }
```

## 31강. 두 번째 요구사항 클리어!

1. 새로운 기능을 추가할때, 위치에 관한 고민과 따른 장단점
2. 복잡한 기능을 추가할 때, 테스트 코드를 작성하는 방법
3. SQL의 inner join, left join 특징과 차이
4. N + 1 문제를 해결하기 위해 fetch join을 사용하는 방법

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)