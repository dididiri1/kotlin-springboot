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

