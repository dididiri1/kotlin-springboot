# 섹션7. 네 번째 요구사항 추가하기 - Querydsl

## 37강. Querydsl 도입하기
- JPQL과 Querydsl의 장단점을 이해할 수 있다.
- Querydsl을 Kotlin + Spring Boot와 함께 사용할 수 있다.
- Querydsl을 활용해 기존에 존재하던 Repository를 리팩토링 할 수 있다.

### JPQL은 무슨 단점이 있을까?
- 문자열이기 떄문에 '버그'를 찾기가 어렵다.
- JPQL 문법이 일반 SQL와 조금 달라 복잡한 쿼리를 작성할 때마다 찾아보아야 한다.
``` 
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userLoanHistories")
fun findAllWithHistories(): List<User>
```

### Spring Data JPA는 무슨 단점이 있을까?
- 조건이 복잡한 동적쿼리를 작성할 때 함수가 계속해서 늘어난다.
- 프로덕션 코드가 변경됬을때 도메인의 필드가 변경되면 프로덕션 코드 변경에 취약하다.
- 동적 쿼리 작성이 어렵다.
```
fun findByName(name: String) : User?

fun findByNameAndAge(userName: String, age: Int?): User?
```

### 이런 단점을 보완하기 위해 Querydsl이 등장!
Sprung Data JPA와 Querydsl을 함께 사용하며 서로를 보완해야 한다.

```
fun findAll(name: String): List<User> {
  return queryFactory.select(user)
     .from(user)
     .where(
       user.name.eq(name)
     )
     .fetch()  
}
```

## 38강. Querydsl 사용하기 - 첫 번째 방법

### Querydsl을 적용한 새로운 Repository 구조

먼저, 기존 Repository 구조에서 UserRepositoryCustom interface를 추가한다.
UserRepositoryCustom은 UserRepository와 같은 패키지에 넣어준다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/38_01.png?raw=true)

```
interface UserRepositoryCustom {

}
```

```
interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {
  
  fun findByName(userName: String): User?
  
}
```

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/38_02.png?raw=true)

```
class UserRepositoryCustomImpl : UserRepositoryCustom {

}
```
이제 다음과 같이 JPAQueryFactory를 스프링 Bean으로 등록 해준다. 이 JPAQueryFactory를  
활용해서 querydsl 코드를 작성할 예정이다. QuerydslConfig는   
com.group.libraryapp.config 패지키 안에 만들어 준다.
```
@Configuration
class QuerydslConfig(
private val em: EntityManager, ){
  @Bean
  fun querydsl(): JPAQueryFactory {
    return JPAQueryFactory(em)
  }
}
```

이제 UserRepositoryCustom에 필요한 함수를 입력하고, UserRepositoryCustomImpl에
querydsl을 구현하면 된다.


```
class UserRepositoryCustomImpl(
  private val queryFactory: JPAQueryFactory,
) : UserRepositoryCustom {
  override fun findAllWithHistories(): List<User> {
    return queryFactory.select(user).distinct()
      .from(user)
      .leftJoin(userLoanHistory).on(userLoanHistory.user.id.eq(user.id)).fetchJoin()
      .fetch()
} }
```

### 장점
서비스단에서 UserRepository 하나만 사용하면 된다.

### 단점
인터페이스와 클래스를 항상 같이 만들어 주어야 하는 것이 부담이고 여러모로 번거롭다.

## 39강. Querydsl 사용하기 - 두 번째 방법

이번 시간에는 지난 시간에 했던 방법과 다른 방법을 활용하여 Querydsl을 사용할 것이다.  
먼저 BookQuerydslRepository를 만들자. Book.kt가 위치한 doamin 패키지에 만들어도   
되지만 이번에는 com.group.libraryapp.repository.book 패키지를 만들어 그 안에 넣어 주겠다.

```
@Component
class BookQuerydslRepository(

  private val queryFactory: JPAQueryFactory,
   
){

}
```
JPAQueryFactory 를 주입 받을 수 있도록 @Componect 어노테이션을 붙여주자! @Repository  
를 붙이더라도 상관 없다.

### JPQL
```
@Query("SELECT NEW com.group.libraryapp.dto.book.request.reponse.BookStatResponse(b.type, COUNT(b.id)) FROM Book b GROUP BY b.type")
fun getStats(): List<BookStatResponse>
```

### QueryDSL
```
fun getStats(): List<BookStatResponse> {
    return queryFactory
      .select(
        Projections.constructor(
          BookStatResponse::class.java,
          book.type,
          book.id.count(),
        )
      )
      .from(book)
      .groupBy(book.type)
      .fetch() 
}
```
- 이번엔 Projections.constructor() 를 사용하였다.
    - 주어진 DTO의 생성자를 호출한다는 뜻이다.
    - Projections.constructor() 안에는 세 가지 파라미터가 들어갔다.  
      BookStatResponse::class.java book.type book.id.count()
- sselect from을 포함해 SQL으로 바꾸면 다음과 같다.
    - select book.type, count(book.id) from book
- groupBy(book.type) 은 SQL로 다음과 같다.
    - group by type

### 2번째 방법의 장점
- 클래스만 바로 만들면 되어 간결하다.
### 2번째 방법의 단점
- 필요에 따라 두 Repository를 모두 불러와야 한다.

### 어떤 방식이 더 좋을까?
개인적으로는 지금 방법을 선호한다.  
**멀티 모듈**을 사용하는 경우 모듈 별로만 Repository를 쓰는 경우가 많기 때문

## 40강. UserLoanHistoryRepository를 Querydsl으로 리팩토링 하기

이번 시간에는 UserLoanHistoryRepository에 있는 기능들을 Querydsl으로 옮겨볼 것이다.
그전에 잠시 현재 UserLoanHistryRepository를 살펴보자.

### 현재의 UserLoanHistoryRepository
```
interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long> {
  
  fun findByBookNameAndStatus(bookName: String, status: UserLoanStatus): UserLoanHistory?
  
  fun countByStatus(status: UserLoanStatus): Long
  
}
```
지금 존재하는 쿼리들은 모두 @Query를 사용하지 않고 Spring Data JAP가 자동으로 만들어 준
쿼리들이다. 이런 쿼리들도 Querydsl로 옮겨야 할까?

개인적으로는 이 쿼리들 역시 Querydsl로 옮기는 것을 선호한다. 그 이유는 Querydsl을 사용함
으로써 얻을 수 있는 장점인 **'동적 쿼리의 간편함'** 떄문이다.

예를 들어 UserLoanHistoryRepository에 다음 함수도 추가로 존재했다고 하자.
```
fun findByBookName(bookName: String): UserLoanHistory?
```
이 기능은 책 이름을 기준으로 UserLoanHistory를 찾는 기능으로 findByBookNameAndStatus와  
유사하지만 status가 존재하지 않는다.  
이렇게 2가지 쿼리, findByBookName과 findByBookNameAndStatus를 보면서 느껴지는 기준이 있다.  
바로 Repository의 함수가 정말 많이 늘어날 수 있다는 사실이다!!
예를 들어, 다음과 같은 조건들을 모두 만족하는 Repository 함수가 필요하다고 해보자,  
단순한 and 조건의 쿼리이지만, 그 필드의 종류가 계속해서 달라질 수 있다.
- findByA
- findByAANDB
- findByAAndC
- ...
- findByAAndBAndCAndDAndE
  이렇케 함수가 늘어나는 불폄함은 Querydsl을 이용하면 간단히 해결된다.

- UserLoanHistoryRepository를 Querydsl로 변경해보자! 두 번째 방법을 사용해보겠다.
```
@Component
class UserLoanHistoryQuerydslRepository(
private val queryFactory: JPAQueryFactory, ){
  fun find(bookName: String): UserLoanHistory? {
    return queryFactory.select(userLoanHistory)
      .from(userLoanHistory)
      .where(
        userLoanHistory.bookName.eq(bookName)
      )
      .limit(1)
      .fetchOne()
  } 
}      
```

우선 가장 첫 번째 함수인 findByBookName을 Querydsl로 옮겨 보았다.
새로 나온 Querydsl 문법의 의미를 살펴보면 다음과 같다.
- limit(1): SQL의 limit 1 이라는 의미로 모든 검색 결과에서 1개만을 가져온다는 것이다.
- fatchOne(): List<Entity>로 조회 결과를 반환하는 대신 Entity 하나만으로 조회 결과를 반환한다.

좋다~ 이제 findByBookNameAndStatus 역시 추가로 구현을 해볼 것이다. 이때 우리는 Kotlin의  
특성을 활용해 다음과 같이 구현할 수 있다.

```
fun find(bookName: String, status: UserLoanStatus? = null): UserLoanHistory? {
  return queryFactory.select(userLoanHistory)
    .from(userLoanHistory)
    .where(
      userLoanHistory.bookName.eq(bookName),
      status?.let { userLoanHistory.status.eq(status) }
    )
.limit(1)
    .fetchOne()
}
```
- find 함수가 status라는 파라미터를 추가로 받게 했다. 이때 default parameter를 null로  
  넣었고 덕분에 외부에는 bookName만 사용할 수도 booName과 status를 같이 사용할 수 도 있다.
- ?.let 을 활용해 status 파라미터가 null인 경우에는 where 조건에 user_loan_history.status = status  
  가 들어가지 않도록 하였따.
    - where에 들어오는 조건이 null이면 Querydsl은 이를 무시하게 된다.
    - 또한 where에 여러 조건이 들어오면 각 조건이 AND로 결합한다.
    - 즉, status가 null이 아닌 경우메나 book_name = ? and status = ? 가 실행된다는 의미이다.
      이어서 countByStatus도 변경해보자.

```
fun count(status: UserLoanStatus): Long {
    return queryFactory.select(userLoanHistory.count())
      .from(userLoanHistory)
      .where(
        userLoanHistory.status.eq(status)
      )
      .fetchOne() ?: 0L
  }
```
추가적인 Querydsl 문법은 다음과 같다.
- userLoanHistory.count() : count(id) 로 변경된다.
- fatchOne() ? : 0L : count의 결과는 수자 1개이므로 fetchOne() 을 사용해준다! 혹시나  
  결과가 비어 있다면 OL을 반환하도록 elivs 연산자를 사용한다.

## 41강. 마지막 요구사항 클리어!
우리는 아래와 같은 기술적인 요구사항을 완벽하게 적용하였다!
**기술적인 요구사항**
- 현재 사용하는 JPQL은 몇가지 단점이 있다.
- Querydsl을 적용해서 단점을 극복하자.

1. JQPL과 Querydsl의 장점을 이해한다.
2. Querydsl을 Kotlin + Spring Boot와 함계 사용하고, 2가지 방식의 장단점을 이해한다.
3. Querydsl의 기본적인 사용법을 익힌다.
4. Querydsl을 활용해 기존 Repository를 리팩토링한다.

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)