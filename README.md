# 1 도서관리 애플리케이션 리팩토링 준비하기
1. Java로 작성된 도서관리 애플리케이션 이해하기
2. 테스트 코드가 무엇인지 왜 필요한지 이해하고, Junit5를 사용해 Spring Boot의 테스트 코드를 작성한다.
3. 실제 만들어진 Java 프로젝트에 대해 Kotlin으로 테스트를 작성하며 Kotlin 코드 작성에 익숙해진다.

### H2 접속
```
spring:
  datasource:
    url: 'jdbc:h2:mem:library'
    username: 'user'
    password: ''
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        show_sql: true
h2: console:
      enabled: true
      path: '/h2-console'
```


![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/01_01.png?raw=true)


# 3강. 코틀린 코드 작성 준비하기

### build.gradle 
```
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
}
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
}
compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
} }
```
```
plugins {
    id 'org.springframework.boot' version '2.6.8'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
}

group = 'com.group'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'

    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

# 4강. 사칙연산 계산기에 대한 테스트 코드 작성하기

## 계산기 요구사항
1. 계산기는 정수만을 취급한다.
2. 계산기가 생성될 때 숫자를 1개 받는다.
3. 최초 숫자가 기록된 이후에는 연산자 함수를 통해 숫자를 받아 지속적으로 계산한다.

### Calculator.kt
```
import java.lang.IllegalArgumentException

data class Calculator(
    private var number: Int,
) {

    fun add(operand: Int) {
        this.number += operand
    }

    fun minus(operand: Int) {
        this.number -= operand
    }

    fun multiply(operand: Int) {
        this.number *= operand
    }

    fun divide(operand: Int) {
        if(operand == 0) {
            throw IllegalArgumentException("0으로 나눌 수 없습니다.")
        }

        this.number /= operand
    }
}
```

### CalculatorTest.kt
```
import java.lang.IllegalArgumentException

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
}

class CalculatorTest {

    fun addTest() {
        val calculator = Calculator(5)
        calculator.add(3)

        val expectedCalculator = Calculator(8)
        if (calculator != expectedCalculator) {
            throw IllegalArgumentException()
        }
    }
}
```

#### 이때 계산기 안에 8이 있는지 확인하는 방법 2가지가 있다.
- 첫번째는 data class의 equals를 사용하는 방법이다.
- data class의 경우 자동으로 equals를 만들어 주기 때문에 8을 가지고 잇는 계산기 인스턴스를  
  만든 다음 != 연산자로두 계산기가 같은지 다른지 확인할 수 있다.
```
data class Calculator(
  private var number: Int,
) {
   // 생략...
}
```
```
fun addTest() {
  val calculator = Calculator(5)
  calculator.add(3)
  val expectedResult = Calculator(8)
  if (calculator != expectedResult) {
    throw IllegalStateException()
  }
}
```

- 두 번째 방법은, Calculator로부터 number을 가져오는 방법이다. 현재는 Calculator가
- private number을 가지고 있기 떄문에 calculator.number로 number 프로퍼티를 가져올 수 없다.
- number를 가져오기 위해서는 간단히 private 대신 public을 사용할 수 있다.

```
class Calculator(
  var number: Int,
){
  // 생략...
}
```
Shift+F6 하면 전체 치완    
- 백킹 프로퍼티 방법(Backing Properties)
- 세 번째 방법은 원래 존재하던 number 라는 프로퍼티를 _number 라는 이름으로 변경하고 불변 number를 추가하는 방법이 있다
```
class Calculator(
  private var _number: Int,
){
  val number: Int
    get() = this._number

  fun add(operand: Int) {
    this._number += operand // this._number           .
  }
  
  // 생략... 
}
```

```
import java.lang.IllegalArgumentException

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
    calculatorTest.minusTest()
    calculatorTest.multiplyTest()
}

class CalculatorTest {

    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        if(calculator.number != 8) {
            throw IllegalArgumentException()
        }
    }
    
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        if(calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }
    
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(2)

        // then
        if(calculator.number != 10) {
            throw IllegalArgumentException()
        }
    }
}
```

## 5강. 사칙연산 계산기의 나눗템 테스트 작성
```
fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.divideExceptionTest()
}

class CalculatorTest {
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when
        try{
            calculator.divide(0)

        }catch (e: IllegalArgumentException) {
            if(e.message != "0으로 나눌 수 없습니다.") {
                throw IllegalStateException()
            }
            // 테스트 성공!!
            return
        } catch (e: Exception) {
            throw IllegalStateException()
        }
        throw java.lang.IllegalStateException("기대하는 예외가 발생하지 않았습니다.")
    }
}
```
### 수동으로 만든 테스트 코드의 단점
1. 테스트 클래스와 메소드가 생길 때마다 메인 메소드에 수동으로 코드를 작성해주어야 하고,
   메인 메소드거 어주 커진다. 테스트 메소드를 개별적으로 실행하기도 어렵다.
2. 테소트가 실패한 경우 무엇을 기대하였고, 어떤 잘못된 값이 들어와 실패했는지 알려주지 않는다. 
   예외를 던지거나 try catch를 사용해야 하는 등 직접 구현해야 할 부분이 많아 불편하다.
3. 테스트 메소드 별로 공통적으로 처리해야 하는 기능이 있다면, 메소드마다 중복이 생긴다.

## 6강. Junit5 사용법과 테스트 코드 리팩토링
### Junit5에서 사용되는 5가지 어노테이션
- @Test: 테스트 메소드를 지정한다. 테스트 메소드를 실행하는 과정에서 오류가 없으면 성공이다.
- @BeforeEach : 각 테스트 메소드가 수행되기 전에 실행되는 메소드를 지정한다.
- @AfterEach: 각 테스트가 수행된 후에 실행되는 메소드를 지정한다.
- @BeforeAll: 모든 테스트를 수행하기 전에 최초 1회 수행되는 메소드를 지정한다.
- @AfterAll: 모든 테스트를 수행한 후 최후 1회 수행되는 메소드를 지정한다.

```
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JunitCalculatorTest {

    @Test
    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        assertThat(calculator.number).isEqualTo(8)
    }

    @Test
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        assertThat(calculator.number).isEqualTo(15)
    }

    @Test
    fun divideTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.divide(2)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when then
        val message = assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.message 
        assertThat(message).isEqualTo("0으로 나눌 수 없습니다.")
    }
}
```
<자바 개발자를 위한 코틀린 입문> 강의에서 다루었던 scope function을 활용하면 예외에 대한
메세지 검증을 리펙토링 할수도 있다.
```
@Test
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when then
        val message = assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.apply {
            assertThat(message).isEqualTo("0으로 나눌 수 없습니다.")    
        }
        

    }
```

## 7강. Junit5으로 Spring Boot 테스트하기
### Spring Boot 각 계층을 테스트 하는 방법
- 각 계층은 테스트 하는 방법이 다릅니다!

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/07_01.png?raw=true)

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/07_02.png?raw=true)

### 어떤 계층을 테스트 해야 할까?!
- 당연히 최선은 모든 계층에 대해 많은 경우를 검증하는 것!
- 하지만 현실적으로 코딩 시간을 고려해 딱 1개 계층만 테스트 한다면  
  일반적인 상황에서는, 개인적으로 Service 계층 테스트를 선호한다.
- Service 계층을 테스트 함으로써 A를 보냈을 떄 B가 잘 나오는지, 또는 원하는 로직을
  잘 수행하는지 검증할 수 있기 때문이다.

```
@SpringBootTest
class UserServiceTest @Autowired constructor(

    private val userRepository: UserRepository,
    private val userService: UserService,
) {

    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("홍길동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동")
        assertThat(results[0].age).isNull()
    }

}
```
```
  ...
  
  @NotNull
  public String getName() {
    return name;
  }

  @Nullable
  public Integer getAge() {
    return age;
  }
  
  ...
  
```
assertThat().isNull() 이라는 새로운 단언문을 사용한다.
위의 테스트 코드를 실행보면 users[0].age에서 에러가 나게 된다,

### 그 이유는 다음과 같다.
- User getAge()가 Java 타입으로 Integer라고 되어 있는데 (즉, 플랫폼 타입이다)
- Kotlin 입장에서는 int가 nullable인지 non-nullalbe인지 몰라 users[0].age라고만  
  타이핑한 경우 null이 아닌 뱐수에 age를 담으려고 한다.
- 하지만 실제 값은 null이었기에 때문에 null을 non-null 변수에 넣으려다 에러가 난 것이다.

이를 해결하기 위해서는 User 테이블의 getter 2개에 Annotation을 붙여주면 된다.
name은 null이 불가능하니 @NotNull을 붙여주고, age는 null이 가능하니 @Nullable을 붙여주자.

org.jetbrains.annotations.NotNull , org.jetbrains.annotations.Nullable을 활용하면 된다. 

## 8강. 유저관련기능테스트작성하기

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/08_00.png?raw=true)

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/08_01.png?raw=true)

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/08_02.png?raw=true)

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/08_03.png?raw=true)

- 조회 테스트가 먼저 수행되더라도, 에러가 발생할 수 있다.
- 테스트가 끝나면 공유자 원인DB를 깨끗하게 해주자!
```
@AfterEach
fun clean() {
  userRepository.deleteAll()  
}
```

### Kotlin
```
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(

    private val userRepository: UserRepository,
    private val userService: UserService,
) {

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @DisplayName("유저 저장이 정상 동작한다.")
    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("홍길동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동")
        assertThat(results[0].age).isNull()

    }

    @Test
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null)
        ))

        // when
        val results = userService.getUsers()

        // then
        assertThat(results).hasSize(2)
        assertThat(results).extracting("name").containsExactlyInAnyOrder("A", "B")
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    fun updateUserTest() {
        // given
        val saveUser = userRepository.save(User("A", null))
        val request = UserUpdateRequest(saveUser.id, "B")

        // when
        userService.updateUserName(request)

        // then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    @Test
    fun deleteUserTest() {
        // given
        userRepository.save(User("A", null))

        // when
        userService.deleteUser("A")

        // then
        assertThat(userRepository.findAll()).isEmpty()
    }
}
```

## 9강. 책 관련 기능 테스트 작성하기

```
import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.JavaUserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    fun clear() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("책 등록이 정상 동작한다.")
    @Test
    fun saveBookTest() {
        // given
        val request = BookRequest("이상한 나라의 엘리스")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("이상한 나라의 엘리스")
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    fun loanBookTest() {
        // given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("홍길동", null))
        val request = BookLoanRequest("홍길동", "이상한 나라의 엘리스")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("이상한 나라의 엘리스")
        assertThat(results[0].user.id).isEqualTo(saveUser.id)
        assertThat(results[0].isReturn).isFalse()
    }

    @Test
    @DisplayName("책이 진작 대출되어 있다면, 신규 대출이 실패 한다")
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("홍길동", null))

        userLoanHistoryRepository.save(UserLoanHistory(saveUser, "이상한 나라의 엘리스", false))
        val request = BookLoanRequest("홍길동", "이상한 나라의 엘리스")

        // when then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    fun returnBookTest() {
        //given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val saveUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(UserLoanHistory(saveUser, "이상한 나라의 엘리스", false))
        val request = BookReturnRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue()
        
    }
}
```

## 12강. 도메인 계층을 Kotlin으로 변경하기 - Book.java

#### 의존성 주입
```
implementation 'org.jetbrains.kotlin:kotlin-reflect:1.6.21'
```

#### Book.kt
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }
}
```

## 13강. 도메인 계층을 Kotlin으로 변경하기 - UserLoanHistory.java, User.java

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

```
import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import java.lang.IllegalArgumentException
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class User(

    var name: String,

    val age: Int?,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
){

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun loanBook(book: Book) {
        this.userLoanHistories.add(UserLoanHistory(this, book.name, false))
    }

    fun returnBook(bookName: String) {
        this.userLoanHistories.first { history -> history.bookName == bookName}.doReturn()
    }
}
```

## 14강. Kotlin과 JPA를 함께 사용할 때 이야기거리 3가지


1. setter에 관한 이야기
- setter 대신 좋은 이름의 함수를 사용하는 것이 훨씬 clean하다!
- 하지만 name에 대한 setter는 public 이기 때문에 유저 이름 업데이트 기능에서 setter를 사용할 수도 있다.
- 코드 상 setter가 열려있어서 사용할수도 있다는 것이 불편하다.
- public getter는 필요하기 때문에 setter만 private하게 만드는 것이 최선이다!

```
class User(
  private val _name: String,
) {
  val name: String
     get() = this._name
}       
```
#### 방법 1. backing property 사용하기 

```
class User(
  name: String
) {
  val name = name
     private set 
}       
```
#### 방법 2. custom setter 이용하기  

> 정리: 하지만 두 방법 모두 프로퍼티가 많아지면 번거롭다!
> 때문에 개인적으로 setter를 열어는 두지만 사용하지 않는 방법을 선호!
> 다행히 현재 팀에서도 setter를 사용하면 안된다는 사실을 모든 개발자 분들이 체득하고 있다.
> Trade-Off의 영역, 팀 컨벤션을 잘 맞추면 되지 않을까!


### 2. 생성자 안의 프로퍼티. 클래스 body 안의 프로퍼티

다시 User 클래스를 보자. user 클래스 주생성자 안에 있는 userLoanHistories와 id는 꼭  
주 생성자 안에 있을 필요가 없다. 아래와 같이 코드가 바뀔 수 있는 것이다


```
@Entity 
class User( 
  var name: String, 
  val age: Int?, 
) { 

  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true) 
  val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),
  
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  val id: Long? = null, 
  
  fun updateName(name: String) { 
    this.name = name 
  } 
   
  fun loanBook(book: Book) { 
    this.userLoanHistories.add(UserLoanHistory(this, book.name)) 
  } 
  
  fun returnBook(bookName: String) { 
    this.userLoanHistories.first {history -> history.bookName == bookName }.doReturn() 
   
  } 
}
  
```

그렇다. 위 코드도 잘 동작한다. 그렇다면 어떻케 해야 더 좋을까?
개인적으로는 큰 상관이 없다고 생각한다. 테스트를 하기 위한 객체를 만들어 줄때도 정적  
팩토리 메소드를 사용하다 보니 프로퍼티가 안에 있건, 밖에 있건 두 경우 모두 적절히 대응 할 수 있다.
하지만 명확한 가이드가 있는 것은 함계 개발을 할 때에 중요하므로
1. 모든 프로퍼티를 생성자에 넣거나
2. 프로퍼티를 생성자 혹은 클래스 body 안에 구분해서 넣을 때 명확한 기준이 있거나 해야한다고 생각한다.


### 3. JPA와 data class
- Entity는 data class를 피하는 것이 좋다. 
- 그 이유는 equals, hashCode, toString 등의 함수를 자동으로 만들어준다. 사실 원래 세 함수는 JPA Entity와
  그렇케 궁합이 좋치 못하다. 연관관계 상황에서 문제가 될 수 있는 경우들이 존재하기 떄문이다
  예를 들어, 현제 프로젝트에 있는 User와 UserLoanHistory를 생각해보자 

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/14_01.png?raw=true)

1:N 연관계를 맺고 있는 상황에서 User쪽에 equals()가 호출된다면 User 는 본인과 관계를 맺고 있는 UserLoanHistory의  
equals()를 호출하게 되고, 다시 UserLoanHistory는 본인과 관계를 맺고 있는 User의 equals()를 호출하게 된다.
때문에 JPA Entitiy는 data class를 피하는 것이 좋다.



마지막으로 작은 TIP 하나를 더 공유하자며 이 TIP 꼭 JPA와 관련된 것은 아니지만, 현재 Kotlin Class가 Domain만 있으니
이 단계를 말하고자 한다.
- Entityu (class) 가 생성되는 로직을 찾고 싶은 경우, constructor 지시어를 명시적으로 작성하고 추적하면 훨씬 편하다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/14_02.png?raw=true)

이제 Kotlin으로 변경된 도메인 객체들을 남겨두고, Repository로 넘어가보자!


## 15강. 리포지토리를 Kotlin으로 변경하기


#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface BookRepository : JpaRepository<Book, Long> {

    fun findByName(bookName: String): Optional<Book>
}
  
```

#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByName(name: String) : Optional<User>
}
```

#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository

interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long>{

    fun findByBookNameAndIsReturn(bookName: String, isReturn: Boolean) : UserLoanHistory?
}  
```

## 16강. 서비스 계층을 Kotlin으로 변경하기 - UserService.java

```
plugins {
    id 'org.springframework.boot' version '2.6.8'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
    id 'org.jetbrains.kotlin.plugin.jpa' version '1.6.21'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21' / 추가
}
```
#### UserService.java
```
import com.group.libraryapp.domain.user.User;
import com.group.libraryapp.domain.user.UserRepository;
import com.group.libraryapp.dto.user.request.UserCreateRequest;
import com.group.libraryapp.dto.user.request.UserUpdateRequest;
import com.group.libraryapp.dto.user.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public void saveUser(UserCreateRequest request) {
    User newUser = new User(request.getName(), request.getAge(), Collections.emptyList(), null);
    userRepository.save(newUser);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers() {
    return userRepository.findAll().stream()
        .map(UserResponse::new)
        .collect(Collectors.toList());
  }

  @Transactional
  public void updateUserName(UserUpdateRequest request) {
    User user = userRepository.findById(request.getId()).orElseThrow(IllegalArgumentException::new);
    user.updateName(request.getName());
  }

  @Transactional
  public void deleteUser(String name) {
    User user = userRepository.findByName(name).orElseThrow(IllegalArgumentException::new);
    userRepository.delete(user);
  }

}
```
#### Kotlin
```
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import com.group.libraryapp.dto.user.response.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun saveUser(request: UserCreateRequest) {
        val newUser = User(request.name, request.age)
        userRepository.save(newUser)
    }

    @Transactional(readOnly = true)
    fun getUsers(): List<UserResponse> {
        return userRepository.findAll()
            .map { user -> UserResponse(user) }
    }

    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findById(request.id).orElseThrow(::IllegalArgumentException)
        user.updateName(request.name)
    }

    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name).orElseThrow(::IllegalArgumentException)
        userRepository.delete(user)
    }

}
```

## 17강. BookService.java를 Kotlin으로 변경하고 Optional 제거하기

#### Kotlin
```
package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,

) {

    @Transactional
    fun saveBook(request: BookRequest) {
        val book = Book(request.name)
        bookRepository.save(book)
    }

    @Transactional
    fun loanBook(request: BookLoanRequest) {
        val book = bookRepository.findByName(request.bookName).orElseThrow(::IllegalArgumentException)
        if (userLoanHistoryRepository.findByBookNameAndIsReturn(request.bookName, false) != null) {
            throw java.lang.IllegalArgumentException("진작 대출되어 있는 책입니다.")
        }

        val user = userRepository.findByName(request.userName).orElseThrow(::IllegalArgumentException)
        user.loanBook(book)

    }

    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user= userRepository.findByName(request.userName).orElseThrow(::IllegalArgumentException)
        user.returnBook(request.bookName)

    }
}
```

JDK 8의 등장한 옵셔널은 어떤 값이 null 될 수 있는지 없는지를 잘 나타내주기 위해 생겨났다.
Kotlin에서는 언저 자체 타입 시스템이 물음표를 통해서 어떤 값이 null인지 아닌지 잘 알려주기 떄문에 필요가 없다.

```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    //fun findByName(name: String) : Optional<User>
    
    fun findByName(name: String) : User?
}
```

```
    ```
    
    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name).orElseThrow(::IllegalArgumentException)
        userRepository.delete(user)
    }
    
    
    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name) ?: throw java.lang.IllegalArgumentException()
        userRepository.delete(user)
    }
    
    ```
    
```

### ?: throw IllegalArgumentException() 반복 코드 발생 - 리팩토링

#### ExceptionUtils.kt
```
import java.lang.IllegalArgumentException

fun fail() : Nothing {
    throw IllegalArgumentException()
}
```
```
    ```
    
    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user= userRepository.findByName(request.userName) ?: fail()
        user.returnBook(request.bookName)

    }
    
    ```
```

#### 확잘 함수를 통해 Optional을 제어할 수 있음

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/17_01.png?raw=true)

#### CrudRepositoryExtensions.kt
```
fun <T, ID> CrudRepository<T, ID>.findByIdOrNull(id: ID): T? = findById(id).orElse(null)
```

```
    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findByIdOrNull(request.id) ?: fail()
        user.updateName(request.name)
    }
```

### 확장함수 직접 만들
```
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import java.lang.IllegalArgumentException

fun fail() : Nothing {
    throw IllegalArgumentException()
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T {
  return this.findByIdOrNull(id) ?: fail()
}
```

```
    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findByIdOrThrow(request.id)
        user.updateName(request.name)
    }
```

## 18강. DTO를 Kotlin으로 변경하기
![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/18_01.png?raw=true)

#### Kotlin 
```
import com.group.libraryapp.domain.user.User

class UserResponse(
    user: User
) {
    val id: Long
    val name: String
    val age: Int?

    init {
        id = user.id!!
        name = user.name
        age = user.age
    }
}

```

#### Kotlin - init에서 부생성자로 리팩토링
```
import com.group.libraryapp.domain.user.User

class UserResponse(
    val id: Long,
    val name: String,
    val age: Int?,
) {

    constructor(user: User) : this(
        id = user.id!!,
        name = user . name,
        age = user.age
    )
}
```

#### Kotlin - 정적팩토리 메소드 리팩토링
```
import com.group.libraryapp.domain.user.User

data class UserResponse( // dto 전체적으로 data 추가
    val id: Long,
    val name: String,
    val age: Int?,
) {

    companion object {
        fun of(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user . name,
                age = user.age)
        }
    }
}
```
> 데이터 트랜스퍼 오브젝트는 즉 DTO는 상황에 따라서 equals나 hashcode 혹은 
> toString 등을 디버깅 과정이나 테스트 과정에서 사용할 수도 있다.
> 그래서 DTO를 항상 그 취지에 맞게 data 클래스로 만들어주는 편이다. 

## 19. Controller 계층을 Kotlin으로 변경하기

#### Java
```
import com.group.libraryapp.dto.book.request.BookLoanRequest;
import com.group.libraryapp.dto.book.request.BookRequest;
import com.group.libraryapp.dto.book.request.BookReturnRequest;
import com.group.libraryapp.service.book.BookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping("/book")
  public void saveBook(@RequestBody BookRequest request) {
    bookService.saveBook(request);
  }

  @PostMapping("/book/loan")
  public void loanBook(@RequestBody BookLoanRequest request) {
    bookService.loanBook(request);
  }

  @PutMapping("/book/return")
  public void returnBook(@RequestBody BookReturnRequest request) {
    bookService.returnBook(request);
  }

}
```

#### Kotlin
```
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.service.book.BookService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BookController(
    private val bookService: BookService,
) {

    @PostMapping("/book")
    fun saveBook(@RequestBody request: BookRequest) {
        bookService.saveBook(request)
    }

    @PostMapping("/book/loan")
    fun loanBook(@RequestBody request: BookLoanRequest) {
        bookService.loanBook(request)
    }

    @PutMapping("/book/return")
    fun returnBook(@RequestBody request: BookReturnRequest) {
        bookService.returnBook(request)
    }

}
```

코틀린은 함수문법에서 Block Body 쓰지 않고 = 도 가능
```
    @GetMapping("/user")
    fun getUser() : List<UserResponse> = userService.getUsers()
    
```

#### Java
```
import com.group.libraryapp.dto.user.request.UserCreateRequest;
import com.group.libraryapp.dto.user.request.UserUpdateRequest;
import com.group.libraryapp.dto.user.response.UserResponse;
import com.group.libraryapp.service.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/user")
  public void saveUser(@RequestBody UserCreateRequest request) {
    userService.saveUser(request);
  }

  @GetMapping("/user")
  public List<UserResponse> getUsers() {
    return userService.getUsers();
  }

  @PutMapping("/user")
  public void updateUserName(@RequestBody UserUpdateRequest request) {
    userService.updateUserName(request);
  }

  @DeleteMapping("/user")
  public void deleteUser(@RequestParam String name) {
    userService.deleteUser(name);
  }

}

```

#### Kotlin
```
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import com.group.libraryapp.dto.user.response.UserResponse
import com.group.libraryapp.service.user.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(

    private val userService: UserService,
) {

    @PostMapping("/user")
    fun saveUser(@RequestBody request: UserCreateRequest) {
        userService.saveUser(request)
    }

    @GetMapping("/user")
    fun getUser() : List<UserResponse> {
        return userService.getUsers()
    }

    @PutMapping("/user")
    fun updateUserName(@RequestBody request: UserUpdateRequest) {
        userService.updateUserName(request)
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestParam name: String) {
        userService.deleteUser(name)
    }

}
```

#### Kotlin
```
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryAppApplication

fun main(args: Array<String>) {
    runApplication<LibraryAppApplication>(*args)
}
```

Spring Boot는 @RestController 어노테이션이 달린 경우 Jackson 라이브러리가 직렬화와 역직렬화를 담당하게 된다.
변환 과정에서는 큰 문제는 없지만 스프링부트와 Kotlin을 함께 사용하게 된다면 달라진다.
의존성 추가로 해결!

#### build.gradle
```
implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2'
```

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


